/**
   $Id: Monitor.java 589 2011-05-18 16:42:27Z mviara $

   Copyright (c) 2010, Mario Viara

   Permission is hereby granted, free of charge, to any person obtaining a
   copy of this software and associated documentation files (the "Software"),
   to deal in the Software without restriction, including without limitation
   the rights to use, copy, modify, merge, publish, distribute, sublicense,
   and/or sell copies of the Software, and to permit persons to whom the
   Software is furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
   ROBERT M SUPNIK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

   Except as contained in this notice, the name of Mario Viara shall not be
   used in advertising or otherwise to promote the sale, use or other dealings
   in this Software without prior written authorization from Mario Viara.
*/
package jmce;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Collections;
import java.io.*;

import jmce.sim.*;
import jmce.sim.cpu.AbstractCPU;

import jmce.util.Hex;
import jmce.sim.terminal.Terminal;
import jmce.sim.Hardware;
import jmce.sim.TraceListener;



/**
 * Debug monitor for JMCE CPU.
 *
 * @author Mario Viara
 * @version 1.01
 * 
 */
public class Monitor  extends AbstractHardware implements ExceptionListener , TraceListener, Runnable, jmce.sim.Console
{
	private Vector<Command> cmds = new Vector<Command>();
	private boolean exit;
	private Device<Integer> console;
	private Terminal terminal;
	CPU cpu;
	private Jmce jmce;
	private Vector<BufferedReader> bufferedReaders = new Vector<BufferedReader>();
	BufferedReader rd = null;
	Timer timer = new Timer(true);
	private int from = -1;
	PrintStream trace = null;
	Thread thread = new Thread(this);


	public CPU getCPU()
	{
		return cpu;
	}
	
	public void addCommand(Command cmd)
	{
		cmds.add(cmd);
	}

	public int getCommandCount()
	{
		return cmds.size();
	}

	public Command getCommandAt(int i)
	{
		return cmds.elementAt(i);
	}

	Exception  addBuffer(String name) 
	{
		print("input from "+name);
		File file = new File(name);
		if (file.exists() == false)
		{
			println(" not exists");
			return null;
		}
		else
		{
			println(" length="+file.length());
		}
		
		try
		{
			BufferedReader rdNew = new BufferedReader(new FileReader(name));
			if (rd != null)
			{
				bufferedReaders.add(rd);
				rd = rdNew;
			}
			else
				rd = rdNew;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return e;
		}

		return null;

	}
	
	void nextBuffer()
	{
		rd = null;
		if (bufferedReaders.size() > 0)
		{
			rd = bufferedReaders.get(0);
			bufferedReaders.remove(0);
		}
	}
	
	public int getch() throws Exception
	{
		while (rd != null)
		{
			try
			{
				int ch = rd.read();
				if (ch < 0)
				{
					try
					{
						rd.close();
					}
					catch (Exception discard)
					{
					}
					nextBuffer();
				}
				else
					return ch;
			}
			catch (Exception ex)
			{
				try
				{
					rd.close();
				}
				catch (Exception discard)
				{
				}
				nextBuffer();
			}
			
		}
		
		return console.readOutput();
	}


	public void printStatusLine(int n,String s)
	{
		if (terminal != null)
			terminal.printStatusLine(n,s);
	}
	
	public void println() 
	{
		putchar(13);
		putchar(10);
	}

	public void println(Object o) 
	{
		print(o);
		println();
	}

	public void print(Object o) 
	{
		String s = String.valueOf(o);
		
		for (int i = 0 ; i < s.length() ; i++)
			putchar(s.charAt(i));
	}

	public void putchar(int c) 
	{
		try
		{
			console.writeInput(c);
		}
		catch (Exception ex)
		{
		}
	}

