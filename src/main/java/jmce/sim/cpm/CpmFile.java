/**
   $Id: CpmFile.java 487 2010-12-24 10:04:23Z mviara $

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
package jmce.sim.cpm;


import java.util.Vector;

/**
 * CP/M file rappresentation.
 * <p>
 * The file is very simple only hold the user number, the file name and
 * 2 array one with the allocated data block number and one with the
 * allocated FCB number.
 * <p>
 * This class rappresent a CP/M file.
 *
 * @author Mario Viara
 * @version 1.01
 *
 */
public class CpmFile
{
	/** User number of this file */
	int user;

	/** File name (8=3 length) */
	String name;

	/** Vector with allocated data lock */
	Vector<Integer> blocks = new Vector<Integer>();

	/** Vector with allocated fcb entry */
	Vector<Integer> fcbs = new Vector<Integer>();


	/** File attribute */
	private boolean fifo,global,archive,readonly;

	/**
	 * Constructor for new file.
	 *
	 * @param user - User number
	 * @param name - File name
	 */
	public CpmFile(int user,String name)
	{
		this.user = user;
		this.name = name;

		/** Clear all attribute */
		fifo = global = archive = readonly = false;
	}

	/**
	 * Add a new data block to the file.
	 *
	 * @param block - Block number
	 */
	public void addBlock(int block)
	{
		blocks.add(block);
	}

	/**
	 * Return the file name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Return the number of allocated data block
	 */
	public int getBlockCount()
	{
		return blocks.size();
	}

	/**
	 * Return the specified data block number
	 */
	public int getBlockAt(int i)
	{
		return blocks.get(i);
	}

	/**
	 * Add a new FCB to the file.
	 */
	public void addFCB(int fcb)
	{
		fcbs.add(fcb);
		
	}

	/**
	 * Return the number of allocated FCBs
	 */
	public int getFCBCount()
	{
		return fcbs.size();
	}

	/**
	 * Return the specified FCB number.
	 */
	public int getFCBAt(int i)
	{
		return fcbs.get(i);
	}


	/**
	 * Return the CP/M user number 0 .. 15
	 */
	public int getUser()
	{
		return user;
	}
	


	/**
	 * Set the fifo attribute.
	 *
	 * @since 1.01
	 */
	public void setAttributeFifo(boolean mode)
	{
		fifo = mode;
	}

	/**
	 * Return the fifo attribute.
	 *
	 * @since 1.01
	 */
	public boolean getAttributeFifo()
	{
		return fifo;
	}

	/**
	 * Set the read only attribute.
	 *
	 * @since 1.01
	 */
	public void setAttributeReadOnly(boolean mode)
	{
		readonly = mode;
	}

	/**
	 * Return the read only attribute.
	 *
	 * @since 1.01
	 */
	public boolean getAttributeReadOnly()
	{
		return readonly;
	}


	/**
	 * Set the global attribute.
	 *
	 * @since 1.01
	 */
	public void setAttributeGlobal(boolean mode)
	{
		global = mode;
	}

	/**
	 * Return the global attribute.
	 *
	 * @since 1.01
	 */
	public boolean getAttributeGlobal()
	{
		return global;
	}

	/**
	 * Set the archive attribute.
	 *
	 * @since 1.01
	 */
	public void setAttributeArchive(boolean mode)
	{
		archive = mode;
	}

	/**
	 * Get the archive attribute.
	 *
	 * @since 1.01
	 */
	public boolean getAttributeArchive()
	{
		return archive;
	}
	

	public String toString()
	{
		return "CP/M "+name+" User="+user+"FCB="+getFCBCount();
	}

	
}

