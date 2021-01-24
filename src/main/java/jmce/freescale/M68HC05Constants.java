/**
   $Id: M68HC05Constants.java 589 2011-05-18 16:42:27Z mviara $

   Copyright (c) 2011, Mario Viara

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
package jmce.freescale;

/**
 * Motorola freescale M68HC05 family constants.<p>
 *
 * @author Mario Viara
 * @since 1.02
 */
public interface M68HC05Constants
{
	/** Condition Code register */
	static public final int CCR_C	= 0x01;	// Carry
	static public final int CCR_Z	= 0x02;	// Zero
	static public final int CCR_N	= 0x04;	// Negative
	static public final int CCR_I	= 0x08;	// Interrupt
	static public final int CCR_H	= 0x10; // Half carry

	/** CCR and and or Mask */
	static public final int CCR_AND_MASK	= 0x1F;
	static public final int CCR_OR_MASK	= 0xE0;

	/** Interrupt vector */
	static public final int M68HC05_RESET_VECTOR	= 0x07FE;
	static public final int SWI_INTERRUPT	= 0x07FC;
	static public final int EXT_INTERRUPT	= 0x07FA;
	static public final int TIMER_INTERRUPT	= 0x07F8;
	
}

