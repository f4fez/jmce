/**
   $Id: Disk.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.sim;

/**
 * Interface to descibe a phisical disk.
 *
 * This interface define a phisical disk unit and permit all the basic
 * operation like read,write,etc. to the phisical disk.
 * <p>
 * For historical reason the first track and head number is 0 and the
 * first sector number is 1.
 * <p>
 * Data are moved to and from one buffer allocated from the disk and
 * the function for read and write only specify the sector number.
 * 
 * @author Mario Viara
 * @version 1.01
 */
public interface Disk extends Peripheral
{
	/**
	 * Generic error on read / write
	 *
	 * @deprecated
	 */
	@Deprecated
	static public final int ERROR = -1;
	
	/**
	 * Error for disk write protect
	 *
	 * @since 1.01
	 */
	static public final int ERROR_WRITE_PROTECT = -2;

	/**
	 * Seek error.
	 *
	 * @since 1.01
	 */
	static public final int ERROR_SEEK = -3;
	
	/**
	 * Return the sector size in bytes
	 */
	public int getSectorSize();

	/**
	 * Return the numebr of track
	 */
	public int getNumTrack();

	/**
	 * Return the number of sector
	 */
	public int getNumSector();

	/**
	 * Return the number of head
	 */
	public int getNumHead();

	/**
	 * Set the sector size in byte.
	 */
	public void setSectorSize(int v);

	/**
	 * Set the number of track.
	 *
	 * Set the total number of track. vlaid track number are 0 to
	 * track -1.
	 *
	 * @param track - Number of track.
	 */
	public void setNumTrack(int track);

	/**
	 * Set the number of sector.
	 *
	 * Set the toral number of sector. Valid sector number are 1 to
	 * sector.
	 *
	 * @param sector - Total number of sector.
	 */
	public void setNumSector(int sector);

	/**
	 * Set the number of head.
	 *
	 * Set the total number of head. Valid head number are 0 to
	 * head - 1.
	 *
	 * @param head - Number of head.
	 */
	public void setNumHead(int head);

	/**
	 * Dismount the disk
	 */
	public void dismount() throws SIMException;

	/**
	 * Mount the disk.
	 *
	 * @return true if the disk is correctly mounted.
	 */
	public boolean mount() throws SIMException;

	/**
	 * Retrun true if the disk is mounted.
	 */
	public boolean isMounted();

	/**
	 * Return one array used for data transfer.
	 *
	 * Is responsabity of the disk alloc one array of byte and use
	 * it for data transfer.
	 *
	 * @return The array for one sector.
	 */
	public byte[] getBuffer();

	/**
	 * Read one sector.
	 *
	 * The specified sector is readed form the disk and the data
	 * are moved in the allocated buffer.
	 * 
	 *
	 * @see #getBuffer()
	 * 
	 * @return number of bytes readed.
	 */
	public int read() throws SIMException;

	/**
	 * Write one sector.
	 *
	 * Before to call this function data must be moved in the
	 * allocated buffer.
	 *
	 * @return number of bytes written.
	 */
	public int write() throws SIMException;

	/**
	 * Format a track
	 */
	public void format() throws SIMException;

	/**
	 * Get the current sector
	 */
	public int getSector();

	/**
	 * Get the current head
	 */
	public int getHead();

	/**
	 * Get the current track
	 */
	public int getTrack();

	/**
	 * Set the track number
	 */
	public void setTrack(int track) throws SIMException;

	/**
	 * Set the sector number
	 */
	public void setSector(int sector) throws SIMException;

	/**
	 * Set the head number
	 */
	public void setHead(int head) throws SIMException;

	/**
	 * Set the disk r/o
	 *
	 * @since 1.01
	 */
	public void setReadOnly(boolean mode);

	/**
	 * Return true if the disk is r/o
	 *
	 * @since 1.01
	 */
	public boolean getReadOnly();
	
}