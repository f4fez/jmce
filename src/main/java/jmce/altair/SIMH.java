/**
   $Id: SIMH.java 510 2011-01-18 09:25:07Z mviara $

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

package jmce.altair;

import jmce.sim.*;

import jmce.util.Logger;
import jmce.util.Hex;
import jmce.util.Timeout;
import jmce.util.FastArray;
import jmce.sim.MemoryWriteListener;
import jmce.sim.MemoryReadListener;

/**
 * Implementation the SIMH I/O port.
 * <p>
 * This class implements partially the <a href="http://simh.trailing-edge.com"/>
 * SIMH</a> extension peripheral for Altar8800.
 * <p>
 * Implement the following SIMH commands :
 * <ul>
 *  <li>{@link #CMD_PRINT_TIME}</li>
 *  <li>{@link #CMD_START_TIMER}</li>
 *  <li>{@link #CMD_STOP_TIMER}</li>
 *  <li>{@link #CMD_VERSION}</li>
 *  <li>{@link #CMD_GET_BANK}</li>
 *  <li>{@link #CMD_SET_BANK}</li>
 *  <li>{@link #CMD_MMU_COMMON}</li>
 *  <li>{@link #CMD_SIMH_RESET}</li>
 *  <li>{@link #CMD_SLEEP}</li>
 * </ul>
 *
 * @author Mario Viara
 * @version 1.00
 */
public class SIMH extends AbstractPeripheral implements MemoryReadListener,MemoryWriteListener,SIMHConstants
{
	private static Logger log = Logger.getLogger(SIMH.class);
	private Timeout sysTime = new Timeout();
	private int lastCommand = CMD_NONE;
	private FastArray<Timeout> timers = new FastArray<Timeout>();
	private int count;
	
	private Memory m;
	private Console c;
	
	public SIMH()
	{
		setName("SIMH");
	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		m = (Memory)cpu.getHardware(Memory.class);
		c = (Console)cpu.getHardware(Console.class);
		
		cpu.addIOReadListener(SIMH_PORT,this);
		cpu.addIOWriteListener(SIMH_PORT,this);
	}

	private void print(Object o) throws SIMException
	{
		String s = String.valueOf(o);
		
		for (int i = 0 ; i < s.length() ; i++)
			c.write((int)s.charAt(i));
	}

	private void println() throws SIMException
	{
		print("\r\n");
	}

	private void println(Object o) throws SIMException
	{
		print(o);
		println();
	}
	
		
	public int readMemory(jmce.sim.Memory mm,int address,int value) throws SIMException
	{
		switch (lastCommand)
		{
			default:
				lastCommand = CMD_NONE;
				break;

			case	CMD_MMU_COMMON:
				if (++count > 1)
				{
					value = 0xc0;
					lastCommand = CMD_NONE;
				}
				else
					value = 0;
				break;
				
			case	CMD_VERSION:
				if (count >= SIMH_VERSION.length())
				{
					value = 0;
					lastCommand = CMD_NONE;
				}
				else
					value = SIMH_VERSION.charAt(count++);
				break;
				
			case	CMD_HAS_BANKED_MEMORY:
				value = 16;
				lastCommand = CMD_NONE;
				break;

			case	CMD_GET_BANK:
				value =  m.getBank();
				lastCommand = CMD_NONE;
				break;
		}

		log.fine("SIMH Read "+Hex.formatByte(address)+" = "+Hex.formatByte(value));
		return value;
	}

	private void newCommand(int value) throws SIMException
	{
		Timeout t;
		count = 0;
		
		switch (value)
		{

			default:
				log.info("SIMH Unknow CMD="+Hex.formatByte(value));
				break;


			case	CMD_SLEEP:
				idle();
				break;

			case	CMD_START_TIMER:
				t = new Timeout();
				timers.add(t);
				break;

			case	CMD_STOP_TIMER:
				if (timers.getSize() > 0)
				{
					t = timers.get(timers.getSize() -1);
					timers.remove(t);
					println("Timer # "+(timers.getSize()+1)+" = "+t.getElapsed()+" ms");
				}
				break;

			case	CMD_SIMH_RESET:
				resetSimh();
				break;

			case	CMD_PRINT_TIME:
				println();
				println("Current System time is "+sysTime.getElapsed()+" ms");
				break;

				/** Multiple byte command */
			case	CMD_MMU_COMMON:
			case	CMD_SET_BANK:
			case	CMD_VERSION:
			case	CMD_HAS_BANKED_MEMORY:
			case	CMD_GET_BANK:
				lastCommand = value;
				break;


		}
		
	}
	
	public void writeMemory(jmce.sim.Memory mm,int address,int value,int oldValue) throws SIMException
	{

		if (lastCommand == CMD_NONE)
		{
			newCommand(value);
			return;
		}

		switch (lastCommand)
		{

			case	CMD_SET_BANK:
				m.setBank(value);
				lastCommand = CMD_NONE;
				break;

				
		}

		log.fine("SIMH Write "+Hex.formatByte(address)+" = "+Hex.formatByte(value));

		
	}

	public void resetSimh()
	{
		sysTime.restart();
		timers.clear();
		lastCommand = CMD_NONE;
	}
	
	public String toString()
	{
		return "SIMH at 0FE "+SIMH_VERSION;
	}
}
