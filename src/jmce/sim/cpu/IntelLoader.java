/**
   $Id: IntelLoader.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.sim.cpu;

import java.io.*;

import jmce.util.Hex;
import jmce.sim.*;

/**
 * Intel file format loader.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class IntelLoader extends AbstractLoader
{
	public IntelLoader(String ext)
	{
		super(ext);
	}

	public void load(Memory m,String name,int base,LoadInfo info) throws SIMException
	{
		try
		{
		BufferedReader rd = new BufferedReader(new FileReader(name));
		String line;
		int start = -1;
		int end = -1;

		while ((line = rd.readLine()) != null)
		{
			if (!line.startsWith(":"))
				throw new SIMIOException(name," is not a valid intel file");


			int lenData	= Hex.getByte(line,1);
			int address	= Hex.getWord(line,3);
			int type	= Hex.getByte(line,7);

			int chksum = lenData + address/256+ address + type;

			for (int i = 0 ; i < lenData + 1; i++)
				chksum += Hex.getByte(line,9+i*2);
			chksum &= 0xff;

			if (chksum != 0)
				throw new SIMException("Invalid chksum "+Hex.formatByte(chksum)+" in "+line);

			if (type == 1)
				break;
			if (type == 3)
				continue;

			if (type != 0)
				throw new SIMException("Unsupported record type "+type);

			if (address < start || start == -1)
				start = address;
			if (address + lenData - 1 > end || end == -1)
				end = address +lenData -1;
			for (int i = 0 ; i < lenData ; i++)
				m.setMemory(address+i,Hex.getByte(line,9+i*2));
		}

		info.start = start;
		info.end = end;

		rd.close();
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(name," Reading");
		}


	}

}

