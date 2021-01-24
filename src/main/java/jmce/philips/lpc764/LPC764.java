/**
   $Id: LPC764.java 691 2011-09-02 07:57:21Z mviara $

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
package jmce.philips.lpc764;

import jmce.sim.*;
import jmce.sim.memory.*;

import jmce.intel.mcs51.*;



/**
 * Philips/NXP LPC764
 * <p>
 *
 * LPC764 is one Philips single chip microcontroller compatible with
 * Intel 8051, with internal ROM. One good reason to use it is that is
 * one of the few chip available in dual inline case.
 * <p>
 *
 * <h3>Implemented peripheral :</h3>
 * <ul>
 *   <li>1 x CODE - 4 Kb rom.</li>
 *   <li>1 x DATA - 256 bytes.</li>
 *   <li>1 x XDATA - 0 bytes.</li>
 *   <li>2 x DPTR.<li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class LPC764 extends MCS51 implements LPC764Constants
{
	
	
	public LPC764()
	{
		setName("LPC764");

		/* Enable dual DPTR @since 1.02 */
		setAuxrDptrEnabled();

		setClock(6000000);
		setClockPerCycle(6);


	}

	protected void initMemories()
	{
		Memory m ;

		m = getMemoryForName("XDATA");
		
		if (m == null)
		{
			addHardware(new PlainMemory("XDATA",0));
		}
		
		m = getMemoryForName("CODE");

		if (m == null)
		{
			m = new PersistentMemory("CODE",getName()+".flash",4096);
			addHardware(m);
		}

		super.initMemories();
	}

	@Override
	protected void initPeripherals() throws SIMException
	{
		if (getHardware(Ports.class) == null)
		{
			Ports p = new Ports();
			addHardware(p);
		}

		super.initPeripherals();

	}




}
