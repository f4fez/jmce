/**
   $Id: Speaker.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.viara.s400;

import jmce.sim.*;

/**
 * Buzzer of S400.
 * <p>
 * The buzzer is connected to P3.4 but it is moved using the Timer0 so
 * the emulator only check the timer 0 configuration.
 *
 * @author Mario Viara
 * @version 1.00
 */
class Speaker extends jmce.sim.audio.Speaker implements MemoryWriteListener
{
	private int div = 0;

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		S400 s4 = (S400)cpu;
		s4.addIOWriteListener(S400.TCON,this);
		s4.addIOWriteListener(S400.TL0,this);
		s4.addIOWriteListener(S400.TH0,this);
	}

	public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		switch (address)
		{
			case	S400.TCON:
				if ((value & 0x10) == 0)
					setSpeaker(false);
				else
					setSpeaker(true);
				break;
			case	S400.TL0:
				div = (div & 0xff00) | value;
				break;
			case	S400.TH0:
				div = (div & 0xff) | (value << 8);
				div = 0x10000 - div;
				
				if (div > 0)
					setFreq(500000/div);
				else
					setFreq(0);
				break;
		}

	}
}
