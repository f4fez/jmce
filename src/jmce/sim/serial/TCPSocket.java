/**
   $Id: TCPSocket.java 695 2011-09-21 06:09:11Z mviara $

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

import java.net.*;
import jmce.sim.*;
import jmce.util.Logger;

/**
 * Base class to implement serial over TCP socket.
 * <p>
 * The sub class must implements the method connect and when the socket
 * is connected call the method #setSocket(Socket s) after this
 * automatically the data from the socket are sent to the connected
 * device and the data received from the device are sent to the socket.
 *
 * @author Mario Viara
 * @version 1.01
 */
abstract public class TCPSocket extends  PolledSerial
{
	private static final Logger log = Logger.getLogger(TCPSocket.class);
	
	public static final int DEFAULT_PORT = 2023;
	private Socket socket = null;
	private int port;

	/**
	 * Set the port used by this socket.
	 */
	public void setPort(int n) 
	{
		port = n;
	}

	/**
	 * Return the port used by this socket.
	 */
	public int getPort()
	{
		return port;
	}


	/**
	 * Set the socket.
	 * <p>
	 * Used by sub class to set the socket used for input/output.
	 */
	protected void setSocket(Socket s) throws SIMException
	{
		log.info("Set Socket "+s);
		try
		{
			socket = s;
			is = socket.getInputStream();
			os = socket.getOutputStream();
			connected = true;
		}
		catch (Exception e)
		{
			throw new SIMIOException(toString()," setSocket");
		}
		
	}

	/**
	 * Close the socket and release all the resources.
	 */
	protected synchronized void close()
	{
		super.close();
		
		if (socket != null)
		{
			try
			{
				socket.close();
			}
			catch (Exception ignore)
			{
			}
			socket = null;
		}


	}

}

