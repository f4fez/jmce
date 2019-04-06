/**
   $Id: Interrupt8051.java 691 2011-09-02 07:57:21Z mviara $

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
import jmce.util.FastArray;

/**
 * Interrupt sub system for MCS51 processor.
 * <p>
 * All interrupt in the 8051 are generaed setting one or more bit in a
 * SFR so the interrupt are generate automatically when the relative bit in
 * the SFR memory is written and the interrupt is enabled.
 *
 * @author Mario Viara
 * @version 1.02
 */
public class Interrupt8051 extends Interrupt implements MemoryWriteListener,MCS51Constants
{
	private FastArray<InterruptCondition> interruptConditions = new FastArray<InterruptCondition>();
	private MCS51 cpu;
	
	/**
	 * Interrupt condition generated on change in some SFR
	 *
	 * @author Mario Viara
	 * @version 1.00
	 */
	class InterruptCondition
	{
		private int sfr,mask;
		

		/**
		 * Check if the specified mask is active in the SFR
		 *
		 * @return true - If 1 or more bit in the mask are
		 * active
		 */
		public boolean isActive() throws SIMException
		{
			if ((cpu.getIOByte(sfr) & mask) != 0)
			{
				return true;
			}
			return false;
		}

		/**
		 * Constructor.
		 *
		 * @param sfr - Sfr to check
		 * @param mask - Mask of bit relative to the interrupt.
		 */
		public InterruptCondition(int sfr,int mask)
		{
			this.sfr = sfr;
			this.mask = mask;
		}
	}

	public Interrupt8051(MCS51 cpu,String name,int v) throws SIMException
	{
		super(cpu,name,v);
		setEnabled(true);
		this.cpu = cpu;
	}



	/**
	 * Check if the interrupt is active.
	 *
	 * @return true if the interrupt is active.
	 */
	private boolean isInterruptActive() throws SIMException
	{
		
		for (int  i =  interruptConditions.getSize() ; --i >= 0;)
		{
			InterruptCondition ii = interruptConditions.get(i);
			if (!ii.isActive())
			{
				return false;
			}
		}

		return true;
	}

	public void addInterruptCondition(int sfr,int mask)
	{
		interruptConditions.add(new InterruptCondition(sfr,mask));
		cpu.addIOWriteListener(sfr,this);
	}

	public void writeMemory(Memory m,int address,int value,int oldValue) throws SIMException
	{
		setActive(isInterruptActive());
	}
	
}