	public void getLine(String prompt,StringBuffer line) throws Exception
	{
		boolean refresh = true;

		for (;;)
		{
			if (refresh)
			{
				line.setLength(0);
				refresh = false;
				print(prompt);
			}

			int c = getch();

			switch (c)
			{
				default:
					if (c >= 32 && c < 128)
					{
						line.append((char)c);
						putchar(c);
					}
					else
						printStatusLine(0,"Invalid key "+c);
					break;
				case	8:
					if (line.length() > 0)
					{
						line.delete(line.length() -1,line.length());
						print("\010 \010");
					}
					break;

				case	13:
				case	10:
					println();
					return;
				case	27:
					println();
					refresh = true;
					break;
			}
		}

	}


	public void run()
	{
		StringBuffer line = new StringBuffer();
		TimerTask t1 = new TimerTask()
		{
			public void run()
			{
				if (cpu != null && cpu.isRunning())
					printStatusLine(0,cpu.getUsageDesc());
				else
					printStatusLine(0,"");
			}
			
		};
		timer.schedule(t1,1000,1000);

		selectCPU();
		println("JMCE Monitor $Id: Monitor.java 589 2011-05-18 16:42:27Z mviara $");
		
		
		while (!exit)
		{
			try
			{
				getLine(">",line);
			}
			catch (Exception e)
			{
				println(e);
			}
			
			if (line.length() == 0)
				continue;
			
			StringToken st = new StringToken(line.toString());

			// Ignore comment
			String c0 = st.getStringAt(0).toLowerCase();
			if (c0.startsWith("rem") ||
			    c0.startsWith(";")   ||
			    c0.startsWith("#")   ||
			    c0.startsWith("//")  ||
			    c0.startsWith("'"))
				continue;
			
			int count = 0;
			Command cmd = null;
			
			for (int i = 0 ; i < getCommandCount() ; i++)
			{
				Command c = getCommandAt(i);
				if (c.compare(st))
				{
					count++;
					cmd = c;

				}

			}

			/*
			 * The line is not a command chek if is one
			 * register and display or show it.
			 */
			if (count == 0)
			{
				Register r = null;
				
				if (cpu != null)
				{
					r = cpu.getRegisterForName(st.getStringAt(0));

					if (r != null)
					{
					try
					{
						println(r.getName()+" = "+r.hexValue());

						if (st.getTokenCount() > 1)
						{
							r.setRegister(getHexNumber(st,1));
							println(r.getName()+" = "+r.hexValue());
						}
					}
					catch (Exception ex)
					{
						println(ex);
					}
					}
				}

				if (r == null)
					println("Invalid command '"+line+"'");

			}
			else if (count > 1)
			{
				println("Too many candidates for '"+line+"'");
				for (int i = 0 ; i < getCommandCount() ; i++)
				{
					Command c = getCommandAt(i);
					if (c.compare(st))
						println("\t"+c.toString());
				}
				
			}
			else
			{
				try
				{
					cmd.exec(st);
				}
				catch (Exception ex)
				{
					println(ex);
					ex.printStackTrace(System.out);
					
				}
			}
		}

		timer.cancel();
		try
		{
			jmce.cpu.destroy();

		}
		catch (Exception ignore)
		{
		}
		System.exit(0);
		
	}

	public void error(String s) throws Exception
	{
		throw new CommandException(s);
	}

	void checkRunning() throws Exception
	{
		if (cpu != null)
			if (cpu.isRunning())
				error("CPU already running");
	}
	
	void checkCPU() throws Exception
	{
		if (cpu == null)
		{
			if (cpu == null)
				error("CPU not loaded, try 'add  ...'");
		}
	}
	

	void checkArg(int n,StringToken line) throws Exception
	{
		if (n >= line.getTokenCount())
			error(n+" Arguments() are required");
	}

	Memory getArgMemory(StringToken st,int n) throws Exception
	{
		checkArg(n,st);

		String s = st.getTokenAt(n);

		for (int i = 0 ; i < cpu.getMemoryCount() ; i++)
		{
			Memory m = cpu.getMemoryAt(i);
			if (m.getName().equalsIgnoreCase(s))
				return m;
		}

		return cpu.getMemoryAt(st.getTokenInt(n));
	}
	
