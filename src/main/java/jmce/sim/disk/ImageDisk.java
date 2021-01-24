/**
   $Id: ImageDisk.java 946 2012-12-02 11:01:18Z mviara $

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

import java.io.RandomAccessFile;
import java.io.File;

import jmce.util.Logger;
import jmce.sim.*;

/**
 * Disk implementation using a file image.
 * 
 * <p>
 * One copy of the disk is stored in one file and the disk read and
 * write operation operate in this file.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class ImageDisk extends AbstractDisk
{
	private static Logger log = Logger.getLogger(AbstractDisk.class);
	private String imageName;
	private RandomAccessFile rf = null;

	public ImageDisk(String name)
	{
		this(77,1,26,128,name);
	}
		
	public ImageDisk()
	{
		this(77,1,26,128,"ibm3740.dsk");
	}

	public ImageDisk(int t,int h,int s,int ss,String filename)
	{
		super(t,h,s,ss);
		setImageName(filename);
	}

	/**
	 * Set the filename of the disk image
	 *
	 * @param name 
	 */
	public void setImageName(String name)
	{
		imageName = name;
	}

	/**
	 * Return the name of the disk image.
	 *
	 */
	public String getImageName()
	{
		return imageName;
	}

	/**
	 * Dismount the disk.
	 */
	public void dismount() throws SIMException
	{
		setMounted(false);
		try
		{
			if (rf != null)
			{
				rf.close();
				rf=null;
			}
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(imageName,"close()");
		}
	}

	/**
	 * Mount the disk.
	 *
	 * @return true if the disk is correctly mounted.
	 */
	public boolean mount() throws SIMException
	{
		if (isMounted())
			return true;

		File file = new File(imageName);
		
		if (!file.exists())
		{
			log.info(imageName+" do not exist");
			return setMounted(false);
		}
		
		if (!file.isFile())
		{
			log.info(imageName+" is not a regular file");
			return setMounted(false);
		}

		try
		{
			rf = new RandomAccessFile(imageName,"rw");
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(imageName,"open()");
		}

		return setMounted(true);
	}
	


	public final int read(int pos) throws SIMException
	{
		if (!mounted)
			throw new SIMException(imageName+" read without mount");
		log.fine("Read at "+pos+" from "+imageName+" Len="+buffer.length);
		try
		{
			rf.seek(pos);
		
			int n = rf.read(buffer,0,sectorSize);
		
			return n;
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(imageName,"Reading");
		}
		
	}
	
	public final int write(int pos) throws SIMException
	{
		if (!mounted)
			throw new SIMException(imageName+" write without mount");
		log.fine("Write at "+pos+" to "+imageName+" len="+buffer.length);
		try
		{
			rf.seek(pos);

			rf.write(buffer,0,sectorSize);

			return sectorSize;
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(imageName,"Writing");
		}

		
	}

	public String toString()
	{
		return imageName+" "+getNumTrack()+"/"+getNumHead()+"/"+getNumSector()+"/"+getSectorSize()+" "+getDiskSizeString();
	}
}

