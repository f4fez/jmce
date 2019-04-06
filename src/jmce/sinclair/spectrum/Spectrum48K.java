/**
   $Id: Spectrum48K.java 611 2011-05-26 15:11:11Z mviara $

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
package jmce.sinclair.spectrum;

import jmce.sim.*;
import jmce.sim.memory.PlainMemory;

/**
 * Sinclair Spectrum 48K.<p>
 * 
 * <h2>Implemented peripheral</h2> :
 * <ul>
 *   <li>1 x Z80 cpu runnning at 4.5 MHz</li>
 *   <li>1 x 16Kb ROM</li>
 *   <li>1 x 48Kb RAM</li>
 *   <li>1 x 20 ms interrupt timer</li>
 *   <li>1 x Screen 256x192 (supporting zoom)</li>
 *   <li>1 x Keyboard 5 x 8</li>
 *   <li>1 x Speaker</li>
 * </ul>
 *
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Spectrum48K extends Spectrum implements SpectrumConstants
{

	public Spectrum48K()
	{
	}

	protected void initPeripherals() throws SIMException
	{
		if (getHardware(Loadable.class) == null)
		{
			Loadable boot = new Loadable("sinclair/spectrum.rom");
			addHardware(boot);
		}
		
		super.initPeripherals();
	}
	
	protected void initMemories()
	{
		if (getMemoryForName(MAIN_MEMORY) == null)
			addHardware(new Memory48K());

		super.initMemories();
	}


}
