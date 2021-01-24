/**
   $Id: SerialFile.java 596 2011-05-24 07:12:27Z mviara $

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

package jmce.sim.serial;

import java.io.*;

import jmce.sim.*;
import jmce.sim.Serial;
import jmce.util.Logger;

/**
 * Serial interface to file.
 * <p>
 * This class implements a serial interface without input and with the
 * output to a file.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class SerialFile extends Serial implements DeviceConsumer<Integer>
{
	private static final Logger log = Logger.getLogger(SerialFile.class);
	private String filename = "";
	private OutputStream os = null;

	public SerialFile()
	{
		this("serial.txt");
	}

	public SerialFile(String name)
	{
		setFileName(name);
	}
	

	@Override
	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);


		log.info("Serial to "+filename);
		
		try
		{
			os = new FileOutputStream(filename);
		}
		catch (java.io.FileNotFoundException e)
		{
			throw new SIMIOException(filename," Not found");
		}
	
		addInputConsumer(this);
	}

	public void consume(Integer c) throws SIMException
	{
		try
		{
			if (os != null)
				os.write(c);
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(filename," Writing");
		}

	}

	/**
	 * Set the name of the file used as output
	 */
	public void setFileName(String s)
	{
		filename = s;
	}

	/**
	 * Return the name of the file used as output
	 */
	public String getFileName()
	{
		return filename;
	}

	public String toString()
	{
		return "SerialFile "+filename;
	}
			       
}