	int getHexNumber(StringToken st,int i) throws Exception
	{
		checkArg(i,st);
		
		String s = st.getTokenAt(i);

		// Search for register
		Register r = null;

		r = cpu.getRegisterForName(s);

		if (r != null)
			return r.getRegister();

		return  st.getTokenHex(i);

	}

	/**
	 * Set the Terminal / Console used bye the monitor.
	 *
	 * @since 1.01
	 */
	public void setConsole(Device<Integer> c)
	{
		console = c;
		if (c instanceof Terminal)
			terminal = (Terminal)c;
		else
			terminal = null;
	}
	

	public void init(Hardware parent) throws SIMException
	{
		/**
		 * Install the console in not alread present
		 */
		if (console == null)
		{
			console = new Device<Integer>();
			@SuppressWarnings("unchecked")
			Device<Integer> t = (Device<Integer>)getHardware(console.getClass());

			if (t == null)
			{
				t = Terminal.createTerminal();
				addHardware(t);
			}

			setConsole(t);
		}
		
		super.init(parent);
	}

	public void reset() throws SIMException
	{
		super.reset();
		if (!thread.isAlive())
			thread.start();
	}

	public Tape getTape() throws Exception
	{
		checkCPU();
		Tape tape = (Tape)cpu.getHardware(Tape.class);

		if (tape == null)
			throw new Exception("Tape not installed");

		return tape;
	}
	
