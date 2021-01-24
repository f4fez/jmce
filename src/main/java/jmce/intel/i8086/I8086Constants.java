/**
   $Id: I8086Constants.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.intel.i8086;

/**
 * Intel 8086 constants.
 * <p>
 * This interface define all constants relative to the intel 8086.
 */
public interface I8086Constants
{

	/** Value of IP at reset */
	public final int RESET_IP = 0xFFF0;

	/** Value of CS at reset */
	public final int RESET_CS = 0xF000;

	public final int F_CF = 0x0001;
	public final int F_R1 = 0x0002;
	public final int F_PF = 0x0004;
	public final int F_R3 = 0x0008;
	public final int F_AF = 0x0010;
	public final int F_R5 = 0x0020;
	public final int F_ZF = 0x0040;
	public final int F_SF = 0x0080;
	public final int F_TF = 0x0100;
	
	/** Interrupt enable flag */
	public final int F_IF = 0x0200;
	public final int F_DF = 0x0400;
	public final int F_OF = 0x0800;

	/** Segment register */
	public final int ES_R = 0;
	public final int CS_R = 1;
	public final int SS_R = 2;
	public final int DS_R = 3;

	/** Other register */
	public final int AX_R = 0;
	public final int CX_R = 1;
	public final int DX_R = 2;
	public final int BX_R = 3;
	public final int SP_R = 4;
	public final int BP_R = 5;
	public final int SI_R = 6;
	public final int DI_R = 7;
	
	/** Repeat prefix */
	public final int REPE_P = 0;
	public final int REPNE_P = 1;
}
