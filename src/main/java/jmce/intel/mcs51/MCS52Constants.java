/**
 * $Id: MCS52Constants.java 372 2010-09-28 06:38:13Z mviara $
 */

package jmce.intel.mcs51;


/*
 * Constants for Intel MCS52.
 *
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.00
 */
public interface MCS52Constants extends MCS51Constants
{
	/** Timer 2 control register */
	static public final int T2CON		= 0xc8;

	/** Timer 2 overflow */
	static public final int T2CON_TF2	= 0x80;

	/** Timer 2 running */
	static public final int T2CON_TR2	= 0x04;
	
	/** If not set the timer is auto reloaded */
	static public final int T2CON_CP	= 0x01;
	
	/** Timer 2 MOD register */
	static public final int T2MOD		= 0xc9;
	
	static public final int T2MOD_DCEN	= 0x01;
	
	/** Timer 2 low byte counter */
	static public final int TL2		= 0xcc;
	
	/** Timer 2 high byte counter */
	static public final int TH2		= 0xcd;
	
	static public final int RCAP2H		= 0xcb;
	static public final int RCAP2L		= 0xca;
	
	/** Interrupt enable second register */
	static public final int IEN0		= 0xa8;

	/** Interrupt enable for timer 2 */
	static public final int IEN0_ET2	= 0x20;
	
	/** Interrupt enable third register */
	static public final int IEN1		= 0xb1;

}

