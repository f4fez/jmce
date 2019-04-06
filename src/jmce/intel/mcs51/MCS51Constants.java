/**
   $Id: MCS51Constants.java 691 2011-09-02 07:57:21Z mviara $

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

/**
 * Constants for Intel MCS51.
 * <p>
 *
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public interface MCS51Constants
{
	/** Accumulator */
	static public final int ACC	=	0xe0;
	
	/** Register B */
	static public final int B	=	0xF0;

	/** Program status word */
	static public final int PSW	=	0xd0;
	
	/** Carry flag */
	static public final int PSW_CY	=	0x80;

	/** Half carry flag */
	static public final int PSW_AC	=	0x40;
	static public final int PSW_F0	=	0x20;
	static public final int PSW_RS1	=	0x10;
	static public final int PSW_RS0	=	0x08;
	static public final int PSW_OV	=	0x04;
	static public final int PSW_F1	=	0x02;
	static public final int PSW_P	=	0x01;

	/** Stack pointer register */
	static public final int SP	=	0x81;

	/** DPTR Low byte register */
	static public final int DPL	=	0x82;

	/** DPTR high byte register */
	static public final int DPH	=	0x83;

	/** Port 0 data */
	static public final int P0	= 0x80;
	static public final int P0M1	= 0x84;
	static public final int P0M2	= 0x85;

	/** Port 1 data */
	static public final int P1	= 0x90;
	static public final int P1M1	= 0x91;
	static public final int P1M2	= 0x92;

	/** Port 2 data */
	static public final int P2	= 0xA0;
	static public final int P2M1	= 0xA4;
	static public final int P2M2	= 0xA5;

	/** Port 3 data */
	static public final int P3	= 0xB0;
	static public final int P3M1	= 0xB1;
	static public final int P3M2	= 0xB2;

	/*  Serial port Control register */
	static public final int SCON	= 0x98;

	/** Serial port transmitter empty bit */
	static public final int SCON_TI	= 0x02;

	/** Serial port received redy bit */
	static public final int SCON_RI	= 0x01;

	/** Serial port read /write buffer */
	static public final int SBUF	= 0x99;

	/** Timer control register */
	static public final int TCON	= 0x88;
	
	static public final int TCON_TF1= 0x80;
	static public final int TCON_TR1= 0x40;
	static public final int TCON_TF0= 0x20;
	static public final int TCON_TR0= 0x10;

	/** Timer mode register */
	static public final int TMOD		= 0x89;
	
	static public final int TMOD_GATE1	= 0x80;
	static public final int TMOD_C_T1	= 0x40;
	static public final int TMOD_T1_M1	= 0x20;
	static public final int TMOD_T1_M0	= 0x10;
	static public final int TMOD_GATE0	= 0x08;
	static public final int TMOD_C_T0	= 0x04;
	static public final int TMOD_T0_M1	= 0x02;
	static public final int TMOD_T0_M0	= 0x01;

	/** Timer 0 counter low byte */
	static public final int TL0		= 0x8a;
	
	/** Timer 0 counter high byte */
	static public final int TH0		= 0x8c;
	
	/** Timer 1 counter low byte */
	static public final int TL1		= 0x8b;
	
	/** Timer 1 counter high byte */
	static public final int TH1		= 0x8d;

	/** Interrupt enable register */
	static public final int IE		= 0xA8;

	/** General interrupt enable */
	static public final int IE_EA		= 0x80;
	static public final int IE_EC		= 0x40;
	static public final int IE_ET2		= 0x20;
	
	/** Serial interrupt enable */
	static public final int IE_ES		= 0x10;

	/** Interrupt timer 1 enable */
	static public final int IE_ET1		= 0x08;
	
	static public final int IE_EX1		= 0x04;

	/** Interrupt timer 0 enable */
	static public final int IE_ET0		= 0x02;
	
	static public final int IE_EX0		= 0x01;


	static public final int AUXR1		= 0xa2;

}
