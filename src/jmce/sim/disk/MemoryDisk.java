/**
   $Id: MemoryDisk.java 632 2011-06-14 11:17:35Z mviara $

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

/**
 * Memory image disk.
 *
 *<p>
 * Disk are stored in one array of byte. <p>
 * Data are not initialized so the disk must be formatted.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class MemoryDisk extends AbstractDisk
{
	private byte disk[] = null;
	
	public MemoryDisk()
	{
	}

	public MemoryDisk(int track,int head,int sector,int ssize)
	{
		super(track,head,sector,ssize);
	}

	public boolean mount() throws SIMException
	{
		if (isMounted())
			return true;

		/**
		 * The first mount() allocate the disk memory.
		 */
		if (disk == null)
			disk = new byte[getNumTrack() * getNumHead() * getNumSector() * getSectorSize()];
			 
		return setMounted(true);

	}

	protected final int write(int pos) throws SIMException
	{
		int s = getSectorSize();
		System.arraycopy(buffer,0,disk,pos,s);

		return s;
	}
	
	protected final int read(int pos) throws SIMException
	{
		int s = getSectorSize();
		
		System.arraycopy(disk,pos,buffer,0,s);

		return s;

	}

	public String toString()
	{
		return "Memory "+getNumTrack()+"/"+getNumHead()+"/"+getNumSector()+"/"+getSectorSize()+" "+getDiskSizeString();
	}

}
