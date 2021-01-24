/**
   $Id: YazeConstants.java 371 2010-09-28 01:41:15Z mviara $

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

public interface YazeConstants
{
	public final int CONSTA = 0;
	public final int CONDAT = 1;
	public final int TERMINATE = 255;
	public final int MMU_PAGE_LOW = 20;
	public final int MMU_PAGE_HI  = 21;
	public final int MMU_PAGE_FRAME = 22;
	public final int FDC_DRIVE		= 10;
	public final int FDC_TRACK_LOW	= 11;
	public final int FDC_TRACK_HI	= 12;
	public final int FDC_SECTOR	= 13;
	public final int FDC_CMD		= 14;
	public final int FDC_STATUS		= 15;
	public final int FDC_DMA_LOW		= 16;
	public final int FDC_DMA_HI		= 17;

	/**
	 * FDC Command READ
	 */
	static public final int FDC_CMD_READ		= 0;

	/**
	 * FDC Command write
	 */
	static public final int FDC_CMD_WRITE		= 1;

	/**
	 * FDC Status ok
	 */
	static public final int FDC_STATUS_SUCCESS	= 0;

	/**
	 * FDC Status error
	 */
	static public final int FDC_STATUS_ERROR		= 1;

	static public final int CCP_LENGTH	= 0x800;
	static public final int BDOS_LENGTH	= 0xe00;
	static public final int CPM_LENGTH	= (CCP_LENGTH + BDOS_LENGTH);

}

