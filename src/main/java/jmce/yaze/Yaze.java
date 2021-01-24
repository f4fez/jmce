/**
   $Id: Yaze.java 811 2012-03-18 12:16:14Z mviara $

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
package jmce.yaze;

import jmce.sim.*;
import jmce.sim.cpm.*;

/**
 * Yaze emulator.
 * <p>
 * This class implemets a bios less version of the Yaze emulator.
 * <p>
 * <h2>Implemented peripheral</h2> :
 * <ul>
 *   <li>Z80 cpu</li>
 *   <li>16 x 64K Memory in page of 4K</li>
 *   <li>2 x IBM 3740 floppy disk.</li>
 *   <li>2 x 4 MB harddisk.</li>
 *   <li>1 x serial console connected to CRT.</li>
 *   <li>1 x automatic boot loader from first drive.</li>
 *   <li>1 x system kit</li>
 * </ul>
 *
 * <h2>Default configuration :</h2>
 * <ul>
 *   <li>Drive A file yaze/cpm30.cpm with bootable
 *   CP/M 3.0 operating system</li>
 *   <li>Drive B directory disks/fd1</li>
 *   <li>Drive C file yaze/hd-work.cpm Empty harddisk.</li>
 *   <li>Drive D directory disks/hd many CP/M application.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 *
 * @see jmce.sim.SysKit
 */
public class Yaze extends jmce.zilog.z80.Z80
{
	public Yaze()
	{
	}

	@Override
	protected void initMemories()
	{
		if (getHardware(Memory.class) == null)
			addHardware(new Memory());
		super.initMemories();
	}
	
	public void initPeripherals() throws SIMException
	{
		if (getHardware(Console.class) == null)
		{
			Console c = new Console();
			jmce.sim.terminal.Terminal t = jmce.sim.terminal.Terminal.createTerminal();
			c.addHardware(t);
			c.setConnected(t);
			addHardware(c);
		}

		if (getHardware(DiskController.class) == null)
		{
			FDC fdc = new FDC();
			fdc.addImageDiskIBM3740("yaze/cpm3.cpm");
			fdc.addDirDiskIBM3740("disks/fd1");	
			fdc.addImageDiskYazeHD("yaze/hd-work.cpm");
			fdc.addDirDiskCPM("disks/hd",new DPBYAHD());
			addHardware(fdc);
		}

		if (getHardware(jmce.sim.disk.BootLoader.class) == null)
			addHardware(new jmce.sim.disk.BootLoader());


		if (getHardware(SysKit.class) == null)
			addHardware(new SysKit());
		
		super.initPeripherals();
	}
	

}
