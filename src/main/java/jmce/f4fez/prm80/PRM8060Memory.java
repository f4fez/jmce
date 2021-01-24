package jmce.f4fez.prm80;

import java.util.ArrayList;

import jmce.sim.Memory;
import jmce.sim.SIMException;
import jmce.sim.memory.AbstractMemory;
import jmce.sim.memory.PlainMemory;
import jmce.util.Logger;

public class PRM8060Memory extends AbstractMemory {

	/** logger */
	private static Logger log = Logger.getLogger(PRM8060Memory.class.getName());

	private ArrayList<MemoryElement> elements;

	public PRM8060Memory() {
		super("XDATA");
		elements = new ArrayList<MemoryElement>();
		MemoryElement me = new MemoryElement(new PlainMemory("PRM80 main", 32768), 0, 32767, ElementAdressMapping.NO_MAP);
		elements.add(me);
	}

	public DirectBusRegister addDirectBusRegister(final int address, final int mask, final int value, final boolean inversed) {
		DirectBusRegister dbr = new DirectBusRegister(value, inversed);
		MemoryElement me = new MemoryElement(dbr, address, (address | mask), ElementAdressMapping.ALWAYS_ZERO);
		elements.add(me);
		return dbr;
	}

	@Override
	protected final Memory mapMemory(int a) {
		for (MemoryElement me : elements) {
			if (me.isInRange(a)) {
				return me.getMemory();
			}
		}
		log.severe("Can't find memory bank at address: " + Integer.toHexString(a));
		return null;
	}

	@Override
	protected int mapAddress(int a) {
		for (MemoryElement me : elements) {
			if (me.isInRange(a)) {
				switch (me.mapping) {
					case ALWAYS_ZERO:
						return 0;
					case NO_MAP:
						return a;
					case MAP:
						return a - me.getStartAdress();
				}
			}
		}
		return a;
	}

	@Override
	protected void set(int a, int v) {
		try {
			setMemory(a, v);
		} catch (SIMException e) {
			log.fatal(e);
		}
	}

	@Override
	protected int get(int a) {
		try {
			return getMemory(a);
		} catch (SIMException e) {
			log.fatal(e);
			return -1;
		}
	}

	public static class MemoryElement {

		private Memory memory;

		private int startAdress;

		private int endAdress;

		private ElementAdressMapping mapping;

		public MemoryElement(final Memory memory, final int startAdress, final int endAdress, final ElementAdressMapping mapping) {
			this.memory = memory;
			this.startAdress = startAdress;
			this.endAdress = endAdress;
			this.mapping = mapping;
		}

		public Memory getMemory() {
			return memory;
		}

		public boolean isInRange(final int address) {
			return startAdress <= address && endAdress >= address;
		}

		/**
		 * @return the startAdress
		 */
		public int getStartAdress() {
			return startAdress;
		}

		/**
		 * @return the endAdress
		 */
		public int getEndAdress() {
			return endAdress;
		}

	}

	/**
	 * Simple memory that store a single byte. Return this byte for any address. Read only, can only be set using the
	 * setDirectBusRegisterValue(final int value) method.
	 */
	public static class DirectBusRegister extends AbstractMemory {

		private int value = 0xff;

		private boolean inversed = true;

		public DirectBusRegister(final int value, final boolean inversed) {
			setSize(1);
			setReadOnly();
		}

		public synchronized void setDirectBusRegisterValue(final int value) {
			this.value = value;
		}

		@Override
		protected void set(int a, int v) {
			throw new UnsupportedOperationException("Can't set direct bus error");
		}

		@Override
		protected synchronized int get(int a) {
			return value;
		}

		public synchronized void pressedButton(int mask) throws SIMException {
			if (inversed) {
				value &= ~mask;
			} else {
				value |= mask;
			}
		}

		public synchronized void releasedButton(int mask) throws SIMException {
			if (inversed) {
				value |= mask;
			} else {
				value &= ~mask;
			}
		}
	}

	public static enum ElementAdressMapping {
		NO_MAP, MAP, ALWAYS_ZERO;
	}

}
