package jmce.philips;

import jmce.sim.SIMException;

/**
 * PCF2111C and PCF2112 LCD controller implementation.
 *
 */
public class PCF2111C extends PCF21xx {
	/**
	 * Has double bank.
	 */
	public final static boolean DOUBLE_BANK = true;
	
	/**
	 * Number of LCD segments per bank. 
	 */
	public final static int SEGMENTS_PER_BANK = 32;
	
	/**
	 * Default constructor.
	 * @throws SIMException 
	 */
	public PCF2111C() throws SIMException {
		super(DOUBLE_BANK, SEGMENTS_PER_BANK);
	}

}
