/**
   $Id: Spectrum128K.java 814 2012-03-29 11:07:49Z mviara $

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
package jmce.sinclair.spectrum;

import jmce.sim.SIMException;

/**
 * Sinclair Spectrum 128K.<p>
 * 
 * <h2>Implemented peripheral</h2>
 * <ul>
 *   <li>1 x Z80 cpu runnning at 4.5 MHz</li>
 *   <li>2 x 16Kb ROM</li>
 *   <li>8 x 16Kb RAM</li>
 *   <li>1 x Memory manager</li>
 *   <li>1 x 20 ms interrupt timer</li>
 *   <li>1 x Screen 256x192 (supporting zoom)</li>
 *   <li>1 x Keyboard 5 x 8</li>
 *   <li>1 x Speaker</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 * @since 1.02
 */
public class Spectrum128K extends Spectrum implements SpectrumConstants
{
	private Memory128K mem;
	
	public Spectrum128K()
	{
	}

	protected void initPeripherals() throws SIMException
	{

		if (getHardware(Timer.class) == null)
			addHardware(new Timer());

		if (getHardware(Screen.class) == null)
		{
			Screen s = new Screen(2);
			addHardware(s);
		}

		if (getHardware(Speaker.class) == null)
			addHardware(new Speaker());
		
		super.initPeripherals();




	}

	protected void initMemories()
	{

		mem = (Memory128K)getMemoryForName(MAIN_MEMORY);
		
		if (mem == null)
		{
			mem = new Memory128K();
			addHardware(mem);
		}
		

		super.initMemories();
	}


}