	public Monitor()
	{
		setName("Monitor");
		jmce = Jmce.getInstance();

		addCommand(new Command(this,"realtime\t[on|off","Enable/disable realtime")
		{
			public void exec(StringToken st) throws Exception
			{
				String s;
				
				checkCPU();

				if (st.getTokenCount() < 2)
				{
					println("Realtime is "+cpu.getRealTime());
				}
				else
				{
					s = st.getTokenAt(1);

					if (s.equalsIgnoreCase("off"))
					{
						cpu.setRealTime(false);
						if (trace != null)
							trace.close();
						println("Realtime false");
					}
					else
					{
						cpu.setRealTime(true);
						println("Realtime true");
					}

				}

			}

		});
		
		addCommand(new Command(this,"tape rewind","Rewind tape")
		{
			public void exec(StringToken st) throws Exception
			{
				Tape tape = getTape();
				tape.play();

			}
		});

		addCommand(new Command(this,"tape play","Play tape")
		{
			public void exec(StringToken st) throws Exception
			{
				Tape tape = getTape();
				tape.play();

			}
		});
		
		addCommand(new Command(this,"tape rec","Record tape")
		{
			public void exec(StringToken st) throws Exception
			{
				Tape tape = getTape();
				tape.rec();

			}
		});

		
		addCommand(new Command(this,"tape stop","Stop tape")
		{
			public void exec(StringToken st) throws Exception
			{
				Tape tape = getTape();
				tape.stop();

			}
		});
		addCommand(new Command(this,"tape config\t[file]","Set show tape configuration")
		{
			public void exec(StringToken st) throws Exception
			{
				Tape tape = getTape();

				if (st.getTokenCount() >= 3)
					tape.setConfig(st.getTokenAt(2));

				println("Tape config '"+tape.getConfig()+"'");
				
			}
		});
		
		addCommand(new CommandExamine(this));
		addCommand(new CommandTrap(this));
		addCommand(new CommandRegister(this));
		
		addCommand(new Command(this,"trace\t[file|off]","Enable/disable/show file")
		{
			public void exec(StringToken st) throws Exception
			{
				String s;

				checkCPU();
				
				if (st.getTokenCount() < 2)
				{
					s = trace == null ? "OFF" : "ON";
					
					println("Trace is "+s);
				}
				else
				{
					s = st.getTokenAt(1);

					if (s.equalsIgnoreCase("off"))
					{
						if (trace != null)
							trace.close();
						println("Trace off");
					}
					else
					{
						trace = new PrintStream(new FileOutputStream(s));
						cpu.addTraceListener(Monitor.this);
						println("Trace on "+s);
					}
							
				}

			}
		});
		
		addCommand(new Command(this,"quit","Terminate the simulator")
		{
			public void exec(StringToken cmdLine)
			{
				exit = true;
			}
		});
		
		addCommand(new Command(this,"?","Show command list")
		{
			
			public void exec(StringToken cmdLine) throws Exception
			{
				Vector<String> v = new Vector<String>();
				int i;
				int l = 0;
				Command c;
				
				for (i = 0 ; i < getCommandCount() ;i++)
				{
					
					c = getCommandAt(i);
					if (c.getCmd().length() > l)
						l = c.getCmd().length();
				}
				
				for (i = 0 ; i < getCommandCount() ;i++)
				{
					c = getCommandAt(i);
					String s = c.getCmd();
					while (s.length() < l)
						s = s +" ";
					
					v.add(s+" -> "+c.getHelp());
				}
				
				Collections.sort(v);
				for (i = 0 ; i < v.size() ; i++)
				{
					if (pressToContinue(i))
						break;
					println(v.elementAt(i));
				}
					
			}
			
		});

		addCommand(new Command(this,"@","Exec command from file")
		{
			public void exec(StringToken st) throws Exception
			{
				checkArg(1,st);
				//checkCPU();
				Exception e = addBuffer(st.getStringAt(1));
				if (e != null)
					println(e);
			}
			
		});
		
		addCommand(new Command(this,".","Display cpu register")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();
				showCpu();
			}
			
		});

		addCommand(new Command(this,"break read\t[mem] add","Set break on read")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();

				int m = st.getTokenInt(2);
				int a = st.getTokenHex(3);
				cpu.addReadBreakPoint(m,a);
			}
		});
		
		addCommand(new Command(this,"break write\t[mem] add","Set break on write")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();

				int m = st.getTokenInt(2);
				int a = st.getTokenHex(3);
				cpu.addWriteBreakPoint(m,a);
			}
		});
		
		addCommand(new Command(this,"break exec\t[mem] add","Set break on exec")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();

				int m = st.getTokenInt(2);
				int a = st.getTokenHex(3);
				cpu.addExecBreakPoint(m,a);
			}
		});
		
		addCommand(new Command(this,"break cancel\tn","Cancel break")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();
				int n =st.getTokenInt(2);
				BreakPoint bp = cpu.getBreakPointAt(n);

				println(bp.toString()+" Cancelled.");
				cpu.removeBreakPoint(n);
			}
		});



		addCommand(new Command(this,"break disable\tn","Disable break")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();

				BreakPoint bp = cpu.getBreakPointAt(st.getTokenInt(2));

				println(bp.toString()+" Disabled");
				bp.setEnabled(false);
			}
		});

		addCommand(new Command(this,"break enable\tn","Enable break")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();

				BreakPoint bp = cpu.getBreakPointAt(st.getTokenInt(2));

				println(bp.toString()+" Enabled");
				bp.setEnabled(true);
			}
		});
		
		addCommand(new Command(this,"break list","Show define breaks")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();

				println("Break point list.");
				
				for (int i = 0 ; i < cpu.getBreakPointCount() ; i++)
				{
					BreakPoint bp;
					
					bp = cpu.getBreakPointAt(i);
					println(i+". "+bp.toString()+" Count="+bp.getFireCount());
				}
			}
		});

		addCommand(new Command(this,"cpu statistics","Dump cpu statistics")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();
				cpu.dumpStatistics("cpu.txt");
			}
		});
		
		addCommand(new Command(this,"show memory","Show memory list")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();

				println("Memory list");
				
				for (int i = 0 ; i < cpu.getMemoryCount() ; i++)
				{
					Memory m = cpu.getMemoryAt(i);
					println(i+". "+m.toString());
				}
			}
		});
		
		addCommand(new Command(this,"show cpu","Show cpu statistics")
		{
			public void show(String s1,String s2)
			{
				while (s1.length() < 32)
					s1 = " "+s1;
				println(s1+" = "+s2);
			}

			public void show(String s1,boolean b)
			{
				show(s1,b ? "true" : "false");
				
			}
			
			public void show(String s1,long n)
			{
				show(s1,""+n);
			}
			
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();
				show("CPU name",cpu.getName());
				show("Realtime ",cpu.getRealTime());
				show("Clock ",cpu.getClock());
				show("Clock per cycle",cpu.getClockPerCycle());
				show("Endian",cpu.isBigEndian() ? "Big" : "Little");
				show("Register",cpu.getRegisterCount());
				show("Peripheral",cpu.getHardwareCount());
				for (int i = 0 ; i < cpu.getHardwareCount() ;i++)
					show("Peripheral #"+i,cpu.getHardware(i).toString());
				show("Cycle",cpu.getCycle());
				show("Running",cpu.isRunning());
				show("Interrupt",cpu.isInterruptEnabled() ? "enabled" : "disabled");

				AbstractCPU a = (AbstractCPU)cpu;
				for (int i = 0 ; i < a.getInterruptCount() ;i++)
				{
					if (a.getInterruptCounter(i) > 0)
						show("Interrupt #"+i,a.getInterruptName(i)+" Count="+a.getInterruptCounter(i));
				}
			}
		});

		addCommand(new Command(this,"go\t[till]","Start cpu with optional break")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();
				checkRunning();
				showCpu();
				println(cpu.toString()+" - Started");
				if (st.getTokenCount() > 1)
					cpu.setTill(st.getTokenHex(1));
				cpu.start();
			}
		});

		addCommand(new Command(this,"halt","Stop cpu")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();
				if (!cpu.isRunning())
					println(cpu.toString()+" - Already stopped");
				else
				{
					cpu.stop();
					println(cpu.toString()+" - Stopped");
				}
				showCpu();
			}
		});

		addCommand(new Command(this,"reset","Reset CPU")
		{
			public void exec(StringToken st) throws Exception
			{
				checkRunning();
				cpu.reset();
			}

		});
		addCommand(new Command(this,"load\t[base] file","Load file in memory")
		{
			public void exec(StringToken st) throws Exception
			{
				int base = 0;
				String file = null;
				
				checkCPU();
				checkRunning();
				checkArg(1,st);
				LoadInfo info = new LoadInfo();
				if (st.getTokenCount() > 2)
				{
					base = getHexNumber(st,1);
					file = st.getStringAt(2);
				}
				else
					file = st.getStringAt(1);
				cpu.load(file,base,info);
				println(file+" loaded at "+Hex.formatWord(info.start)+" - "+Hex.formatWord(info.end));
			}
			
		});

		addCommand(new Command(this,"xml load\t[cpu.xml]","Load new xml configuration")
		{
			public void exec(StringToken st) throws Exception
			{
				String s = st.getTokenCount() > 2 ? st.getTokenAt(2) : Jmce.JMCE_XML;
				println("Decode CPU from "+s);
				Object o = jmce.decode(s);
				jmce.setCPU(o);
				selectCPU();
			}
			
		});
		
		addCommand(new Command(this,"xml save\t[cpu.xml]","Save current configuration")
		{
			public void exec(StringToken st) throws Exception
			{
				String s = st.getTokenCount() > 2 ? st.getTokenAt(2) : Jmce.JMCE_XML;
				println("Encode CPU to "+s);
				jmce.encode(s);
			}
		});
		
		
		addCommand(new Command(this,"clock\tn","Set cpu clock to n")
		{
			public void exec(StringToken st) throws Exception
			{
				checkCPU();
				cpu.setClock(st.getTokenInt(1));
			}

		});
		
		addCommand(new Command(this,"show config","Show cpu configuration")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();
				Jmce.showConfig(cpu,Monitor.this);
			}
		});
		
		addCommand(new Command(this,"add\tclass","Add new class")
		{
			@SuppressWarnings("rawtypes")
			public void exec(StringToken cmdLine) throws Exception
			{
				checkArg(1,cmdLine);
				String s = cmdLine.getTokenAt(1);
				Hardware parent = jmce.cpu;
				if (cmdLine.getTokenCount() > 2)
				{
					parent = parent.getHardware(cmdLine.getTokenInt(2));
				}
				Class clazz = Class.forName(s);
				Object o = clazz.newInstance();
				if (!(o instanceof jmce.sim.Hardware))
					error("Not instance of jmce.sim.Hardware");
				
				Hardware  h = (Hardware)o;

				if (parent == null)
				{
					jmce.setCPU(o);
					h.reset();
					
				}
				else
				{
					h.init(parent);
					h.initSwing(parent);
					h.reset();
					jmce.addChild(parent,h);
				}
				
				
				
				if (cpu == null && o instanceof CPU)
					selectCPU((CPU)o);

				    
			}
			
		});

		
		addCommand(new Command(this,"unassemble\t[add]","Disassmble program")
		{

			public void exec(StringToken st) throws Exception
			{
				checkCPU();
				if (from < 0)
					from = cpu.pc();
				if (st.getTokenCount() > 1)
					from = getHexNumber(st,1);
				for (int i = 0 ; i < 16 ; i++)
				{
					println(cpu.decodeAt(from));
					from += cpu.getLenghtAt(from);
				}
				
			}			
			
		});

		addCommand(new Command(this,"dump\t[mem] add","Dump memory")
		{
			int from = 0;
			Memory m = null;
			
			public void exec(StringToken st) throws Exception
			{
				checkCPU();

				if (m == null)
					m = cpu.getMemoryAt(0);
				
				if (st.getTokenCount() > 2)
				{
					m = getArgMemory(st,1);
					from = getHexNumber(st,2);
				}
				else if (st.getTokenCount() > 1)
					from = getHexNumber(st,1);

				println(m.getName()+" at "+Hex.formatWord(from));
				
				for (int i = 0 ; i < 8 ; i++)
				{
					String s = Hex.formatWord(from)+" ";
					String s1 = "";
					String s2 = "";
					for (int j = 0 ; j < 16 ; j ++)
					{
						byte c = (byte)m.getMemory(from+j);
						s1 += Hex.formatByte(c&0xff);
						s1 += j == 7 ? '.' : ' ';
						s2 += c < 32 || c > 127 ? '.' : (char)(c);
					}
					println(s+s1+" "+s2);
					from += 16;
				}
			}			

		});

		addCommand(new Command(this,"pass","Exec to next line")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();
				checkRunning();
				int till = cpu.pc() +cpu.getLenghtAt(cpu.pc());
				cpu.setTill(till);
				from = till;
				cpu.start();
			}			
		});
		
		addCommand(new Command(this,"step","Exec one istructions")
		{
			public void exec(StringToken cmdLine) throws Exception
			{
				checkCPU();
				checkRunning();
				cpu.step();
				showCpu();
				from = cpu.pc();

			}			
		});
	}

	private void selectCPU()
	{
		if (jmce.cpu == null)
			return;
		
		if (jmce.cpu instanceof Hardware)
			selectCPU(jmce.cpu);
		else
		{
			CPU cpu = (CPU)jmce.cpu.getHardware(CPU.class);
			selectCPU(cpu);
		}

	}

	public void selectCPU(CPU cpu)
	{

		if (this.cpu != null)
			this.cpu.removeExceptionListener(this);
		if (cpu != null)
			cpu.addExceptionListener(this);
		this.cpu = cpu;
	}

	public void trace(String s) throws SIMException
	{
		trace.println(s);
	}
	
	public 	void exceptionEvent(ExceptionEvent ev)
	{
		cpu.stop();
		println(ev.toString());
		try
		{
			from = cpu.pc();
			showCpu();
		}
		catch (Exception ignore)
		{
		}

	}

	public void showCpu() throws Exception
	{
		for (int i = 0 ; i < cpu.getRegisterCount () ; i++)
		{
			if (terminal != null)
			{
				if (terminal.getCol() + 8 > terminal.getNumCol())
					println();
			}
			Register r = cpu.getRegisterAt(i);
			print(r.getName()+"="+r.descValue()+" ");
		}
		
		println();
		println(cpu.decodeAt(cpu.pc()));
	}

}

