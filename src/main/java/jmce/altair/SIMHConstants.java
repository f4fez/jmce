/**
   $Id: SIMHConstants.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.altair;

/**
 * Constants rlative to the SIMH simulator.
 */
public interface SIMHConstants
{
	/** Check if has banked memory */
	static public final  int CMD_HAS_BANKED_MEMORY = 18;

	/** Print the current time in ms on the console */
	static public final int CMD_PRINT_TIME = 0;
	
	/** Set the current bank */
	static public final int CMD_SET_BANK = 12;

	/** Get the current bank */
	static public final int CMD_GET_BANK = 11;

	/** Reset SIMH device */
	static public final int CMD_SIMH_RESET = 14;

	/** Start a new timer */
	static public final int CMD_START_TIMER = 1;

	/** Stop the last timer */
	static public final int CMD_STOP_TIMER = 2;

	/** Sleep */
	static public final int CMD_SLEEP = 27;

	/** Get version */
	static public final int CMD_VERSION = 6;
	
	/** Port to send / recv command */
	static public final  int SIMH_PORT = 0xfe;


	/** Internal NONE command */
	static public final int CMD_NONE = -1;
	
	/** Return the base of common memory */
	static public final int CMD_MMU_COMMON = 13;
	
	/** Interface version */
	static final String SIMH_VERSION = "SIMH003";

}
