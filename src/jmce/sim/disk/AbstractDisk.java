/**
   $Id: AbstractDisk.java 632 2011-06-14 11:17:35Z mviara $

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

/**
 * Abstract implementation of <code>Disk</code>.
 * 
 * <p>
 * Subclass must implements only the <code>write</code> and
 * <code>read</code> method to access the phisical disk and if
 * necessary can override the <code>mount</code> and
 * <code>dismount</code> method.
 * 
 * @author Mario Viara
 * @version 1.01
 */
public abstract class AbstractDisk extends jmce.sim.AbstractPeripheral implements Disk
{
	private static Logger log = Logger.getLogger(AbstractDisk.class);
	protected int numTrack,numHead,numSector,sectorSize;
	protected boolean mounted = false;
	protected boolean readOnly = false;
	/** Buffer for one sector */
	protected byte buffer[] = null;
	/** Disk current position */
	protected int track,head,sector;
	
	/**
	 * Default contructor.
	 * 
	 * Create a disk wit IBM 3740 geometry.
	 */
	public AbstractDisk()
	{
		// Default is IBM 3740 disk
		this(77,1,26,128);
	}

	/**
	 * Constructor with the specified geometry.
	 *
	 * @param track - Number of track.
	 * @param head - Number of head.
	 * @param sector - Number of sector.
	 * @param ssize - Sector size in bytes.
	 */
	public AbstractDisk(int track,int head,int sector,int ssize)
	{
		setName("Disk");
		setNumTrack(track);
		setNumHead(head);
		setNumSector(sector);
		setSectorSize(ssize);
	}

	
	public void setNumTrack(int t)
	{
		numTrack = t;
	}

	public int getNumTrack()
	{
		return numTrack;
	}

	public void setNumSector(int s)
	{
		numSector = s;
	}

	
	public int getNumSector()
	{
		return numSector;
	}

	public void setNumHead(int h)
	{
		numHead = h;
	}

	public int getNumHead()
	{
		return numHead;
	}

	public void setSectorSize(int size)
	{
		sectorSize = size;
		buffer = new byte[size];
	}

	public byte[] getBuffer()
	{
		return buffer;
	}


	public void dismount() throws SIMException
	{
		setMounted(false);
	}
	
	public boolean isMounted()
	{
		return mounted;
	}

	protected boolean setMounted(boolean b)
	{
		mounted = b;
		return mounted;
	}
	public boolean mount() throws SIMException
	{
		return setMounted(true);
	}
	
	public int read(int track,int head,int sector) throws SIMException
	{
		setTrack(track);
		setHead(head);
		setSector(sector);
		return read();
	}

	public int read() throws SIMException
	{
		if (!seek(track,head,sector))
			return ERROR_SEEK;

		int pos = numSector * numHead * track + numSector * head + sector - 1;
		pos *= sectorSize;

		
		int n = read(pos);


		return n;
	}


	/**
	 * Check the coordinate of the sector.
	 *
	 * Check the coordinate of the sector and if the disk is
	 * correctly mounted.
	 * 
	 * @return true if the sector is valid.
	 */
	private boolean seek(int track,int head,int sector) throws SIMException
	{
		if (track < 0 || track >= getNumTrack())
		{
			
			log.info("Track "+track+" not valid.");
			
			return false;
		}

		if (head < 0 || head >= getNumHead())
		{
			log.info("Head "+head+" not valid.");
			return false;
		}

		if (sector < 1 || sector > getNumSector())
		{
			log.info("Sector "+sector+" not valid.");

			return false;
		}

		if (!isMounted())
			if (!mount())
			{
				log.info("Drive  not ready "+getName());
				return false;
			}

		return true;

	}
	
	public int write(int track,int head,int sector) throws SIMException
	{
		setTrack(track);
		setHead(head);
		setSector(sector);
		return write();
	}

	public int write() throws SIMException
	{
		/** since 1.01 Check for read/only */
		if (readOnly)
			return ERROR_WRITE_PROTECT;
		
		if (!seek(track,head,sector))
			return ERROR_SEEK;
		

		int pos = numSector * numHead * track + numSector * head + sector - 1;
		pos *= sectorSize;
		int n = write(pos);


		return n;
	}


	
	public int getSectorSize()
	{
		return sectorSize;
	}

	public int getSector()
	{
		return sector;
	}

	public int getHead()
	{
		return head;
	}

	public int getTrack()
	{
		return track;
	}

	public void setHead(int head)
	{
		this.head = head;
	}

	public void setSector(int sector)
	{
		this.sector = sector;
	}


	public void setTrack(int track)
	{
		this.track = track;
	}
	
	/**
	 * Format one track.
	 *
	 * For one simulator format means write a default pattern in
	 * all sector of the disk
	 */
	public void format() throws SIMException
	{
		
		for (int i = 0 ; i < buffer.length ; i++)
			buffer[i] = (byte)0xFF;

		for (int s = 1; s <= getNumSector() ; s++)
		{
			setSector(s);
			write();
		}
	}

	/**
	 * Write one sector.
	 *
	 * Write one sector at specified position. Must be implemented
	 * by subclass.
	 *
	 * @return the number of bytes written.
	 */
	abstract protected int write(int pos) throws SIMException;
	
	/**
	 * Read one sector.
	 *
	 * Read one sector at specified position. Must be implemented
	 * by subclass.
	 *
	 * @return the number of bytes readed.
	 */
	abstract protected int read(int pos) throws SIMException;

	public int getDiskSize()
	{
		return getNumTrack() * getNumHead() * getNumSector() * getSectorSize();
	}

	public String getDiskSizeString()
	{
		int size = getDiskSize();

		if (size < 1024 * 1024)
			return size +" Bytes";
		else if (size < 1024 * 1024 * 1024)
			return ((size + 1023 - 1) / ( 1024 ))+" KB";
		else
			return (size / (1024*1024))+" MB";
	}


	/**
	 * Return true if the disk is read only.
	 *
	 * @since 1.01
	 */
	public boolean getReadOnly()
	{
		return readOnly;
	}

	/**
	 * Set the read only status for the disk.
	 *
	 * @since 1.01
	 */
	public void setReadOnly(boolean mode)
	{
		readOnly = mode;
	}
	
		
}