class CommandTrap extends Command implements MemoryReadListener,MemoryWriteListener
{
	CommandTrap(Monitor m)
	{
		super(m,"trap\t[mem] [on|off]","Set / unset / show trap on memory");
	}

	public void exec(StringToken st) throws Exception
	{
		monitor.checkCPU();
		Memory m = monitor.cpu.getMemoryForName("IO");
		String cmd = "";
		
		if (st.getTokenCount() > 2)
		{
			m = monitor.getArgMemory(st,1);
			cmd = st.getTokenAt(2);
		}
		else if (st.getTokenCount() > 1)
			cmd = st.getTokenAt(1);

		print("Trap on "+m.getName()+" : ");
		
		if (cmd.equalsIgnoreCase("on"))
		{
			m.addMemoryWriteListener(this);
			m.addMemoryReadListener(this);
			println("ON");
		} else if (cmd.equalsIgnoreCase("off"))
		{
			m.removeMemoryWriteListener(this);
			m.removeMemoryReadListener(this);
			println("OFF");
		}
		else
		{
			boolean found = false;

			for (int i = 0; i < m.getMemoryWriteListenerCount() ; i++)
				if (m.getMemoryWriteListenerAt(i) == this)
					found = true;
			
			println("is "+(found ? "ON" : "OFF"));
		}
		
		
	}			

