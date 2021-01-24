/**
   $Id: Timer.java 461 2010-12-14 13:44:05Z mviara $

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
package jmce.sinclair.spectrum;

import jmce.sim.*;
import jmce.intel.i8080.INT;
import jmce.intel.i8080.I8080;

/**
 * This class implements a 20 ms interrupt timer.
 *
 * <p>
 * Generate one interrupt every 20 ms. 
 * <p>
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Timer extends AbstractPeripheral implements jmce.util.TimerListener
{
	private INT irq;
	private jmce.util.Timer timer;

	public Timer()
	{
		setName("Timer 20 ms");
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		irq = new INT((I8080)cpu,"Timer");
		
		/**
		 * Set auto end of interrupt because spectrum rom do
		 * not use IE RETI but IE RET
		 */
		irq.setAutoReset(true);
		
		timer = new jmce.util.Timer(20,true,this);
		cpu.addTimerMs(timer);

	}


	public void timerExpired() throws SIMException
	{
		irq.setActive(true);
	}
}

