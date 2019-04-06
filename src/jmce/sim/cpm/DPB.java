/**
   $Id: DPB.java 860 2012-05-27 13:32:20Z mviara $

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

/**
 * CP/M Disk parameter block.
 *
 * <p>
 * This class rappresent a CP/M 2.2 Disk Parameter Block.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 */
public class DPB
{
	/** Sector size for CP/M 2.2 disk */
	static public final int SECSIZ = 128;
	
	/** Number of 128 bytes sector per track. (2 bytes) AT 0 */
	static public final int SPT = 0;

	/** Block shift factor. Ex 3 Block size is 128 << 3 == 1024.AT
	 2 */
	static public final int BSH = 2;
	
	/** Data allocation block mask.  (2 ^ BSH) - 1. AT 03 */
	static public final int BLM = 3;
	
	/** Extent mask number of extents per directory. AT 04 */
	static public final int EXM = 4;
	
	/** Total number of block -1. 2 Bytes AT 05 */
	static public final int DSM = 5;
	
	/** Total number of directory - 1. 2 Bytes AT 07 */
	static public final int DRM = 7;
	
	/** Directory allocation map byte 0 AT 09 */
	static public final int AL0 = 9;
	
	/** Directory allocation map byte 0 AT 0A */
	static public final int AL1 = 10;
	
	/** Number of checked sector for disk change. 2 Bytes AT 0B */
	static public final int CHS = 11;
	
	/** Number of reserved track. 2 Byte at 0D */
	static public final int OFF = 13;

	private int translate[] = null;
	
	int spt;

	int bsh;

	int blm;

	int exm;

	int dsm;

	int drm;

	int al0;
	
	int al1;

	int cks;

	int off;


	public DPB(int spt,int bsh,int blm,int exm,int dsm,int drm,int al0,int al1,int cks,int off)
	{
		   this.spt=spt;
		   this.bsh=bsh;
		   this.blm=blm;
		   this.exm=exm;
		   this.dsm=dsm;
		   this.drm=drm;
		   this.al0=al0;
		   this.al1=al1;
		   this.cks=cks;
		   this.off=off;
	}
	
	/**
	 * Return the number of 128 bytes sector x track
	 */
	public int getSPT()
	{
		return spt;
	}

	/**
	 * Return the total number of block.
	 */
	public int getDSM()
	{
		return dsm;

	}

	/**
	 * Get number of directory - 1
	 */
	public int getDRM()
	{
		return drm;
	}

	public int getBSH()
	{
		return bsh;
	}
	

	public int getOFF()
	{
		return off;
	}

	public void setTranslation(int vector[])
	{
		translate = vector;
	}
	
	public int translateSector(int s)
	{
		if (translate == null)
			return s;
		return translate[s-1];
	}

	public int getAL0()
	{
		return al0;
	}

	public int getAL1()
	{
		return al1;
	}

	public int getBLM()
	{
		return blm;
	}

	public int getTRK()
	{
		int t = (getDSM()+1) << getBSH();
		
		t += getSPT() - 1;

		t = t/getSPT() + getOFF();

		return t;

	}

	public int getEXM()
	{
		return exm;
	}
}
