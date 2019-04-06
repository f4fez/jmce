/**
   $Id: TCPClient.java 596 2011-05-24 07:12:27Z mviara $

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
import jmce.util.Logger;

/**
 * Sample client device.
 * 
 * <p>This class connect the socket to specified host and port.
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class TCPClient extends  TCPSocket
{
	private static Logger log = Logger.getLogger(TCPClient.class);
	private String host;

	/**
	 * Constructor with specified host and port
	 */
	public TCPClient(String host,int port)
	{
		setHost(host);
		setPort(port);
	}

	/**
	 * Constructor with specified port the local host is used.
	 */
	public TCPClient(int port)
	{
		setPort(port);
		setHost("127.0.0.1");
	}

	/**
	 * Default constructor
	 */
	public TCPClient()
	{
		setPort(DEFAULT_PORT);
		setHost("127.0.0.1");
	}


	/**
	 * Set the host
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Get the host
	 */
	public String getHost()
	{
		return host;
	}
	
	
	protected void connect() throws Exception
	{
		log.info("Connecting  to "+host+":"+getPort());
		Socket s = new Socket(host,getPort());
		log.info("Connected with "+s);
		setSocket(s);
	}

	public String toString()
	{
		return "TCPClient "+host+":"+getPort()+" Sent="+sent+",Rcvd="+rcvd;
	}
	
}

