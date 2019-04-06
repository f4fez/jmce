/**
   $Id: AbstractDiskController.java 632 2011-06-14 11:17:35Z mviara $

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
import jmce.sim.cpm.*;

/**
 * Abstract implementation of DiskCntroller.
 *
 * <p>Implements all function not hardare depending.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class AbstractDiskController extends jmce.sim.AbstractPeripheral implements DiskController
{
	protected int drive;
	protected final Disk nullDisk = new NullDisk();
	protected Disk disk = nullDisk;
	
	public AbstractDiskController()
	{
	}

	public int getDrive()
	{
		return drive;
	}

	
	public void setDrive(int drive) throws SIMException
	{
		disk = getDisk(drive);
		this.drive = drive;
	}
	


	public int getDiskCount()
	{
		return getHardwareCount();
	}
	
	public Disk getDisk(int n) throws SIMException
	{
		return (Disk)getHardware(n);
	}

	public void addDirDiskIBM3740(String dir)
	{
		addDirDiskCPM(dir,new DPB3740());
	}
	
	public void addDirDiskCPM(String dir,DPB dpb)
	{
		DirDiskCPM d = new DirDiskCPM();
		d.setDirectory(dir);
		d.setDPB(dpb);
		addHardware(d);
	}
	
	public void addImageDisk(String name,DPB dpb)
	{
		ImageDiskCPM d = new ImageDiskCPM();
		d.setDPB(dpb);
		d.setImageName(name);
		addHardware(d);
	}
	
	public void addImageDiskIBM3740(String name)
	{
		addImageDisk(name,new DPB3740());
	}

	public void addNullDisk()
	{
		addHardware(nullDisk);
	}
}
