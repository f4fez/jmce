/**
   $Id: MCS52.java 694 2011-09-02 12:01:08Z mviara $

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
package jmce.intel.mcs51;

import jmce.sim.*;
import jmce.sim.memory.PlainMemory;

/**
 * Implementation of CPU for Intel MCS52.
 * <p>
 * Intel 8052 is one extension of Intel 8051 with 256 bytes of internal
 * memory (memory DATA) and a new <tt>Timer2</tt> peripheral
 * <p>
 *
 * <h3>Implemented peripheral other than MCS51 :</h3>
 * <p>
 * <ul>
 *   <li>1 x Timer2</li>
 *   <li>1 x 256 byte DATA memory.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.01
 *
 */
public class MCS52 extends MCS51 implements MCS52Constants
{
	
	/**
	 * Default constructor
	 */
	public MCS52()
	{
		setName("i8052");
	}

	
	@Override
	protected void initMemories() 
	{
		Memory m = getMemoryForName(DATA_MEMORY);
		if (m == null)
			addHardware(new PlainMemory(DATA_MEMORY,256));
		
		super.initMemories();
	}

	@Override
	protected void initNames()
	{
		super.initNames();
		setSfrName(TL2,"TL2");
		setSfrName(TL2,"TH2");
		setSfrName(T2MOD,"T2MOD");
		setSfrName(T2CON,"T2CON");
		setSfrName(RCAP2H,"RCAP2H");
		setSfrName(RCAP2L,"RCAP2L");

		
	}

	@Override
	protected void initPeripherals() throws SIMException
	{

		if (getHardware(Timer2.class) == null)
			addHardware(new Timer2());
		
		super.initPeripherals();
		
	}


}
