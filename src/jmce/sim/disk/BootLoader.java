/**
   $Id: BootLoader.java 946 2012-12-02 11:01:18Z mviara $

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
package jmce.sim.disk;

import jmce.sim.*;

import jmce.util.Logger;
import jmce.util.Hex;

/**
 * Automatic disk boot loader.
 *
 * <p>
 * 
 * This <code>Peripheral</code> at CPU reset search for a
 * <code>DiskController</code> and if it is present load the first
 * sector of the first drive in the main memory at the address specified
 * with setAddres method.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class BootLoader extends jmce.sim.AbstractPeripheral implements jmce.sim.ResetListener
{
	private static Logger log = Logger.getLogger(BootLoader.class);
	private int address = 0;

	public BootLoader()
	{
		setName("DiskBootLoader");
		setAddress(0);
	}

	/**
	 * Set the address where the boot will be loaded
	 */
	public void setAddress(int a)
	{
		address = a;
	}

	/**
	 * Return the address where the boot will be loaded
	 */
	public int getAddress()
	{
		return address;
	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addResetListener(this);

	}

	/**
	 * Load code thru reset listener to be sure that all peripheral
	 * are ready and initialized.
	 */
	public void reset(jmce.sim.CPU cpu) throws SIMException
	{
		DiskController fdc = (DiskController)cpu.getHardware(DiskController.class);
		if (fdc == null)
			log.info("FDC not installed");
		else
		{
			if (fdc.getDiskCount() < 1)
				log.info("No disk installed");
			else
			{
				Disk d = fdc.getDisk(0);
				
				byte buffer[] = d.getBuffer();
			
				d.setTrack(0);
				d.setHead(0);
				d.setSector(1);
				int len = d.read();

				log.info("Bootloader "+len+" bytes from drive 0");
				for (int i = 0 ; i < len ; i++)
					cpu.setByte(i+address,buffer[i]);
			}
				
			
		}
	}

	public String toString()
	{
		return "Disk boot loader AT 0x"+Hex.formatWord(address);
	}
}
