/**
   $Id: SpectrumConstants.java 624 2011-06-01 17:18:43Z mviara $

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
package jmce.sinclair.spectrum;

/**
 * Constants relative to Spectrum
 *
 * @author Mario Viara
 * @version 1.00
 */
public interface SpectrumConstants
{
	/**
	 * Memory map spectrum 48K
	 */
	public static final int ROM_MEMORY_END			= 0x3FFF;
	public static final int SCREEN_MEMORY_SIZE		= 0x1800;
	public static final int SCREEN_MEMORY_START		= 0x0000;
	public static final int SCREEN_MEMORY_END		= 0x17FF;
	public static final byte ATTRIBUTE_FLASH		= (byte)0x80;
	public static final byte ATTRIBUTE_BRIGHT		= (byte)0x40;
	
	public static final int SCREEN_ATTRIBUTE_START	= 0x1800;
	public static final int SCREEN_ATTRIBUTE_END	= 0x1AFF;
	public static final int SCREEN_ATTRIBUTE_SIZE	= 0x0300;

	/**
	 * Memory manager 128K
	 */
	public static final int MMU_VIDEO	= (1 << 3);
	public static final int MMU_ROM		= (1 << 4);
	public static final int MMU_DISABLE = (1 << 5);
	public static final int MMU_PORT	= 0x7ffd;

	/**
	 * ULA Interface
	 */
	public static final int ULA_PORT	= 0xFE;

	/** Output */
	public static final int ULA_BORDER	= 0x07;
	public static final int ULA_MIC		= 0x08;
	public static final int ULA_OUT		= 0x10;

	/** Input */
	public static final int ULA_IN		= 0x40;
	public static final int ULA_KBD		= 0x1F;
	
}
