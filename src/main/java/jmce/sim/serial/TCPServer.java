/**
   $Id: TCPServer.java 510 2011-01-18 09:25:07Z mviara $

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
 * Sample server device over TCP.
 * <p>
 * This device listen on the specified port (#DEFAULT_PORT) for incoming
 * connection and send/receive data to the connected device.
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class TCPServer extends  TCPSocket
{
	
	private static Logger log = Logger.getLogger(TCPServer.class);
	private ServerSocket ss = null;

	/**
	 * Default constructor
	 */
	public TCPServer()
	{
		this(DEFAULT_PORT);
	}

	/**
	 * Constructor with specific port
	 */
	public TCPServer(int port)
	{
		setPort(port);

	}


	protected void close()
	{
		super.close();

		if (ss != null)
		{
			try
			{
				ss.close();
			}
			catch (Exception ex)
			{
			}

			ss = null;
		}

	}
	
	protected void connect() throws SIMException
	{
		log.info(" Wait for connection on port "+getPort());

		try
		{
			ss = new ServerSocket(getPort());
				
			Socket s = ss.accept();
			log.info("Connected with "+s);
			setSocket(s);
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(toString()," accept");
		}
		
	}

	public String toString()
	{
		return "TCPServer on "+getPort()+" Sent="+sent+",Rcvd="+rcvd;
	}

}

