/**
   $Id: Serial.java 510 2011-01-18 09:25:07Z mviara $

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
 * Standard 8051 serial interface.
 * <p>
 *  All function to send receive data are supported in polling and
 * interrupt mode.
 * The timing of the uart are not simulated and all character sent or
 * received are  processed as soon as possible.
 * <p>
 * The serial interface extends the <tt>jmce.sim.serial</tt> class
 * an order to connect the uart to  another CPU or to one terminal.
 *
 * @author Mario Viara
 * @version 1.01
 *
 * @see jmce.sim.Serial
 */
public class Serial extends jmce.sim.Serial implements MemoryReadListener,MemoryWriteListener,MCS51Constants
{
	private int sbuf,scon;
	private boolean sbufSent = true;
	private Interrupt8051 irq;

	/**
	 * Default constructor
	 */
	public Serial()
	{
		super("Serial");
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addIOWriteListener(SBUF,this);
		cpu.addIOReadListener(SBUF,this);
		cpu.addIOWriteListener(SCON,this);

		irq = new Interrupt8051((MCS51)cpu,"Serial",0x0023);

		// Enable interrupt on SIO
		irq.addInterruptCondition(IE,IE_ES);

		// RX or TX interrupt
		irq.addInterruptCondition(SCON,SCON_RI|SCON_TI);
	}

	@Override
	public void reset() throws SIMException
	{ 
		super.reset();
		sbuf = '?';
		scon = 0;
		sbufSent = true;
	}

	@Override
	public void writeInput(Integer c) throws SIMException
	{
		super.writeInput(c);
		feedCpu();
	}

	/**
	 * Feed the CPU is new data area available
	 */
	private void feedCpu() throws SIMException
	{
		if (!sbufSent || (scon & SCON_RI) != 0)
		{
			return;
		}
		
		if (readyRead())
		{
			sbuf = read();
			sbufSent = false;
			cpu.setIOByte(SCON,cpu.getIOByte(SCON)|SCON_RI);
		}
	}
	


	public int readMemory(Memory m,int a,int v) throws SIMException
	{
		switch (a)
		{
			case	SBUF:
				v = sbuf;
				sbufSent = true;
				feedCpu();
				return v;
		}
		
		return 0;
	}



	public void writeMemory(Memory m,int a,int v,int oldValue) throws SIMException
	{
		switch (a)
		{
			case	SCON:
				scon = v;
				feedCpu();
				break;
				
			case	SBUF:
				write(v);
				cpu.setIOByte(SCON,cpu.getIOByte(SCON)|SCON_TI);
				break;
		}
	}
}
