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

import jmce.sim.Interrupt;
import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.util.FastArray;

/**
 * Interrupt sub system for MCS51 processor.
 * <p>
 * All interrupt in the 8051 are generaed setting one or more bit in a SFR so the interrupt are generate automatically
 * when the relative bit in the SFR memory is written and the interrupt is enabled.
 *
 * @author Mario Viara
 * @version 1.02
 */
public class Interrupt8051 extends Interrupt implements MCS51Constants {

	private FastArray<InterruptCondition> interruptConditions = new FastArray<InterruptCondition>();

	private MCS51 cpu;

	/**
	 * Interrupt condition generated on change in some SFR
	 *
	 * @author Mario Viara
	 * @version 1.00
	 */
	class InterruptCondition {

		private final int mask;

		private boolean risingFrontCondition;

		private boolean active;

		/**
		 * Check if the specified mask is active in the SFR
		 *
		 * @return true - If 1 or more bit in the mask are active
		 */
		public boolean isActive() {
			return active;
		}

		public void updateState(final int old, final int value) throws SIMException {
			int mVal = value & mask;

			if (risingFrontCondition) {
				int mOld = old & mask;
				if (mVal != mOld && mVal != 0) {
					active = true;
				}
			} else {
				active = mVal != 0;
			}
		}

		public void reset() {
			if (risingFrontCondition) {
				active = false;
			}
		}

		/**
		 * Constructor.
		 *
		 * @param sfr - Sfr to check
		 * @param mask - Mask of bit relative to the interrupt.
		 */

		public InterruptCondition(final int mask, final boolean risingFrontCondition) {
			this.mask = mask;
			this.risingFrontCondition = risingFrontCondition;
		}

	}

	/**
	 * 
	 *
	 */
	private class Interrupt8051Listener implements MemoryWriteListener {

		final InterruptCondition ic;

		Interrupt8051Listener(final InterruptCondition ic) {
			this.ic = ic;
		}

		@Override
		public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
			ic.updateState(oldValue, value);
			setActive(isInterruptActive());
		}

	}

	public Interrupt8051(MCS51 cpu, String name, int v) throws SIMException {
		super(cpu, name, v);
		setEnabled(true);
		this.cpu = cpu;
	}

	/**
	 * Check if the interrupt is active.
	 *
	 * @return true if the interrupt is active.
	 */
	private boolean isInterruptActive() throws SIMException {

		for (int i = interruptConditions.getSize(); --i >= 0;) {
			InterruptCondition ii = interruptConditions.get(i);
			if (!ii.isActive()) {
				return false;
			}
		}

		return true;
	}

	public void addInterruptCondition(int sfr, int mask) {
		InterruptCondition ic = new InterruptCondition(mask, false);
		interruptConditions.add(ic);
		cpu.addIOWriteListener(sfr, new Interrupt8051Listener(ic));
	}

	public void addInterruptRisingFrontCondition(int sfr, int mask) {
		InterruptCondition ic = new InterruptCondition(mask, true);
		interruptConditions.add(ic);
		cpu.addIOWriteListener(sfr, new Interrupt8051Listener(ic));
	}

	@Override
	public void endISR() throws SIMException {
		super.endISR();
		for (int i = interruptConditions.getSize(); --i >= 0;) {
			interruptConditions.get(i).reset();
		}
	}

}
