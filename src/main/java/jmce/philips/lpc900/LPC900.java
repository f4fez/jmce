/**
   $Id: LPC900.java 810 2012-03-15 00:31:07Z mviara $

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
package jmce.philips.lpc900;

import jmce.sim.*;
import jmce.intel.mcs51.*;
import jmce.sim.memory.*;


/**
 * NXP LPC9xx family.
 * <p>
 * The LPC9xx is a family of Philips/NXP single chip microcontroller compatible
 * with the intel 8052 but with more internal peripheral and one
 * machine cycle of only <tt>2</tt> clock compared with the <tt>12</tt>
 * of the original intel implementation.
 * <p>
 * <h3>Implemented peripheral : </h3>
 * <ul>
 *   <li>1 x CODE flash memory of least 8 KB</li>
 *   <li>1 x XDATA ram up to 768 bytes.</li>
 *   <li>3 x 8 bit I/O port.</li>
 * </ul>
 *
 * @author Mario Viara
 * @version 1.00
 */
public class LPC900 extends MCS52 implements LPC900Constants
{
	private int flashSize = 8 * 1024;
	
	public LPC900()
	{
		setName("LPC900");
		setClock(7372800);
		setClockPerCycle(2);
	}

	public void setFlashSize(int n)
	{
		this.flashSize = n;
	}
	
	protected void initMemories()
	{
		Memory m ;

		m = getMemoryForName("CODE");
		
		if (m == null)
		{
			m = new PersistentMemory("CODE",getName()+".flash",flashSize);
			addHardware(m);
		}
		

		
		m = getMemoryForName("XDATA");
		
		if (m == null)
			m = (Memory)addHardware(new PlainMemory("XDATA",768));
		
		super.initMemories();
	}


	protected void initPeripherals() throws SIMException
	{
		if (getHardware(jmce.intel.mcs51.Ports.class) == null)
		{
			addHardware(new  Ports());
		}

		super.initPeripherals();

	}

	@Override
	protected void initNames()
	{
		super.initNames();

		/**
		 * Add LPC900 specific SFR names
		 */
		setSfrName(LPC900Constants.FMDATA,	"FMDATA");
		setSfrName(LPC900Constants.FMCON,	"FMCON");
		setSfrName(LPC900Constants.FMADRL,	"FMADRL");
		setSfrName(LPC900Constants.FMADRH,	"FMADRH");
		setSfrName(LPC900Constants.DEECON,	"DEECON");
		setSfrName(LPC900Constants.DEEDAT,	"DEEDAT");
		setSfrName(LPC900Constants.DEEADR,	"DEEADR");
		setSfrName(LPC900Constants.AUXR1,	"AUXR1");
	}

	
}
