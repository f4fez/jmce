/**
   $Id: SerialRXTX.java 596 2011-05-24 07:12:27Z mviara $

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
 * Serial port using RXTXLibrary.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class SerialRXTX extends RXTX 
{
	public SerialRXTX()
	{
		this("COM1");
	}

	public SerialRXTX(String port)
	{
		setName("RXTXSerial");
		setPortName(port);
	}
	
	protected void registerPort(CommPort port) throws SIMException
	{
		super.registerPort(port);

		connected = true;

	}

	protected void connect() throws Exception
	{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(getPortName());

		if ( portIdentifier.isCurrentlyOwned() )
		{
			throw new SIMIOException(getPortName()," is Busy");
		}

		CommPort p = portIdentifier.open(this.getClass().getName(),2000);

		if (p instanceof SerialPort )
			registerPort(p);
		else
		{
			try
			{
				p.close();
			}
			catch (Exception ex)
			{
			}
			
			throw new SIMIOException(getPortName()," not a serial port!");
		}
	}

	public String toString()
	{
		return "SerialRXTX on "+getPortName();
	}
	
}
