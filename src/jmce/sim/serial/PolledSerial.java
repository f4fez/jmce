/**
   $Id: PolledSerial.java 634 2011-06-16 07:49:34Z mviara $

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
import jmce.util.Logger;

/**
 * Abstract base class for serial polled device.
 * <p>
 * This base class is used for all <tt>Serial</tt> implemented in
 * polling mode. One thread is created for each port and the sub class
 * must implements only the method relative to the connection of the
 * port. When connected automatically thru Input/Output stream this
 * class delivery the data to and from the device.
 *
 * @author Mario Viara
 * @version 1.02
 *
 */
abstract public class PolledSerial extends Serial implements Runnable,DeviceConsumer<Integer>
{
	private static final Logger log = Logger.getLogger(PolledSerial.class);
	
	private static final boolean logFine = false;
	private byte buffer[] = new byte[1024];
	private int count;
	private int i;
	protected long sent= 0 ,rcvd=0;
	
	private final int MIN_CONNECT_DELAY = 100;
	private final int MAX_CONNECT_DELAY = 6 * 609 * 1000;
	protected boolean connected = false;

	/** Input stream */
	protected InputStream is = null;

	/** Output stream */
	protected OutputStream os = null;


	/**
	 * Receive data from the input device if the device is
	 * connected the data are delivery to the output stream.
	 */
	public void consume(Integer c) throws SIMException
	{
		if (connected)
		{
			try
			{
				os.write(c);
				sent++;
			}
			catch (Exception e)
			{
				log.info(e);
				close();
			}
		}
	}
	
	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);
		
		Thread t = new Thread(this);
		
		t.start();

	}

	/**
	 * Wait for the specified number of ms. Ignore any exception
	 */
	protected void delay(int delay)
	{
		if (logFine)
			log.fine(this+" delay "+delay);
		
		try
		{
			Thread.sleep(delay);
		}
		catch (Exception ignore)
		{
			log.info(ignore);
		}
	}

	/**
	 * Close the input and output stream and set the connection to
	 * false. All exception are ignored.
	 */
	protected synchronized void close()
	{
		if (connected)
			log.info("Close");

		if (is != null)
		{
			try
			{
				is.close();
			}
			catch (Exception ignore)
			{
				log.info(ignore);
			}
			is = null;
		}

		if (os != null)
		{
			try
			{
				os.close();
			}
			catch (Exception ignore)
			{
				log.info(ignore);
			}
			os = null;
		}


		connected = false;

		removeInputConsumer(this);


	}

	/**
	 * Thread to handle the connection and the input stream.
	 */
	public final void run()
	{

		
		for (;;)
		{

			int delay = MIN_CONNECT_DELAY;

			/**
			 * Wait to be connected
			 */
			while (!connected)
			{
				delay(delay);

				try
				{
					connect();
					delay = MIN_CONNECT_DELAY;
				}
				catch (Exception ex)
				{
					close();
					delay *= 2;
					if (delay > MAX_CONNECT_DELAY)
						delay = MAX_CONNECT_DELAY;
					log.fine(this+" "+ex.toString());

				}


			}

			if (connected)
				log.info("Polling : "+toString());

			addInputConsumer(this);
			
			/**
			 * Handle incoming data while connected.
			 */
			while (connected)
			{
				try
				{
					count = is.read(buffer);
				}
				catch (Exception ex)
				{
					log.info(ex);
					break;
				}

				if (count > 0)
				{
					rcvd += count;
					try
					{
						for (i = 0 ; i < count ; i++)
							write(buffer[i] & 0xff);
					}
					catch (SIMException e)
					{
						log.info(e);
						break;
					}
				}
			}

			close();
		}
	}

	/**
	 * Implemented by sub class.
	 * <p>
	 * Must set the stream to the appropriated value and set
	 * connected true.
	 */
	abstract protected void connect() throws Exception;

}
