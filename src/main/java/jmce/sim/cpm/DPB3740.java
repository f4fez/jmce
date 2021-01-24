/**
   $Id: DPB3740.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.sim.cpm;

/**
 * Standard 8" IBM 3740.<p>
 *
 * Standard IBM 3740 SS SD
 * 
 * <ul>
 * <li>77 Track.</li>
 * <li>26 Sector.</li>
 * <li>128 byte for sector.</li>
 * <li>Single side.</li>
 * <li>Single density.</li>
 * </ul>
 * <p>
 * 
 * This was the only standard disk format available to exchange data
 * between different system.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class DPB3740 extends DPB
{
	static private int trans[] = {1,7,13,19,25,5,11,17,
				      23,3,9,15,21,2,8,14,
				      20,26,6,12,18,24,4,10,16,22};
	
	public DPB3740()
	{
		super(26,3,7,0,242,63,192,0,16,2);
		setTranslation(trans);
	}
}
