/**
   $Id: VIC6561.java 486 2010-12-23 16:49:11Z mviara $

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
package jmce.commodore.vic20;

/**
 * Special implementation of VIC6561 for commodore VIC20.
 * <p>
 * The bit 13 of address ememory from 6561 is connected to the bit 15
 * of the CPU.
 *
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public class VIC6561 extends jmce.mos.VIC6561
{
	/**
	 * Cosntructor with vic20 parameters.
	 */
	public VIC6561()
	{
		setBase(0x9000);
		setScaleWidth(3);
		setScaleHeight(2);
		setSizeHeight(260);
		setSizeWidth(208);
	}
	
	@Override
	public int convertAddress(int add)
	{
		int target = add & 0x1FFF;

		/** A13 is connected to !A15 */
		if ((add & 0x2000) == 0)
			target |= 0x8000;

		return target;
	}
}