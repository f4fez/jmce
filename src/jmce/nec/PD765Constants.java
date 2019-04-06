/**
   $Id: PD765Constants.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.nec;

/**
 * 
 * NEC 765 A / B Floppy disk controller constants.
 * <p>
 * Define all constants and default value for NEC 765 floppy disk
 * controller.
 *
 * @author Mario Viara
 * @version 1.00
 * 
 */
public interface PD765Constants
{
	/**
	 * FDC Main status register (RO)
	 */
	public final int MSR = 0;

	/** MSR Busy drive # 0 */
	public final int MSR_BUSY_DRIVE0 = 0x01;

	/** MSR Busy drive # 1 */
	public final int MSR_BUSY_DRIVE1 = 0x02;

	/** MSR Busy drive # 2 */
	public final int MSR_BUSY_DRIVE2 = 0x04;

	/** MSR Busy drive # 3 */
	public final int MSR_BUSY_DRIVE3 = 0x08;

	/** MSR FDC busy */
	public final int MSR_BUSY	= 0x10;

	/** MSR EXM Set during execution of non dma command */
	public final int MSR_EXM	= 0x20;

	/** MSR DIO Data transfer direction 1 ==> cpu */
	public final int MSR_DIO	= 0x40;


	/** MSR Ready */
	public final int MSR_READY	= 0x80;



	/**
	 * FDC Data register R/W
	 */
	public final int DATA = 1;

	/** Status register # 0 */

	/** Normal termination */
	public final int ST0_IC_NT	= 0x00;

	/** Abnormal termination */
	public final int ST0_IC_AT	= 0x40;

	/** Abnormal termination disk change state */
	public final int ST0_IC_AC	= 0xc0;

	/** Invalid command */
	public final int ST0_IC_IC	= 0x80;

	/** Seek end */
	public final int ST0_SE		= 0x20;

	/** Equipement check */
	public final int ST0_EC		= 0x10;

	/** Disk not ready */
	public final int ST0_NR		= 0x08;

	/** Head address */
	public final int ST0_HA		= 0x04;

	/** Drive unit select 1 */
	public final int ST0_S1		= 0x02;

	/** Drive unit select 0 */
	public final int ST0_S0		= 0x01;


	/** Command Sense Interrupt Status */
	public final int CMD_SIS	= 0x08;

	/** Command recalibrate */
	public final int CMD_RECALIBRATE= 0x07;
}
