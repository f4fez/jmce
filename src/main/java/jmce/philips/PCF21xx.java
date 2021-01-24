package jmce.philips;

import jmce.sim.AbstractHardware;
import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.sim.memory.MemoryBit;
import jmce.sim.memory.PlainMemory;

/**
 * Abstract implementation of the PCF21xx chip family.
 *
 */
public abstract class PCF21xx extends AbstractHardware {
	
	/**
	 * Number of segments per bank.
	 */
	private final int segmentsPerBank;
	
	/**
	 * Output buffer. 
	 * Bank mapped to address.
	 */
	private final Memory registers;
	
	/**
	 * CLB pin.
	 */
	private MemoryBit clb;
	
	/**
	 * DATA pin.
	 */
	private MemoryBit data;
	
	/**
	 * DLEN pin.
	 */
	private MemoryBit dlen;
	
	/**
	 * Hold the previous clock pin state.
	 */
	private boolean oldClb;
	
	/**
	 * Hold the previous dlen pin state.
	 */
	private boolean oldDlen;
	
	/**
	 * Listener mapped to the clock pin.
	 */
	private MemoryWriteListener clockListener;
	
	/**
	 * Listener mapped to the dlen pin.
	 */
	private MemoryWriteListener dlenListener;
	
	/**
	 * Current state of the load pulse.
	 */
	private LoadPulseState loadPulseState = LoadPulseState.IDLE;
		
	/**
	 * Current decoded bit number.
	 */
	private int bitCount = 0;
	
	/**
	 * Used to store the data currently transfered.
	 */
	private boolean inputBuffer[];
	
	/**
	 * Default constructor.
	 * @param doubleBank true if has a double bank.
	 * @param segmentsPerBank Number of segments per bank.
	 * @throws SIMException 
	 */
	protected PCF21xx(final boolean doubleBank, final int segmentsPerBank) throws SIMException {
		this.segmentsPerBank = segmentsPerBank;
		registers = new PlainMemory("PCF21xx registers", doubleBank ? 8 : 16);
		for (int i = 0; i < (doubleBank ? 8 : 16); i++) {
			registers.setMemory(i,0);
		}
		inputBuffer = new boolean[segmentsPerBank+1];
		
		clockListener = new MemoryWriteListener() {
			
			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue)
					throws SIMException {
				boolean currentClb = clb.get();
				if (oldClb !=  currentClb) {
					if (dlen.get()) {
						// Chip selected, check if clock fall
						if(!currentClb) {
							clockFall();
						}
					}
					else {
						// Chip not selected, check loadPulse
						if (loadPulseState == LoadPulseState.WAIT && currentClb) {
							loadPulseState = LoadPulseState.HIGH;
						}
						if (loadPulseState == LoadPulseState.HIGH && !currentClb) {
							transferRegisters();
						}
					}
				}
				oldClb = currentClb;
			}
		};
		
		dlenListener = new MemoryWriteListener() {
			
			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue)
					throws SIMException {
				boolean currentDlen = dlen.get();
				if (oldDlen != currentDlen) {
					if(!currentDlen) {
						loadPulseState = LoadPulseState.WAIT;
					}
					else {
						loadPulseState = LoadPulseState.IDLE;
						bitCount = 0;
					}
				}				
				oldDlen = currentDlen;
			}
		};
	}

	/**
	 * Add a listener called when registers are updated.
	 * The address contain the bank. The value, the bit field.
	 *  
	 * @param l Listener
	 */
	public void addRegisterWriteListener(MemoryWriteListener l) {
		registers.addMemoryWriteListener(l);
	}

	/**
	 * @param l
	 */
	public void removeRegisterWriteListener(MemoryWriteListener l) {
		registers.removeMemoryWriteListener(l);
	}


	/**
	 * @return the clb
	 */
	public MemoryBit getClb() {
		return clb;
	}

	/**
	 * @param clb the clb to set
	 */
	public void setClb(MemoryBit clb) {
		if (this.clb != null) {
			this.clb.removeMemoryWriteListener(clockListener);
		}
		this.clb = clb;
		this.clb.addMemoryWriteListener(clockListener);
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
	 * @return the dlen
	 */
	public MemoryBit getDlen() {
		return dlen;
	}

	/**
	 * @param dlen the dlen to set
	 */
	public void setDlen(MemoryBit dlen) {
		if (this.dlen != null) {
			this.dlen.removeMemoryWriteListener(dlenListener);
		}
		this.dlen = dlen;
		this.dlen.addMemoryWriteListener(dlenListener);
	}
	
	private void clockFall() throws SIMException {
		bitCount++;
		if (bitCount == 1) {
			//Leading zero
			return;
		}
		if (bitCount > (segmentsPerBank+1)) {
			//Something wrong. Stop here.
			// Don't know how the real device manage that.
			return;
		}
		inputBuffer[bitCount-2] = data.get();
	}
	
	private void transferRegisters() throws SIMException {
		int val = 0;
		for(int i = 0; i < segmentsPerBank; i++) {
			if (inputBuffer[i]) {
				val |= (1 << i); 
			}
		}
		registers.setMemory(inputBuffer[segmentsPerBank] ? 0 : 4, val & 0xff);
		registers.setMemory(inputBuffer[segmentsPerBank] ? 1 : 5, (val >> 8) & 0xff);
		registers.setMemory(inputBuffer[segmentsPerBank] ? 2 : 6, (val >> 16) & 0xff);
		registers.setMemory(inputBuffer[segmentsPerBank] ? 3 : 7, (val >> 24) & 0xff);
		loadPulseState = LoadPulseState.IDLE;
	}
	
	private enum LoadPulseState {
		IDLE, WAIT, HIGH
	}
}

