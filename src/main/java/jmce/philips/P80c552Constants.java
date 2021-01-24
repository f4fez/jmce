
package jmce.philips;

import jmce.intel.mcs51.MCS51Constants;


public interface P80c552Constants extends MCS51Constants
{
	/** Port 4 data */
	static public final int P4	= 0xC0;

	/** Port 5 data */
	static public final int P5	= 0xC4;
	
	/** Timer 2 */
	static public final int TM2CON	= 0xEA;
	static public final int TM2IR	= 0xC8;
	static public final int TMH2	= 0xED;
	static public final int TML2	= 0xEC;
	
	/** ADC */
	static public final int ADCON = 0xC5;
	static public final int ADCH = 0xC6;
	
}

