/**
   $Id: M65XXConstants.java 441 2010-11-19 08:54:49Z mviara $

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
package jmce.mos;

/**
 * MOS 65XX Constants.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.01
 */
public interface M65XXConstants
{
	/** Sign */
	public static final int P_S = 0x80;

	/** Overflow */
	public static final int P_V = 0x40;


	public static final int P_E = 0x20;

	/** BRK command */
	public static final int P_B = 0x10;

	/** Decimal mode */
	public static final int P_D = 0x08;

	/** Irq disable */
	public static final int P_I = 0x04;

	/** Zero flag */
	public static final int P_Z = 0x02;

	/** Carry Flag */
	public static final int P_C = 0x01;

	/** Page for the ZERO address */
	public static final int Z_PAGE		= 0x0000;

	/** Page for the stack */
	public static final int S_PAGE		= 0x0100;


	/** Interrupt vector */
	public static final int IRQ_VECTOR	= 0;

	/** Break vector */
	public static final int BRK_VECTOR	= 0;
	
	/** Reset vector */
	public static final int RESET_VECTOR	= 1;

	/** NMI vector */
	public static final int NMI_VECTTOR	= 2;


}
