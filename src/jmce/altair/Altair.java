/**
   $Id: Altair.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.altair;

import jmce.sim.*;

import jmce.sim.MemoryReadListener;

/**
 * Altair 8800 system.
 * 
 * <p>Implements the Altair main class.
 * 
 * <p>The original Alteir 8800 is completly supported the SIMH
 * implementation is only partially supported and not all software run
 * correctly.
 * <p>
 * <h2>Supported peripheral</h2>
 * <ul>
 *  <li>1 x Z80 cpu.</li>
 *  <li>1 x Altair MITS-8800 floppy disk controller.</li>
 *  <li>4 x MITS 8800-DISK floppy disk drive (77/1/32/137).</li>
 *  <li>1 x Switch on the front panel (port 0FFH).</li>
 *  <li>2 x Serial port one connected to a VT100.</li>
 *  <li>1 x boot load from file altair/boot.com.</li>
 *  <li>1 x SIMH partial support CP/M 3.0 do not work and CP/M 2.2 is
 *  not perfect.</li>
 * </ul>
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Altair extends jmce.zilog.z80.Z80 implements MemoryReadListener,AltairConstants
{
	private int SR = 0;
	
	public Altair()
	{
	}

	protected void initMemories()
	{
		if (getHardware(Memory.class) == null)
			addHardware(new Memory());

		super.initMemories();
	}


	protected void initPeripherals() throws SIMException
	{
		if (getHardware(Loadable.class) == null)
		{
			Loadable boot = new Loadable("altair/boot.rom",RESET_ADDRESS);
			setResetAddress(RESET_ADDRESS);
			addHardware(boot);
		}
		
		
		if (getHardware(Console.class) == null)
		{
			Console c = new Console();
			jmce.sim.terminal.Terminal t = jmce.sim.terminal.Terminal.createTerminal();;
			c.addHardware(t);
			c.setConnected(t);
			addHardware(c);
		}

		if (getHardware(FDC.class) == null)
		{
			FDC fdc = new FDC();
			fdc.addAltairFD("altair/altcpm.dsk");
			addHardware(fdc);
		}


		// Add SR
		addIOReadListener(0xFF,this);


		if (getHardware(SIMH.class) == null)
		{
			addHardware(new SIMH());
		}
		
		super.initPeripherals();
	}


	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		return SR;
	}

	public void setSR(int sr)
	{
		this.SR = sr;
	}

	public int getSR()
	{
		return SR;
	}
			
}
