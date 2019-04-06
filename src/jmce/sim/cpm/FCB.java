/**
   $Id: FCB.java 632 2011-06-14 11:17:35Z mviara $

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

import java.util.Arrays;

/**
 * This class rappresent a CP/M FCB (Directory file control block.)
 * <p>
 * Directory File Control block
 *
 * Space in CP/M is allocated in 128 byte block called record.<p>
 * 
 * Every FCB is 32 byte and have the following layout :<p>
 * 
 * <pre>
 *
 *  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F
 * UU N0 N1 N2 N3 N4 N5 N6 N7 T0 T1 T2 EX S1 S2 RC
 * A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF
 *
 *
 * UU      User 0 - 15 in CP/M 1.4,2.2 0-31 in CP/M 3.0.
 *	   If the file is deleted this byte is set to 0xE5.
 *	   
 * N0..N7  File name right filled with spaces.
 * T0..T2  File type  right filled with spaces.
 * EX      Number of extension 0-31, every extension is 128 record.
 * S1      Reserved.
 * S2	   Reserved.
 * RC	   Number of record used in the last extension.
 * 
 * A0..AF  Block allocation map 1 if BSM < 256 else each block use 2
 *	   2 consecutive byte LSB first.
 * 
 * </pre>
 * <p>
 *  Turbodos mantain the flag for the file in the high bit of the
 *  filename :
 *  <p>
 *  N0 - Fifo file attribute
 *  T0 - Read only
 *  T1 - Global file
 *  T2 - Archive
 *  <p>
 *  
 * @see DPB
 *
 * @version 1.01
 * @author Mario Viara
 */
public class FCB
{
	static final byte b00 = 0x00;
	
	/** FCB length */
	static public final int SIZE = 32;
	
	/** Entry deleted  */
	static public final byte DELETED = (byte)0xe5;

	/** Offset to user number */
	static public final int USER	= 0;

	/** Offset to extension number */
	static public final int EX		= 12;

	/** Offset to record counter */
	static public final int RC		= 15;

	/** Byte image of FCB */
	private byte fcb[] = new byte[SIZE];

	/**
	 * Create a new empty FCB
	 */
	public FCB()
	{
		clear();
		
	}

	/**
	 * Clear all the data in the FCB
	 */
	public void clear()
	{
		Arrays.fill(fcb,b00);
	}

	/**
	 * Copy in one buffer the FCB
	 */
	public void getBuffer(byte buffer[],int from)
	{
		System.arraycopy(this.fcb,0,buffer,from,SIZE);
	}
	
	/**
	 * Copy the FCB from one buffer
	 */
	public void setBuffer(byte buffer[],int from)
	{
		System.arraycopy(buffer,from,this.fcb,0,SIZE);
	}
	
	/**
	 * Get the space used by the FCB
	 */
	public byte[] getBytes()
	{
		return fcb;
	}

	/**
	 * Get the user number
	 */
	public int getUser()
	{
		return fcb[USER] & 0xff;
	}

	/**
	 * Set the user number
	 */
	public void setUser(int user)
	{
		fcb[USER] = (byte)user;
	}

	/**
	 * Set the filename in the FCB
	 */
	public void setFileName(String name)
	{
		int i,j;

		Arrays.fill(fcb,1,12,(byte)' ');

		for (i = 0,j = 0 ; i < name.length() ; i++)
		{
			char c = name.charAt(i);
			if (c == '.')
			{
				j = 8;
				continue;
			}
			fcb[1+j++] = (byte)c;

		}

	}

	/**
	 * Get the filename in the FCB
	 */
	public String getFileName()
	{
		StringBuffer sb = new StringBuffer();
		char c;
		
		for (int i = 0 ; i < 8 ; i++)
		{
			c = (char)fcb[i+1];
			c &= 0x7f;
			
			if (c != ' ')
				sb.append(c);
		}		
		sb.append('.');

		for (int i =0 ; i < 3 ; i++)
		{
			c = (char)fcb[i+9];
			c &= 0x7f;
			
			if (c != ' ')
				sb.append(c);
		}

		String fileName = sb.toString();

		if (fileName.endsWith("."))
			fileName = fileName.substring(0,fileName.length() -1);

		return fileName;
	}

	/**
	 * Set this FCB as deleted
	 */
	public void setDeleted()
	{
		fcb[0] = DELETED;
	}

	/**
	 * Return true if this FCB is deleted
	 */
	public boolean isDeleted()
	{
		return fcb[USER] == DELETED ? true : false;
	}

	/**
	 * Get record counter
	 */
	public int getRC()
	{
		return fcb[RC] & 0xff;
	}

	/**
	 * Set record counter
	 */
	public void setRC(int rc)
	{
		fcb[RC] = (byte)rc;
	}

	/**
	 * Get the extension
	 */
	public int getEX()
	{
		return fcb[EX] & 0xff;
	}

	/**
	 * Set the extension
	 */
	public void setEX(int ex)
	{
		fcb[EX] = (byte)ex;
	}

	/**
	 * Get one allocation block using 1 byte for block
	 */
	public int getBlockByte(int block)
	{
		return fcb[16+block] & 0xff;
	}

	/**
	 * Get one allocation block using 1 word for block
	 */
	public int getBlockWord(int block)
	{
		return (fcb[16+block*2+0] & 0xff)+
			   ((fcb[16+block*2+1]) & 0xff) * 256;
	}

	public void setBlockByte(int block,int value)
	{
		fcb[16+block] = (byte)value;
	}

	public void setBlockWord(int block,int value)
	{
		setBlockByte(block*2+0,value & 0xff);
		setBlockByte(block*2+1,value >> 8);
	}

	/**
	 * Clear all the allocation block map
	 */
	public void clearBlocks()
	{
		Arrays.fill(fcb,16,32,b00);
	}

	/**
	 * Return true if the turbodos Fifo is set
	 */
	public boolean getAttributeFifo()
	{
		return (fcb[1] & 0x80) != 0;
	}

	/**
	 * Return true if the turbodos read only is set.
	 */
	public boolean getAttributeReadOnly()
	{
		return (fcb[9] & 0x80) != 0;
	}

	/**
	 * Return true if the turbodos global attribute is set.
	 */
	public boolean getAttributeGlobal()
	{
		return (fcb[10] & 0x80) != 0;
	}

	/**
	 * Return true if the turbodos global attribute is set.
	 */
	public boolean getAttributeArchive()
	{
		return (fcb[11] & 0x80) != 0;
	}
	
}
