/**
   $Id: INTZ80.java 792 2012-02-14 20:06:36Z mviara $
   
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
package jmce.zilog.z80;


import jmce.sim.SIMException;
import jmce.util.Hex;

/**
 *
 * Manage the interrupt in Z80.<p>
 * <p>
 *
 * Supported mode :
 * <ul>
 *  <li>Interrupt mode 0 not supported.</li>
 *  <li>Interrupt mode 1 Restart 38 supported.</li>
 *  <li>Interrupt mode 2 Vector supported.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.01
 */
public class INTZ80 extends jmce.intel.i8080.INT
{
	private Z80 z80;
	
	/**
	 * Standard constructor.
	 */
	public INTZ80(Z80 z80,String name) throws SIMException
	{
		super(z80,name);
		this.z80 = z80;
		setEnabled(true);
	}

	
	public final int getVector() throws SIMException
	{
		
		if ((vector & 0x01) != 0)
			throw new SIMException("Invalid vector "+Hex.formatByte(vector));
		
		switch (z80.im())
		{
			default:
			case	0:
				throw new SIMException("IM0 not supported");

			case	1:
				return (0x38);

			case	2:
				int a = ((z80.I() ) << 8) | vector;
				int w = z80.getWord(a);
				return w;
				
		}

	}


	public String toString()
	{
		switch (z80.im())
		{
			case	0:
				return super.toString()+" IM0";

			case	1:
				return super.toString()+" IM1 (38)";

			case	2:
				try
				{
					return super.toString()+" IM2 V="+Hex.formatByte(getVector());
				}
				catch (SIMException ignore)
				{
					return "IM2 V=xx";
				}
				
		}

		return super.toString();
	}
	
}
