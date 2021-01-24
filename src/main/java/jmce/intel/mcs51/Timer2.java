/**
   $Id: Timer2.java 810 2012-03-15 00:31:07Z mviara $

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
package jmce.intel.mcs51;

import jmce.sim.*;

/**
 *
 * Standard Intel 8052 Timer2.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Timer2 extends AbstractPeripheral implements CycleListener,MCS52Constants
{
	private Interrupt8051 irq;
	private MCS52 mcs52;
	private int t2 = 0;

	/**
	 * Standard constructor
	 */
	public Timer2()
	{
		setName("Timer2");
	}

	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		
		mcs52 = (MCS52)cpu;

		irq = new Interrupt8051(mcs52,"Timer2",0x2b)
		{
			@Override
			public void startISR() throws SIMException
			{
				mcs52.sfrReset(T2CON,T2CON_TF2);
				super.startISR();
			}

		};
		irq.addInterruptCondition(IEN0,IEN0_ET2);
		irq.addInterruptCondition(T2CON,T2CON_TF2);

		mcs52.addIOReadListener(TL2,new MemoryReadListener()
		{
			public int readMemory(Memory memory,int address,int value)
			{
				return t2 & 0xff;
			}
			
		});

		mcs52.addIOReadListener(TH2,new MemoryReadListener()
		{
			public int readMemory(Memory memory,int address,int value)
			{
				return (t2 >>> 8 ) & 0xff;
			}

		});


		mcs52.addIOWriteListener(TL2,new MemoryWriteListener()
		{
			public void	writeMemory(Memory memory,int address,int value,int oldValue)
			{
				t2 = (t2 & 0xff00) | value;
			}
			
		});

		mcs52.addIOWriteListener(TH2,new MemoryWriteListener()
		{
			public void	writeMemory(Memory memory,int address,int value,int oldValue)
			{
				t2 = (t2 & 0x00ff) | (value << 8);
			}

		});

		mcs52.addCycleListener(this);

	}

	public final void cycle(int n) throws SIMException
	{

		/** Do nothing if timer2 not running */
		if (!mcs52.sfrIsBit(T2CON,T2CON_TR2))
			return;


		t2 += n;

		
		if (t2 > 0xffff)
		{
			mcs52.sfrSetBit(T2CON,T2CON_TF2);

			/** Check for auto reload  */
			if (!mcs52.sfrIsBit(T2CON,T2CON_CP))
			{
				t2 = mcs52.sfr(RCAP2H) * 256 + mcs52.sfr(RCAP2L);
			}
		}

	}


}


