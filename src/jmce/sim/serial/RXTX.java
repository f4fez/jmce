/**
   $Id: RXTX.java 596 2011-05-24 07:12:27Z mviara $

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

import gnu.io.*;

import jmce.sim.*;

/**
 * Generic communication port using RXTXLibrary.
 * 
 * <p>The binary version of JMCE contains the RXTXComm.jar and the
 * shared library for WIN32 and Linux i686. for other system copy the
 * correct shared library in the directory <code>\bin</code> in the
 * installation root directory.
 * If some think is not working check the documentation for your system
 * on the <a href="http://www.rxtx.org">RXTX</a> website.
 *
 * @author Mario Viara
 * @version 1.00
 */
abstract public class RXTX extends PolledSerial
{
	private String portName = null;
	private CommPort port = null;

	RXTX()
	{
		setName("RXTX");
	}

	public void setPortName(String name)
	{
		this.portName = name;
	}

	public String getPortName()
	{
		return portName;
	}
	
	protected void registerPort(CommPort port) throws SIMException
	{
		this.port = port;
		try
		{
			is = port.getInputStream();
			os = port.getOutputStream();
		}
		catch (java.io.IOException e)
		{
			throw new SIMException("registerPort "+portName);
		}
	}


	protected synchronized void close()
	{
		super.close();
		try
		{
			if (port != null)
				port.close();
		}
		catch (Exception ignore)
		{
		}

		port = null;
	}
			
}
