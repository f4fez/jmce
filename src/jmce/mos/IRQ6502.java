/**
   $Id: IRQ6502.java 615 2011-05-31 09:25:43Z mviara $

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

import jmce.sim.*;

/**
 * MOS 6502 family interrupt manager.
 * <p>
 * 
 * The cpu have only one interrupt pin and one interrupt routine so this
 * implementation use a simple level sensitive interrupt controller.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public class IRQ6502 extends Interrupt
{
	
	/**
	 * Standard constructor.
	 */
	public IRQ6502(M6502 m6502,String name)
	{
		super(m6502,name,M65XXConstants.IRQ_VECTOR);
	}


}
