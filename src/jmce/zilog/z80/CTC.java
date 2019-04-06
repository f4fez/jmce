/**
   $Id: CTC.java 810 2012-03-15 00:31:07Z mviara $

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
package jmce.zilog.z80;

import jmce.sim.*;
import jmce.util.Logger;
import jmce.util.Hex;

/**
 *  Z80 CTC
 * <p>
 * The Z80 CTC have 4 different channel that can operate as counter or
 * timer. 
 *<p>
 * Each channel have a control register :
 * <pre>
 * BIT 7 1 Enable interrupt
 * BIT 6 0 Timer mode / 1 Counter mode
 * BIT 5 0 Prescaler 16 / 1 Prescaler 256 (Only in timer mode)
 * BIT 4 0 Count failing edge / 1 Count rising edge 
 * BIT 3 0 Counter automatic trigger / 1 External trigger (ignored)
 * BIT 2 1 Time constants follow
 * BIT 1 1 Software reset.
 * BIT 0 1 Control word / 0 Interrupt vector.
 * <pre>
 * The 4 control register are located at base + 0 , + 1 , + 2 , +3.
 * <p>
 * <h2>Properties :</h2>
 * <p>
 * base - Base address of the peripheral (default to 0)<p>
 * <p>
 * @author Mario Viara
 * @version 1.01
 * 
 * @since 1.01
 */ 
public class CTC extends AbstractPeripheral implements ResetListener,MemoryReadListener,MemoryWriteListener,CycleListener
{
	private static Logger log = Logger.getLogger(CTC.class);
	static public final int CTC_CHANNEL = 4;

	/** Enable interrupt */
	static public final int CW_IE = 0x80;

	/** Interrupt vector */
	static public final int CW_VECTOR	= 0x01;

	/** Software reset */
	static public final int CW_RESET	= 0x02;
	
	private int base = 0;
	private INTZ80 irq[] = new INTZ80[CTC_CHANNEL];
	private Z80 z80;
	private int counter[] = new int[CTC_CHANNEL];
	private int time[] = new int[CTC_CHANNEL];
	private int prescaler[] = new int[CTC_CHANNEL];
	private int cw[] = new int[CTC_CHANNEL];
	private int clock[] = new int[CTC_CHANNEL];
	private boolean waitTime[] = new boolean[CTC_CHANNEL];

	/**
	 * Default constructor
	 */
	public CTC()
	{
		super("CTC");
	}

	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		this.z80 = (Z80)cpu;

		z80.addResetListener(this);
		z80.addCycleListener(this);

		for (int i = 0 ; i < CTC_CHANNEL ;i ++)
		{
			irq[i] = new INTZ80(z80,"CTC"+i);
			irq[i].setAutoReset(true);
			z80.addIOWriteListener(base+i,this);
			z80.addIOReadListener(base+i,this);
		}

	}

	public void reset(CPU cpu) throws SIMException
	{
		for (int i = 0 ; i < CTC_CHANNEL ; i++)
			reset(i);
	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		int ch = address - base;

		value = counter[ch];

		log.fine("CTC RD="+Hex.formatByte(value)+" AT "+ch+" = "+value);

		return value;
	}

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		int ch = address - base;

		log.fine("CTC WR="+Hex.formatByte(value)+" AT "+ch);

		/** Check if a time costants must be loaded */
		if (waitTime[ch])
		{
			waitTime[ch] = false;
			counter[ch] = time[ch] = value;
			return;
		}

		/** Check if it is an interrupt vector */
		if ((value & CW_VECTOR) == 0)
		{
			log.fine("Vector="+(value & 0xf8));
			for (int i = 0 ; i < 4 ; i++)
			{
				irq[i].setVector(value & 0xF8 | (i << 1));
			}

			return;
		}


		/** Program a new control word */
		cw[ch] = value;

		/** Reset ? */
		if ((value & CW_RESET) != 0)
			reset(ch);

		/** Prescaler */
		if ((value & 0x40) != 0)
			prescaler[ch] = 1;
		else
		{
			if ((value & 0x20) == 0)
				prescaler[ch] = 16;
			else
				prescaler[ch] = 256;
		}

		/** Time follow ? */
		if ((value & 0x04) != 0)
			waitTime[ch] = true;
	}

	/**
	 * Process a number of clock for timer or counter
	 */
	private final void clock(int i,int n) throws SIMException
	{
		if (waitTime[i])
			return;

		clock[i] += n;

		while (clock[i] > prescaler[i])
		{

			clock[i] -= prescaler[i];
			counter[i] = (counter[i] - 1) & 0xff;
			if (counter[i] == 0)
			{
				if ((cw[i] & 0x80) != 0)
				{
					log.fine("CH="+i+" IRQ "+irq[i].toString());
					irq[i].setActive(true);
				}
				counter[i] = time[i];
			}
		}
		
	}

	/**
	 * Called by external peripheral to count pulse.
	 */
	public final void count(int i,int n) throws SIMException
	{
		/** Ignored in timer mode */
		if ((cw[i] & 0x40) == 0)
			return;
		clock(i,n);
	}
	
	/**
	 * Called at every instruction cycle to decrement the timer.
	 */
	public final void cycle(int n)  throws SIMException
	{
		for (int i = 0 ; i < 4 ; i++)
		{
			/** Timer mode, no external trigger and not reset */
			if ((cw[i] & 0x48) == 0)
			{
				clock(i,n);
			}
		}

	}


	/**
	 * Reset a CTC channel.<p>
	 *
	 * Called at CPU reset or when the software reset bit is set.
	 */
	public void reset(int ch)
	{
		counter[ch] = time[ch] = cw[ch] = 0;
		prescaler[ch] = 16;
		waitTime[ch] = false;
		clock[ch] = 0;
	}

	/**
	 * Set the base addres of this CTC
	 */
	public void setBase(int base)
	{
		this.base = base;
	}

	/**
	 * Return the base address.
	 */
	public int getBase()
	{
		return base;
	}

	public String toString()
	{
		return "Z80 CTC AT "+Hex.formatByte(base);
	}
}
