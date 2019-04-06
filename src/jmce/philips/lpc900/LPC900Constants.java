/**
   $Id: LPC900Constants.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.philips.lpc900;

/**
 * NXP LPC900 Constants.
 *
 * @author Mario Viara
 * @version 1.00
 */
public interface LPC900Constants extends jmce.intel.mcs51.MCS51Constants
{
	static public final int FMCON	= 0xe4;
	static public final int FMDATA	= 0xe5;
	static public final int FMADRL	= 0xe6;
	static public final int FMADRH	= 0xe7;
	static public final int DEECON	= 0xf1;
	static public final int DEEDAT  = 0xf2;
	static public final int DEEADR  = 0xf3;
	static public final int AUXR1	= 0xa2;
	static public final int AUXR1_SRST = 0x08;

	static public final int WDCON	= 0xa7;
	static public final int IE_WD	=0x40;

	// 1 Clock 400Khz , 0 Clock CCLK
	static public final int WDCON_WDCLK	= 0x01; 
	static public final int WDCON_WDTOF	= 0x02;
	// 1 WD running
	static public final int WDCON_WDRUN	= 0x04;

	static public final int WDL	= 0xC1;
	static public final int WDFEED1 = 0xC2;
	static public final int WDFEED2 = 0xC3;

	static public final int FLASH_MISC = 0xfff0;

	static public final int UCFG1	= 0x00;
	// 1 Watch dog reset enable
	static public final int UCFG1_WDTE = 0x80;
	// 1 Watch dog safety enable
	static public final int UCFG1_WDSE = 0x10;

	static public final int BOOTV	= 0x02;
	static public final int BOOTSTAT= 0x03;
	static public final int SEC0	= 0x08;
	static public final int SEC1	= 0x09;
	static public final int SEC2	= 0x0A;
	static public final int SEC3	= 0x0B;
	static public final int SEC4	= 0x0C;
	static public final int SEC5	= 0x0D;
	static public final int SEC6	= 0x0E;
	static public final int SEC7	= 0x0F;
}