	public int readMemory(Memory m,int address,int value) throws SIMException
	{
		int n = m.getMemoryReadListenerCount(address);
		if (n == 0)
			throw new SIMException("TRAP I/O Read Address="+Hex.formatWord(address));
		return value;
	}

	public void writeMemory(Memory m,int address,int value,int oldValue) throws SIMException
	{
		int n = m.getMemoryWriteListenerCount(address);

		if (n == 0)
			throw new SIMException("TRAP I/O Write Address="+Hex.formatWord(address)+" Value="+Hex.formatByte(value));
	}

}

class CommandRegister extends Command
{
	CommandRegister(Monitor m)
	{
		super(m,"register\treg [value]","Examine / change register");
	}

	public void exec(StringToken st) throws Exception
	{
		CPU cpu = getCPU();
		String name = st.getStringAt(1);
		Register r = cpu.getRegisterForName(name);

		if (r == null)
			println(name+" register not found");
		else
		{
			println(r.getName()+" = "+r.hexValue());
			
			if (st.getTokenCount() > 2)
			{
				int value = st.getTokenHex(2);
				r.setRegister(value);
				println(r.getName()+" = "+r.hexValue());

			}
		}

	}			


}

class CommandExamine extends Command
{
	int address = 0;
	
	CommandExamine(Monitor m)
	{
		super(m,"examine\t[mem] add","Examine / change memory");
	}
	
	public void exec(StringToken st) throws Exception
	{
		monitor.checkCPU();
		Memory m = monitor.cpu.getMemoryAt(0);

		if (st.getTokenCount() > 2)
		{
			m = monitor.getArgMemory(st,1);
			address = getHexNumber(st,2);
		}
		else if (st.getTokenCount() > 1)
			address = getHexNumber(st,1);

		println("Examine :"+m.getName()+" at "+Hex.formatWord(address));

		for (;;)
		{
			StringBuffer line = new StringBuffer();
			print(Hex.formatWord(address)+" "+Hex.formatByte(m.getMemory(address)));
			getLine(" ",line);

			if (line.length() == 0)
			{
				address++;
				continue;
			}
						

			
			st = new StringToken(line.toString());
			
			if (st.getTokenAt(0).charAt(0) == '.')
				return;

			int v;
			
			try
			{
				v = getHexNumber(st,0);
			}
			catch (Exception ex)
			{
				println(ex);
				continue;
			}
			
			m.setMemory(address,v);
			println(Hex.formatWord(address)+" ==> "+Hex.formatByte(m.getMemory(address)));
			address++;

		}
	}			

}


