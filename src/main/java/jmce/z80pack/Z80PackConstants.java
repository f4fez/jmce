/**
   $Id: Z80PackConstants.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.z80pack;

/**
 * Constants for Z80Pack system.
 *
 * @author Mario Viara
 * @version 1.00
 */
public interface Z80PackConstants
{
	/** Printer status 00 not ready FF ready */
	public final int PRTSTA = 2;

	/** Printer data port */
	public final int PRTDAT = 3;
	
	// Console status return 0 if no data is available
	public final int CONSTA = 0;

	// Console data R/W the console.
	public final int CONDAT = 1;
	public final int TERMINATE = 255;

	/** FDC drive register */
	public final int FDC_DRIVE		= 10;

	/** FDC track register */
	public final int FDC_TRACK		= 11;

	/** FDC sector register */
	public final int FDC_SECTOR		= 12;

	/** FDC command register */
	public final int FDC_CMD		= 13;

	/** FDC status register */
	public final int FDC_STATUS		= 14;

	/** FDC Dma low register */
	public final int FDC_DMA_LOW		= 15;

	/** FDC dma hi register */
	public final int FDC_DMA_HI		= 16;

	/** FDC sector hi register */
	public final int FDC_SECTOR_HI	= 17;
	
	/**
	 * FDC Command READ
	 */
	static public final int FDC_CMD_READ	= 0;

	/**
	 * FDC Command write
	 */
	static public final int FDC_CMD_WRITE	= 1;

	/**
	 * FDC Status ok
	 */
	static public final int FDC_STATUS_SUCCESS	= 0;

	/**
	 * FDC Status error
	 */
	static public final int FDC_STATUS_ERROR	= 1;

	// Initialize the MMU
	static public final int MMU_INIT =	20;

	// Select Bank
	static public final int MMU_BANK =	21;

	// Set segment size in 256 bytes
	static public final int MMU_SEGMENT =	22;

	/**
	 * Delay control.
	 * <p>
	 * Write at rhis port delay the CPU 10 x the specified number
	 * of ms.
	 */
	public final int DELAY_CTRL = 28;
	
	/**
	 * Timer control.
	 *
	 * Read / write port a value different of 0 enable one
	 * interrupt timer every 10 ms.
	 */
	static public final int TIMER_CTRL = 27;

	/**
	 * Network interface # 0 base address
	 */
	static public final int NETWORK_SERVER_BASE_1 = 40;
	static public final int NETWORK_SERVER_BASE_2 = 42;
	static public final int NETWORK_SERVER_BASE_3 = 44;
	static public final int NETWORK_SERVER_BASE_4 = 46;
	static public final int NETWORK_CLIENT_BASE_1 = 50;
}
