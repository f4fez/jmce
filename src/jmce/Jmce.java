/**
   $Id: Jmce.java 946 2012-12-02 11:01:18Z mviara $

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

import java.io.*;

import jmce.sim.*;

import jmce.sim.terminal.SampleTerminal;
import jmce.sim.serial.TCPServer;
import jmce.util.Logger;

import jmce.sim.disk.*;



/**
 * Main class for jmce.
 * <p>
 * Process command line options and launch the monitor or the selected
 * cpu.
 *
 * @author Mario Viara
 * @version 1.02
 */
public class Jmce extends AbstractHardware implements ExceptionListener
{
	private static Logger log = Logger.getLogger(Jmce.class);
	static public String versionNumber = Svnversion.VERSION;
	static public String version1 = "JMCE (Java Multiple Computer Emulator) Version "+Svnversion.VERSION+" Build "+Svnversion.SVNVERSION;
	static public String version2 = "Copyright (c) 2010-2012 Mario Viara - http://www.viara.eu/en/jmce";
	static public final String JMCE_XML = "cpu.xml";
	static Jmce instance = null;
	
	private String name = null;
	private int monitorType = 0;
	private int port;
	private boolean monitor = false;
	private boolean debugger = false;
	public CPU cpu = null;

	static public Jmce getInstance()
	{
		return instance;
	}

	static void usage()
	{
		System.err.println(version1);
		System.err.println(version2);
		System.err.println("usage : jmce [options] class|xml\n");
		System.err.println(" class  Is a class name of one CPU.");
		System.err.println(" xml    Is the name of xml file with a configuration.\n");
		System.err.println(" options : -g class -t type -p port -l file -m :\n");
		System.err.println(" -g class  Set the  default name for the class CRT.");
		System.err.println(" -t type   Set the type of system monitor :");
		System.err.println("            0 - Monitor on terminal (default).");
		System.err.println("            1 - Monitor on TCP port (see -p).");
		System.err.println(" -p port   Set the TCP port for monitor (see -t 1).");
		System.err.println(" -l file   Set default file for log config. Default logging.properties");
		System.err.println(" -m        Enable monitor implies -g jmce.sim.terminal.SwingCRT");
		System.err.println(" -d        Elable the debugger");
		System.err.println();
		System.err.println(" try:jmce jmce.sinclair.spectrum.Spectrum48K to start ZX (require swing).");
		System.err.println("  or:jmce jmce.z80pack.Z80Pack to start Z80pack with default configuration.");
	}

	void error(String s)
	{
		usage();
		System.err.println("\nError : "+s+"\n");
		System.exit(0);
	}
	
	
	void invalidOption(String s)
	{
		error(s+" is not a valid option.");
	}

	void argumentRequired(String opt,String desc)
	{
		error(opt+" require as argument "+desc);
	}

	void invalidArgument(String opt,String desc)
	{
		error("invalid argument "+opt+" "+desc);
	}
	
	void processOptions(String argv[]) throws SIMException
	{
		int i;

		for (i = 0 ; i < argv.length ; i++)
		{
			String s = argv[i];

			if (s.charAt(0) == '-')
			{
				if (s.length() > 2)
					invalidOption(s);
				switch (s.charAt(1))
				{
					case	'l':
						if (++i >= argv.length)
							argumentRequired(s,"log config");
						jmce.util.Logger.setConfiguration(argv[i]);
						break;
						
					case	'g':
						if (++i >= argv.length)
							argumentRequired(s,"Class name");
						SampleTerminal.setDefaultCRT(argv[i]);
						break;

					case	't':
						if (++i >= argv.length)
							argumentRequired(s,"Monitor type");
						monitorType = Integer.parseInt(argv[i]);
						if (monitorType < 0 || monitorType > 1)
							invalidArgument(argv[i],"Must be 0 or 1");
						break;
						
					case	'p':
						if (++i >= argv.length)
							argumentRequired(s,"TCP Port");
						port = Integer.parseInt(argv[i]);
						if (port < 0)
							invalidArgument(argv[i],"Must >= 0");
						break;

					case	'd':
						debugger = true;
						if (monitor)
							error("-d is not compatible with -m");

						break;
						
					case	'm':
						SampleTerminal.setDefaultCRT("jmce.sim.terminal.SwingCRT");
						monitor = true;
						if (debugger)
							error("-m is not compatible with -d");
						break;
				}
			}
			else
				break;

		}

		if (i >= argv.length)
		{
			if (!monitor)
				error("No class name or xml specified");
		}
		else
				name = argv[i];

	}

	private boolean destroyed = false;
	
