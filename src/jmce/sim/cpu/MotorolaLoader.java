/**
   $Id: MotorolaLoader.java 589 2011-05-18 16:42:27Z mviara $

   Copyright (c) 2011, Mario Viara

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
import jmce.util.Logger;

/**
 * Motorola S19 file loader.<p>
 * 
 * This class is not fully tested. For now only 1 and 9 record type are
 * supported.
 *
 * @author Mario Viara
 *
 * @since 1.01
 */
public class MotorolaLoader extends AbstractLoader
{
	private static final Logger log = Logger.getLogger(MotorolaLoader.class);
	
	public MotorolaLoader(String ext)
	{
		super(ext);
	}

	public void load(Memory m,String name,int base,LoadInfo info) throws SIMException
	{
		try
		{
			int lenAddress = 4;
			
			BufferedReader rd = new BufferedReader(new FileReader(name));
			String line;
			int start = -1;
			int end = -1;

			while ((line = rd.readLine()) != null)
			{
				if (!line.startsWith("S"))
					throw new SIMIOException(name," is not a valid intel file");

				int type	= Hex.getDigit(line,1);
				int lenData	= Hex.getByte(line,2);
				int address	= Hex.getHex(line,4,lenAddress);

				
				log.info("Type "+type+" len "+lenData);

				
				int chksum = 0;
				
				for (int i = 0 ; i < lenData+1 ; i++)
				{
					chksum += Hex.getByte(line,2+i*2);
				}					
				
				lenData -= lenAddress / 2 + 1;
				
				chksum &= 0xff;

				if (chksum != 0xff)
					throw new SIMException("Invalid chksum "+Hex.formatByte(chksum)+" in "+line);

				if (type == 9)
					break;

				if (type != 1)
					throw new SIMException("Unsupported record type "+type);

				if (address < start || start == -1)
					start = address;
				if (address + lenData - 1 > end || end == -1)
					end = address +lenData -1;
				log.info("Load "+lenData+" at "+Hex.formatWord(address));
				for (int i = 0 ; i < lenData ; i++)
					m.setMemory(address+i,Hex.getByte(line,4+lenAddress+i*2));
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

