package jmce.sim;

import java.util.ArrayList;
import java.util.List;

import jmce.sim.memory.MemoryBits;
import jmce.sim.memory.PlainMemory;

/**
 * Implementation of a shift register. i.e 74HC4094
 *
 */
public abstract class ShiftRegister extends MemoryBits {

	// private static Logger log = Logger.getLogger(ShiftRegister.class.getName());

	/**
	 * Value returned in hight impedance state.
	 */
	private final Integer highImpedanceValue;

	private boolean zState = false;
	
	private boolean strobe;

	int overflowMask;
	
	int storageRegister;

	private List<ShiftRegister> cascades = new ArrayList<ShiftRegister>();

	/**
	 * Default constructor.
	 * 
	 * @param width Wide of the register (8,16...) Up to 32.
	 * @throws SIMException
	 */
	public ShiftRegister(final int width, final Integer highImpedanceValue) throws SIMException {
		super(new PlainMemory("ShiftRegister", 1), 0, 0, width);
		overflowMask = (1 << (getWidth() - 1));
		this.highImpedanceValue = highImpedanceValue;
	}

	public ShiftRegister(final int width) throws SIMException {
		this(width, null);
	}

	public void pushRight(final boolean bit) throws SIMException {
		int overflow = (storageRegister & overflowMask);
		int val = (storageRegister << 1);
		if (bit) {
			val++;
		}
		storageRegister = val;
		
		if (strobe) {
			setBits(storageRegister);
		}
		cascade(overflow != 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmce.sim.memory.AbstractMemory#get(int)
	 */
	@Override
	public int getBits() throws SIMException {
		return zState & (highImpedanceValue != null) ? highImpedanceValue : getBits();
	}

	/**
	 * @return the highState
	 */
	public boolean isZState() {
		return zState;
	}

	/**
	 * @param highState the highState to set
	 */
	public void setZState(boolean zState) {
		this.zState = zState;
	}

	protected void cascade(final boolean bit) throws SIMException {
		for (ShiftRegister sr : cascades) {
			sr.pushRight(bit);
		}
	}

	public void addCascade(final ShiftRegister next) {
		if (cascades.contains(next)) {
			removeCascade(next);
		}
		cascades.add(next);
	}

	public void removeCascade(final ShiftRegister next) {
		cascades.remove(next);
	}
	
	protected void strobe(final boolean value) throws SIMException {
		strobe = value;
		if (strobe) {
			setBits(storageRegister);
		}
	}

}
