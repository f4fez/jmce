/**
   $Id: AbstractMemory.java 625 2011-06-07 06:52:42Z mviara $

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

package jmce.sim.memory;

import jmce.sim.AbstractPeripheral;
import jmce.sim.CPU;
import jmce.sim.Memory;
import jmce.sim.MemoryException;
import jmce.sim.MemoryReadListener;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.util.FastArray;
import jmce.util.Hex;
import jmce.util.Logger;

/**
 * Abstract implementation of Memory.
 * <p>
 *
 * All method not relative to set/get memory operation are implemented from this class. Also implements helper to make
 * simple add virtual memory features this make the class a little bit more complex but do not change the performance on
 * critical operation. All subclass must remember to call the method setSize() to allocate space for listener and names.
 *
 * @author Mario Viara
 * @version 1.02
 */
public abstract class AbstractMemory extends AbstractPeripheral implements Memory {

	private static Logger log = Logger.getLogger(AbstractMemory.class.getName());

	/** Array with all virtual memory */
	protected FastArray<AbstractMemory> vms = new FastArray<AbstractMemory>();

	/** Local read only flag */
	private boolean rol[];

	/** Array with global MemoryWriteListener */
	private FastArray<MemoryWriteListener> mwl = new FastArray<MemoryWriteListener>();

	/** Array with global MemoryReadListener */
	private FastArray<MemoryReadListener> mrl = new FastArray<MemoryReadListener>();

	/** Array with specific MemoryWriteListener */
	private FastArray<MemoryWriteListener>[] mwls;

	/** Array with specific MemoryReadListener */
	private FastArray<MemoryReadListener>[] mrls;

	// Array with memory location name
	private String[] names = new String[0];

	/** Size of this memory */
	protected int size;

	/** Name lenght to format address in hex */
	private int nameLength = 4;

	public AbstractMemory() {
		this(CPU.MAIN_MEMORY);
	}

	public AbstractMemory(String name) {
		super(name);
	}

	/**
	 * Set memory.
	 * <p>
	 * Used by subclass to implement the phisical memory.
	 */
	abstract protected void set(int a, int v);

	/**
	 * Get memory.
	 * <p>
	 * Used by subclass to implement the phisical memory.
	 */
	abstract protected int get(int a);

	public final boolean getReadOnly(int add) {
		return rol[add];
	}

	public final void setReadOnly() {
		setReadOnly(0, getSize());
	}

	public final void setReadOnly(int a, int len) {
		Memory mm = mapMemory(a);
		a = mapAddress(a);

		if (mm != this) {
			mm.setReadOnly(a, len);
			return;
		}

		for (int i = 0; i < len; i++)
			rol[a + i] = true;
	}

	public final void setReadOnly(int add) {
		setReadOnly(add, 1);
	}

	public final void addMemoryWriteListener(MemoryWriteListener l) {
		mwl.add(l);
	}

	public final void removeMemoryWriteListener(MemoryWriteListener l) {
		mwl.remove(l);
	}

	public final void addMemoryWriteListener(int a, MemoryWriteListener l) {
		Memory mm = mapMemory(a);
		a = mapAddress(a);

		if (mm != this) {
			mm.addMemoryWriteListener(a, l);
			return;
		}

		FastArray<MemoryWriteListener> m = mwls[a];

		if (m == null) {
			m = new FastArray<MemoryWriteListener>();
			mwls[a] = m;
		}

		m.add(l);

	}

	public void removeMemoryWriteListener(int a, MemoryWriteListener l) {

		FastArray<MemoryWriteListener> m = mwls[a];

		m.remove(l);
	}

	public void addMemoryReadListener(MemoryReadListener l) {
		mrl.add(l);
	}

	public void removeMemoryReadListener(MemoryReadListener l) {
		mrl.remove(l);
	}

	public void removeMemoryReadListener(int a, MemoryReadListener l) {
		FastArray<MemoryReadListener> m = mrls[a];

		m.remove(l);
	}

	public void addMemoryReadListener(int a, MemoryReadListener l) {
		Memory mm = mapMemory(a);
		a = mapAddress(a);
		if (mm != this) {
			mm.addMemoryReadListener(a, l);
			return;
		}

		FastArray<MemoryReadListener> m = mrls[a];
		if (m == null) {
			m = new FastArray<MemoryReadListener>();
			mrls[a] = m;
		}

		m.add(l);

	}

	public int getMemoryWriteListenerCount() {
		return mwl.getSize();
	}

	public int getMemoryWriteListenerCount(int a) {
		FastArray<MemoryWriteListener> m = mwls[a];

		return m == null ? 0 : m.getSize();
	}

	public int getMemoryReadListenerCount() {
		return mrl.getSize();
	}

	public int getMemoryReadListenerCount(int a) {
		FastArray<MemoryReadListener> m = mrls[a];

		return m == null ? 0 : m.getSize();
	}

	public MemoryWriteListener getMemoryWriteListenerAt(int i) {
		return mwl.get(i);
	}

	public MemoryReadListener getMemoryReadListenerAt(int i) {
		return mrl.get(i);
	}

	public MemoryWriteListener getMemoryWriteListenerAt(int a, int i) {
		FastArray<MemoryWriteListener> m = mwls[a];

		return m.get(i);

	}

