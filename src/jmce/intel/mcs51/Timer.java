/**
   $Id: Timer.java 694 2011-09-02 12:01:08Z mviara $

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
 * Standard Timer0/Timer1  for 8051 family. <p>
 *
 * For each timer only MODE 0,MODE 1 and MODE 2 are supported.
 *
 * @author Mario Viara
 * @version 1.00
 *
 */
public class Timer extends AbstractPeripheral implements CycleListener
{
	private MCS51 mcs51;
	
	/**
	 * Implementation of single timer.
	 * <p>
	 * Support Timer mode 0,1,2
	 * 
	 * @author Mario Viara
	 * @version 1.00
	 */
	class SingleTimer implements MCS51Constants
	{
		private int tconShift;
		private int tmodShift;

		/** For performance reason a copy of the timer counter
		 *  THx and TLx are stored in the class and not in the
		 *  SFR Memory
		 */
		private int TH,TL;
		
		private int TF;
		private int ET;
		private int timer;
		private int tcon;
		private int tmod;
		private Interrupt8051 irq;
		
		SingleTimer(int timer)
		{
			tconShift = 2 * timer;
			tmodShift = 4 * timer;
			TH = MCS51Constants.TH0 + timer;
			TL = MCS51Constants.TL0 + timer;
			TF = TCON_TF0 << tconShift;
			ET = timer == 0 ? IE_ET0 : IE_ET1;
			this.timer = timer;

		}


		void registerCPU(MCS51 cpu) throws SIMException
		{
			irq = new Interrupt8051(cpu,"Timer"+timer,0x0b+timer * 0x10) 
			{
				public void startISR() throws SIMException
				{
					mcs51.sfrReset(TCON,TF);
					super.startISR();
				}
			};
			
			irq.addInterruptCondition(IE,ET);
			irq.addInterruptCondition(TCON,TF);
			
			cpu.addIOWriteListener(TCON,new MemoryWriteListener()
			{
				public void writeMemory(Memory m,int r,int v,int oldValue)
				{
					tcon = v >> tconShift;

				}
			});

			cpu.addIOWriteListener(TMOD,new MemoryWriteListener()
			{
				public void writeMemory(Memory m,int r,int v,int oldValue)
				{
					tmod = v >> tmodShift;

				}
			});

			cpu.addIOReadListener(TH0+timer,new MemoryReadListener()
			{
				public int readMemory(Memory m,int r,int v)
				{
					return TH;
				}
			});

			cpu.addIOWriteListener(TH0+timer,new MemoryWriteListener()
			{
				public void writeMemory(Memory m,int r,int v,int oldValue)
				{
					TH = v;
				}
			});

			cpu.addIOReadListener(TL0+timer,new MemoryReadListener()
			{
				public int readMemory(Memory m,int r,int v)
				{
					return TL;
				}
			});

			cpu.addIOWriteListener(MCS51Constants.TL0+timer,new MemoryWriteListener()
			{
				public void writeMemory(Memory m,int r,int v,int oldValue)
				{
					TL = v;
				}
			});

		}

		/**
		 * Called at every istruction cycle
		 */
		final void cycle(int n)  throws SIMException
		{
			int tl;

			// Do nothing if timer is not running
			if ((tcon & TCON_TR0) == 0)
				return;


			switch (tmod & (TMOD_T0_M0 | TMOD_T0_M1))
			{
				case	0:	// 13 bit timer
					tl = TL + n;
					TL = tl & 0x1f;

					if (tl > 0x1f)
					{
						TH = (TH + 1) & 0xff;
						if (TH == 0)
						{
							mcs51.sfrSet(TCON,TF);
							//System.out.println("Timer"+timer+" TF13");
						}
					}

					break;

					// Mode 1
				case	TMOD_T0_M0:
					tl = TL + n;
					TL= tl & 0xff;
					if (tl > 255)
					{

						TH = (TH + 1) & 0xff;
						if (TH == 0)
						{
							mcs51.sfrSet(TCON,TF);
						}
					}
					break;

					// Mode 2
				case	TMOD_T0_M1:
					tl = TL;

					while (n-- > 0)
					{
						tl = (tl + 1) & 0xff;

						if (tl == 0)
						{
							tl = TL = TH;
							mcs51.sfrSet(TCON,TF);
						}

					}
					TL = tl;
					break;

			}

		}





	}

	private SingleTimer timer0 = new SingleTimer(0);
	private SingleTimer timer1 = new SingleTimer(1);
	
	/**
	 * Default constructor
	 */
	public Timer()
	{
		setName("Timer0/1");
	}


	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addCycleListener(this);

		this.mcs51 = (MCS51)cpu;
		timer0.registerCPU(mcs51);
		timer1.registerCPU(mcs51);
		
	}


	public final void cycle(int n) throws SIMException
	{
		timer0.cycle(n);
		timer1.cycle(n);
	}
}
