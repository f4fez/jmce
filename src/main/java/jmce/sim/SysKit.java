/**
   $Id: SysKit.java 469 2010-12-15 09:06:35Z mviara $

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
package jmce.sim;


import jmce.util.Logger;
import jmce.util.Hex;
import jmce.util.Timeout;
import jmce.util.FastArray;
import jmce.sim.ResetListener;
import jmce.sim.MemoryWriteListener;
import jmce.sim.MemoryReadListener;
import jmce.sim.terminal.*;

/**
 * JMCE System kit.
 * <p>
 * This peripheral permit one iteration from the emulated operating
 * system and the emulator implementing some useful function. 
 * <p>
 * Use only one Read/Write port that can be set using the method {@link
 * #port} .
 * <p>This sample Z80 program track the elapsed time of the function
 * <b>dowork</b> on the console :
 * <p>
 * <pre>
 * 
 *	ld	a,4		; Start new timer function
 *	out	(0f0h),a	;
 *	call	dowork		; Do the work ....
 *	ld	a,5		; Stop last timer
 *	put	(0f0h),a	; Write elapsed time on
 *				; the console (if any!)
 * </pre>
 * 
 * @author Mario Viara
 * @version 1.01
 *
 * 1.01	4 Oct 2010
 *	- Added command SYSKIT_CMD_DEBUG to call the monitor
 *	programmatically.
 *	
 */
public final class SysKit extends AbstractPeripheral implements
				   MemoryReadListener,MemoryWriteListener,ResetListener
{
	private static Logger log = Logger.getLogger(SysKit.class);
	
	/** Check SysKit when write this command the 2 successive read
	 *  will return 0x69 and then 0x96 this permit to check if the
	 *  syskit is installed
	 */
	static public final int SYSKIT_CMD_CHECK = 0x69;
	
	/** Get version this command return the version of the class */
	static public final int SYSKIT_CMD_GETVER = 1;

	/** Reset the internal status command */
	static public final int SYSKIT_CMD_RESKIT = 2;

	/** Release control of the cpu to the emulator */
	static public final int SYSKIT_CMD_DELAY = 3;

	/** Create a new millis timer and push on the timer stack */
	static public final int SYSKIT_CMD_TSTART = 4;

	/** Pop a timer from the timer stack and display on the console
	 * the elapsed time
	 */
	static public final int SYSKIT_CMD_TSTOP = 5;

	/** Print the version on the console */
	static public final int SYSKIT_CMD_PRTVER = 6;

	/** Enter in debug @since 1.01 */
	static public final int SYSKIT_CMD_DEBUG = 7;
	
	/** No command only for internal status */
	static public final int SYSKIT_CMD_NONE = 255;
	
	/** Current version */
	static public  final int SYSKIT_VERSION = 0x01;

	
	/** Default base register */
	protected  int port = 0xf0;


	private int count;
	private int cmd = SYSKIT_CMD_NONE;
	private FastArray<Timeout> timers = null;
	private Serial console = null;
	
	public SysKit()
	{
		setName("SysKit");
	}

	/**
	 * Return the current base port.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Set the new base port.
	 *
	 * @param port
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addIOReadListener(port,this);
		cpu.addIOWriteListener(port,this);
		cpu.addResetListener(this);

		Terminal t = (Terminal)cpu.getHardwareTree(Serial.class,Terminal.class);
		if (t != null)
			console = (Serial)t.getParent();
		if (console == null)
			log.info("No Terminal installed in "+cpu);
	}

	public void reset(CPU cpu) throws SIMException
	{
		reskit();
	}

	public int readMemory(jmce.sim.Memory m,int address,int v) throws SIMException
	{
		switch (cmd)
		{
			default:
				log.info("Unknown read command "+Hex.formatByte(cmd));
				cmd = SYSKIT_CMD_NONE;
				v = SYSKIT_CMD_NONE;
				break;
				
			case	SYSKIT_CMD_GETVER:
				v = SYSKIT_VERSION;
				cmd = SYSKIT_CMD_NONE;
				break;
				
			case	SYSKIT_CMD_CHECK:
				if (count == 0)
					v = 0x69;
				else
					v = 0x96;
				if (++count >= 2)
					cmd = SYSKIT_CMD_NONE;
				break;
				
		}
		return v;
	}

	private void newCommand(int v) throws SIMException
	{
		count = 0;
		Timeout t;
		
		switch (v)
		{
				
			default:
				log.info("Unknown new command "+Hex.formatByte(v));
				break;

				/** Enter in debug @since 1.01 */
			case	SYSKIT_CMD_DEBUG:
				cpu.setTill(cpu.pc());
				break;
				
				/** Stop timer */
			case	SYSKIT_CMD_TSTOP:
				if (timers.getSize() > 0)
				{
					t = timers.get(timers.getSize() - 1);
					timers.remove(timers.getSize() - 1);
					String s = "Timer # "+(timers.getSize() + 1) +" = "+t.getElapsed()+" millis elapsed";
					log.info(s);
					println(s);
					break;
					    
				}
				else
					log.info("Stop timer without start!");
				break;
				
				/* Start timer */
			case	SYSKIT_CMD_TSTART:
				t = new Timeout();
				timers.add(t);
				log.info("Start timer # "+timers.getSize());
				break;
				
				/** Command requiring an  answer */
			case	SYSKIT_CMD_GETVER:
			case	SYSKIT_CMD_CHECK:
				cmd = v;
				break;

				/** Reset SysKit */
			case	SYSKIT_CMD_RESKIT:
				reskit();
				break;

				/** Delay execution for a little */
			case	SYSKIT_CMD_DELAY:
				idle();
				break;

				/** Print the SysKit version */
			case	SYSKIT_CMD_PRTVER:
				println(toString());
				break;
						
		}
	}

	private void reskit()
	{
		cmd = SYSKIT_CMD_NONE;
		timers = new FastArray<Timeout>();
		
	}
	
	public void	writeMemory(jmce.sim.Memory m,int address,int v,int oldValue) throws SIMException
	{
		switch (cmd)
		{
				/* New command */
			case	SYSKIT_CMD_NONE:
				newCommand(v);
				break;
		}
	}

	public void print(Object o)  throws SIMException
	{
		String s = String.valueOf(o);

		for (int i = 0 ; i < s.length() ; i++)
			putchar(s.charAt(i));

	}

	public void println() throws SIMException
	{
		print("\r\n");
	}

	public void println(Object o) throws SIMException
	{
		String s = String.valueOf(o);

		print(s+"\r\n");
	}

	public void putchar(int c) throws SIMException
	{
		if (console != null)
			console.write(c);
	}
	

	public String toString()
	{
		return "SysKit Ver. "+SYSKIT_VERSION+" at "+Hex.formatWord(port);
	}
}


