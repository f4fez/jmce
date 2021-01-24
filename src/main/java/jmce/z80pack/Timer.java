/**
   $Id: Timer.java 467 2010-12-15 07:46:18Z mviara $

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
package jmce.z80pack;

import jmce.sim.*;
import jmce.intel.i8080.INT;
import jmce.intel.i8080.I8080;

/**
 * This class implements a 10 ms interrupt timer.
 *
 * <p>
 * Generate one interrupt every 10 ms. The register <tt>TIMER_CTRL</tt>
 * control the timer if a value different from 0 is written the timer
 * is enabled otherwise the timer is disabled.
 * <p>
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Timer extends AbstractPeripheral implements
   Z80PackConstants,MemoryReadListener,MemoryWriteListener,ResetListener,
   jmce.util.TimerListener
	   
					
{
	private INT irq;
	private int timerMode = 0;
	private jmce.util.Timer timer;

	public Timer()
	{
		setName("Timer 10 ms");
	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		
		cpu.addIOReadListener(TIMER_CTRL,this);
		cpu.addIOWriteListener(TIMER_CTRL,this);

		cpu.addResetListener(this);

		irq = new INT((I8080)cpu,"Timer");
		irq.setAutoReset(true);
		timer = new jmce.util.Timer(10,true,this);

	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		return timerMode;
	}

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		timerMode = value;
		if (timerMode == 0)
		{
			timer.cancel();
		}
		else
		{
			if (timer.isRunning() == false)
				cpu.addTimerMs(timer);
		}
	}

	public void reset(CPU cpu) throws SIMException
	{
		timer.cancel();
	}

	public void timerExpired() throws SIMException
	{
		irq.setActive(true);
	}
}

   