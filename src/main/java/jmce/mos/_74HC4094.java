package jmce.mos;

import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.sim.ShiftRegister;
import jmce.sim.memory.MemoryBit;

public class _74HC4094 extends ShiftRegister {

	/**
	 * Cp pin.
	 */
	private MemoryBit cp;

	/**
	 * DATA pin.
	 */
	private MemoryBit data;

	/**
	 * OE pin.
	 */
	private MemoryBit oe;

	/**
	 * Strobe pin.
	 */
	private MemoryBit str;

	/**
	 * Listener mapped to the clock pin.
	 */
	private MemoryWriteListener clockListener;

	/**
	 * Listener mapped to the oe pin.
	 */
	private MemoryWriteListener oeListener;
	
	/**
	 * Listener mapped to the str pin.
	 */
	private MemoryWriteListener strListener;

	private boolean oldOe;

	private boolean oldCp;
	
	private boolean oldStr;

	public _74HC4094(final Integer highImpedanceValue) throws SIMException {
		super(8, highImpedanceValue);
		clockListener = new MemoryWriteListener() {

			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				if (oldCp != cp.get()) {
					if (cp.get()) {
						pushRight(data.get());
					}
					oldCp = cp.get();
				}

			}

		};

		oeListener = new MemoryWriteListener() {

			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				if (oldOe != oe.get()) {
					setZState(!oe.get());
					oldOe = oe.get();
				}
			}

		};
		
		strListener = new MemoryWriteListener() {

			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				if (oldStr != str.get()) {
					strobe(str.get());
					oldStr = str.get();
				}
			}

		};
	}

	/**
	 * @return the cp
	 */
	public MemoryBit getCp() {
		return cp;
	}

	/**
	 * @param cp the cp to set
	 */
	public void setCp(MemoryBit cp) {
		if (this.cp != null) {
			this.cp.removeMemoryWriteListener(clockListener);
		}
		this.cp = cp;
		this.cp.addMemoryWriteListener(clockListener);
	}

	/**
	 * @return the data
	 */
	public MemoryBit getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(MemoryBit data) {
		this.data = data;
	}

	/**
	 * @return the oe
	 */
	public MemoryBit getOe() {
		return oe;
	}

	/**
	 * @param oe the oe to set
	 */
	public void setOe(MemoryBit oe) {
		if (this.oe != null) {
			this.oe.removeMemoryWriteListener(oeListener);
		}
		this.oe = oe;
		this.oe.addMemoryWriteListener(oeListener);
	}

	/**
	 * @return the str
	 */
	public MemoryBit getStr() {
		return str;
	}

	/**
	 * @param str the str to set
	 */
	public void setStr(MemoryBit str) {
		if (this.str != null) {
			this.str.removeMemoryWriteListener(strListener);
		}
		this.str = str;
		this.str.addMemoryWriteListener(strListener);
	}

}
