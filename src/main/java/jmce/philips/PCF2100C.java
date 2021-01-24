package jmce.philips;

import jmce.sim.SIMException;

/**
 * PCF2100C LCD controller implementation.
 *
 */
public class PCF2100C extends PCF21xx {
	/**
	 * Has double bank.
	 */
	public final static boolean DOUBLE_BANK = true;
	
	/**
	 * Number of LCD segments per bank. 
	 */
	public final static int SEGMENTS_PER_BANK = 20;
	
	/**
	 * Default constructor.
	 * @throws SIMException 
	 */
	public PCF2100C() throws SIMException {
		super(DOUBLE_BANK, SEGMENTS_PER_BANK);
	}

}
