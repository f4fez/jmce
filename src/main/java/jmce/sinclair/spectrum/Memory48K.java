/**
   $Id: Memory48K.java 814 2012-03-29 11:07:49Z mviara $

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

import jmce.sim.*;
import jmce.sim.memory.*;

/**
 * Spectrum 48K memory<p>
 * 
 * <pre>
 * C000 Ram
 * 8000 Ram
 * 4000	Video memory
 * 0000	Rom
 * </pre>
 *
 * @author Mario Viara
 * @version 1.00
 * @since 1.02
 */
public class Memory48K extends CombinedMemory implements SpectrumMemory
{
	Memory bank[] = new Memory[4];
	
	public Memory48K()
	{
		super(CPU.MAIN_MEMORY);
		
		for (int i = 0 ; i < 4 ; i++)
		{
			bank[i] = new PlainMemory("Bank"+i,16*1024);
			addHardwareMemory(bank[i]);
		}

		/** Bank 0 is ROM */
		bank[0].setReadOnly();

	}

	public Memory getVideoMemory()
	{
		return bank[1];
	}
}