	public MemoryReadListener getMemoryReadListenerAt(int a, int i) {
		FastArray<MemoryReadListener> m = mrls[a];

		return m.get(i);

	}

	@SuppressWarnings("unchecked")
	public void setSize(int newSize) {
		this.size = newSize;

		FastArray<MemoryWriteListener> w = new FastArray<MemoryWriteListener>();
		FastArray<MemoryReadListener> r = new FastArray<MemoryReadListener>();

		mwls = (FastArray<MemoryWriteListener>[]) java.lang.reflect.Array.newInstance(w.getClass(), newSize);
		mrls = (FastArray<MemoryReadListener>[]) java.lang.reflect.Array.newInstance(r.getClass(), newSize);
		rol = new boolean[newSize];
		names = new String[newSize];

		if (size > 0x1000000)
			nameLength = 8;
		else if (size > 0x100000)
			nameLength = 7;
		else if (size > 0x10000)
			nameLength = 6;
		else if (size > 0x10000)
			nameLength = 5;
		else if (size > 0x100)
			nameLength = 4;
		else
			nameLength = 2;

	}

	public int getSize() {
		return size;
	}

	public final void setMemoryName(int a, String name) {
		Memory m = mapMemory(a);
		a = mapAddress(a);

		if (m != this) {
			m.setMemoryName(a, name);
			return;
		}

		names[a] = name;
	}

	public final String getMemoryName(int a) {
		Memory m = mapMemory(a);
		a = mapAddress(a);

		if (m != this)
			return m.getMemoryName(a);

		if (names[a] == null)
			names[a] = Hex.formatValue(a, nameLength);
		return names[a];

	}

	/**
	 * Add a new memory and also add to the hardware list.
	 *
	 * @param m - Memory to add.
	 *
	 * @since 1.01
	 */
	public void addHardwareMemory(Memory m) {
		addMemory(m);
		addHardware(m);
	}

	/**
	 * Add virtual memory to the actual list.
	 */
	public void addMemory(Memory v) {
		vms.add((AbstractMemory) v);

	}

	/** Return the number of virtual memory */
	protected int getMemoryCount() {
		return vms.getSize();
	}

	/** Return the specified memory */
	protected AbstractMemory getMemoryAt(int i) {
		return vms.get(i);
	}

	public boolean isBit(int a, int mask) throws MemoryException {
		return (get(a) & mask) != 0;
	}

	public void setBit(int a, int mask) throws SIMException {
		int v = get(a) | mask;
		setMemory(a, v);
	}

	public void clrBit(int a, int mask) throws SIMException {
		int v = get(a) & ~mask;
		setMemory(a, v);
	}

	public final int getMemory(int add) throws SIMException {
		int i, v, a;

		Memory mm = mapMemory(add);
		a = mapAddress(add);

		if (mm != this) {
			v = mm.getMemory(a);

			/** Apply global read listener */
			for (i = mrl.getSize(); --i >= 0;)
				v = mrl.get(i).readMemory(this, add, v);

			return v;
		}

		v = get(a);

		for (i = mrl.getSize(); --i >= 0;)
			v = mrl.get(i).readMemory(this, a, v);

		FastArray<MemoryReadListener> m = mrls[a];

		if (m != null) {
			for (i = m.getSize(); --i >= 0;)
				v = m.get(i).readMemory(this, a, v);
		}

		return v;

	}

	public final void setMemory(int add, int v) throws SIMException {
		int i, a;

		Memory mm = mapMemory(add);
		a = mapAddress(add);

		if (mm != this) {
			/** Apply global memory write listener */
			for (i = mwl.getSize(); --i >= 0;)
				mwl.get(i).writeMemory(this, add, v, -1);
			mm.setMemory(a, v);
			return;
		}

		v &= 0xff;
		int oldValue = get(a);

		/**
		 * If the memory is read only and the cpu is running do not change the memory but call the write listeners.
		 */
		if (rol[a] && cpu != null && cpu.isRunning()) {
			if (v != oldValue) {
				log.fine(getName() + " write ignored at " + getMemoryName(a) + " from " + Hex.formatByte(oldValue) + " to " + Hex.formatByte(v));
			}
		} else
			set(a, v);

		/** Apply global memory write listener */
		for (i = mwl.getSize(); --i >= 0;)
			mwl.get(i).writeMemory(this, a, v, oldValue);

		FastArray<MemoryWriteListener> m = mwls[a];

		if (m != null) {
			for (i = m.getSize(); --i >= 0;)
				m.get(i).writeMemory(this, a, v, oldValue);
		}

	}

	/**
	 * Map memory for virtual memory.
	 * <p>
	 * This method must be overriden from virtual memory sub class.
	 */
	protected Memory mapMemory(int a) {
		return this;
	}

	/**
	 * Map address for virtual memory.
	 * <p>
	 * This method must be overridden from virtual memory sub class.
	 */
	protected int mapAddress(int a) {
		return a;
	}

	public String toString() {
		int size = getSize();
		if (size < 1024)
			return getName() + " " + size + " bytes";
		else
			return getName() + " " + (size / 1024) + " KB";
	}

}
