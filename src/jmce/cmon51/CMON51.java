/**
   $Id: CMON51.java 792 2012-02-14 20:06:36Z mviara $

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
package jmce.cmon51;

import jmce.sim.*;
import jmce.sim.memory.*;

/**
 * 
 * Special 8052 with XDATA and CODE shared.
 * <p>
 * The CODE is mapped from 0000-7FFF in ROM and from 8000 to FFFF in
 * XDATA the XDATA is always mapped from 0000-7FFF.
 * 
 * <p>At reset load in memory the application <tt>hex/cmon51.hex</tt>.
 *
 * @author Mario Viara
 * @version 1.00
 *
 */
public class CMON51 extends jmce.intel.mcs51.MCS52
{
	public CMON51()
	{
		setName("CMON51");
	}
	

	public void init(Hardware parent) throws SIMException
	{
		Memory xdata = getMemoryForName("XDATA");
		
		if (xdata == null)
			xdata = (Memory)addHardware(new PlainMemory("XDATA",0x8000));

		if (getMemoryForName("CODE") == null)
		{
			CombinedMemory code = new CombinedMemory("CODE");
			Memory rom = new LoadableMemory("CODE",0x8000,"hex/cmon51.hex");
			code.addHardware(rom);
			code.addMemory(rom);
			code.addMemory(xdata);
			addHardware(code);
		}

		super.init(parent);
	}

}
