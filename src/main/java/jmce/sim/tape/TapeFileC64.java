/**
   $Id: TapeFileC64.java 628 2011-06-08 09:57:43Z mviara $

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

package jmce.sim.tape;


import java.io.*;

import jmce.sim.TapePulse;

/**
 * Tape file decoder for commodore .TAP file.
 * <p>
 *
 * @author Mario Viara
 * 
 * @version 1.00
 * @since 1.01
 */
public class TapeFileC64 implements TapeFileDecoder
{
	static public final int MULTIPLIER = 8;
	
	private int version;

	public boolean isFileSupported(InputStream is) throws java.io.IOException
	{
		byte header[] = new byte[20];

		if (is.read(header) != 20)
			return false;

		String signature = new String(header,0,12);
		
		if (!signature.equals("C64-TAPE-RAW"))
			return false;
		
		version = header[12] & 0xff;

		return true;
	}

	public TapeData decode(BufferedInputStream is) throws java.io.IOException
	{
		TapeData data =  new TapeData(985248);


		for (;;)
		{
			int v = is.read();
			if (v == -1)
				break;
			
				  

			if (v == 0)
			{
				if (version == 0)
					v = 256;
				else
				{
					int b1 = is.read() & 0xff;
					int b2 = is.read() & 0xff;
					int b3 = is.read() & 0xff;

					v = (b1 + b2 * 256 + b3 * 256 * 256) / MULTIPLIER;
					if (v > 100000)
						v = 100000;
				}


			}
		
			data.add(new TapePulse(v*MULTIPLIER));

		}

		return data;
	}

	public String toString()
	{
		return "C64 1.00";
	}
	
}