	public 	void exceptionEvent(ExceptionEvent ev)
	{
		if (destroyed)
			return;
		
		destroyed = true;
		
		cpu.stop();
		try
		{
			log.fine("Destroy root");
			cpu.destroy();
		}
		catch (Exception ignore)
		{
		}

		Logger.writeEx(ev.ex);
		System.exit(0);
	}

	
	public Jmce(String argv[])
	{
		instance = this;
	
		try
		{
			SampleTerminal.setDefaultCRT("jmce.sim.terminal.ttyCRT");

			processOptions(argv);

			// Load the cpu class
			if (name != null)
			{
				if (name.toUpperCase().endsWith(".XML"))
					setCPU(decode(name));
				else
				{
					@SuppressWarnings("rawtypes")
					Class clazz = Class.forName(name);
					Object o = clazz.newInstance();
					setCPU(o);
				}
					
			}

				
			// Start monitor if required
			if (monitor)
			{
				Monitor m = new Monitor();
				if (monitorType == 1)
				{
					TCPServer s = new TCPServer();
					s.setPort(port);
					m.addHardware(s);
				}
				addHardware(m);

				init(null);
				initSwing(null);
				reset();
				if (cpu != null)
					cpu.reset();

			}
			// Start emulator
			else
			{
				/** Start the debugger if enabled */
				if (debugger)
				{
					JDebug jd = new JDebug(cpu);
					jd.pack();
					jd.setVisible(true);
				}

				try
				{
					cpu.reset();


					/**
					 * If no one control the cpu
					 * start it.
					 */
					if (cpu.getExceptionListenerCount() == 0)
						cpu.addExceptionListener(this);

					if (!debugger)
						cpu.start();
					
					
				}
				catch (Exception ex)
				{
					exceptionEvent(new ExceptionEvent(ex));
					
				}
				
			}

					
		}
		catch (Throwable e)
		{
			System.err.println("\njmce error : "+e);
			Logger.writeEx(e);
		}
	}


	public void start()
	{
		
	}
	
	
	
	public void encode(String filename) throws SIMException
	{
		try
		{
			java.beans.XMLEncoder e =
			     new java.beans.XMLEncoder(new BufferedOutputStream(
				new FileOutputStream(filename)));
			e.setExceptionListener(new java.beans.ExceptionListener()
			{
				public void exceptionThrown(Exception e)
				{
					e.printStackTrace(System.out);
				}

			});

			e.writeObject(cpu);
			e.close();
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(filename,"XML encoding");
		}
		
	}

	public void addChild(Hardware parent,Object o) throws SIMException
	{
		Hardware h = (Hardware)o;
		parent.addHardware(h);
		
		h.init(parent);
		h.initSwing(parent);

	}
	
	public void setCPU(Object o) throws SIMException
	{
		cpu = (CPU)o;
		cpu.init(null);
		cpu.initSwing(null);

	}
	
	public Object decode(String filename) throws SIMException
	{
		try
		{
			java.beans.XMLDecoder d =
				new java.beans.XMLDecoder(new BufferedInputStream(
				new FileInputStream(filename)));	
			Object o = d.readObject();
			d.close();
			return o;
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(filename,"XML Decoding");
		}

	}



	static public  void showConfig(CPU cpu,jmce.sim.Console t) throws SIMException
	{
		int i,j;


		t.println(jmce.Jmce.version1);
		t.println(jmce.Jmce.version2);

		t.println("CPU "+cpu.getName()+" Clock="+(cpu.getClock()/1000000)+" Mhz");
		//t.println();
		for (i = 0 ; i < cpu.getMemoryCount() ; i++)
		{
			jmce.sim.Memory m = cpu.getMemoryAt(i);
			t.println("  Memory #"+i+". "+m.toString());

		}
		//t.println();

		DiskController fdc;
		
		for (i = 0 ; (fdc = (DiskController)cpu.getHardware(DiskController.class,i)) != null ; i++)
		{
			t.println("Disk controller # "+i+" "+fdc);
			Disk dsk;

			for (j = 0 ; j < fdc.getDiskCount() ; j++)
			{
				dsk = fdc.getDisk(j);
				if (!(dsk instanceof NullDisk))
				{
					t.println("  Disk #"+j+" "+(char)('A'+j)+"  "+dsk.toString());
				}
			}
						
		}

		Serial s ;
		
		t.println("Serials :");
		for (i = 0 ; (s = (Serial)cpu.getHardware(Serial.class,i)) != null; i++)
		{
			t.print("  Serial # "+i+" "+s.toString());

			Device<Integer> d1,d2;
			d1 = s;
			while ((d2 = d1.getConnected()) != null)
			{
				
				t.print(" ==> "+d2.toString());
				d1 = d2;
			}
			t.println();
				  
		}
		t.println("Other hardware :");
		
		for ( i = 0; i < cpu.getHardwareCount() ; i++)
		{
			Hardware h = cpu.getHardware(i);

			if (!(h instanceof Memory) &&
			    !(h instanceof DiskController) &&
			    !(h instanceof Serial))
			    t.println("  Hardware #"+i+" "+h.toString());
		}
			
	}

	public static void main(String argv[])
	{
		Jmce j = new Jmce(argv);
		j.start();
	}

	public String toString()
	{
		return "JMCE Ver. "+versionNumber;
	}
}

