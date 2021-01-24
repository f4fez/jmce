/**
   $Id: Ports.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce.intel.mcs51;

import jmce.sim.*;

/**
 * Standard MCS51 ports.
 *
 *<p>
 * This implements the standard 8051 , it work as a container for the
 * class {@link Port} that  really implements a single port.
 * 
 * @author Mario Viara
 * @version 1.00
 */
public class Ports extends AbstractPeripheral
{
	private int disableMasks[];
	private int numPort;

	public Ports(int n)
	{
		setName("Ports");
		setNumPort(n);
	}

	public Ports()
	{
		this(0);
	}
	
	public void setNumPort(int n)
	{
		this.numPort = n;
		disableMasks = new int[n];
	}

	public int getNumPort()
	{
		return numPort;
	}

	public Port getPort(int n)
	{
		return (Port)getHardware(Port.class,n);
	}
	
	public void init(Hardware parent) throws SIMException
	{
		for (int i = 0 ; i < numPort ; i++)
		{
			Hardware p = getHardware(Port.class,i);
			if (p == null)
				addHardware(new Port(i));
		}

		// Now the disable mask can be applied
		for (int i = 0 ; i < numPort ; i++)
		{
			Port p = getPort(i);
			p.setDisableMask(disableMasks[i]);
		}
		
		super.init(parent);
	}

	public void setDisableMask(int n,int mask)
	{
		disableMasks[n] = mask;
	}

	public int getSfrP(int n)
	{
		return Port.sfrPorts[n];
	}

	public int getSfrM1(int n)
	{
		return Port.sfrM1s[n];
	}

	public int getSfrM2(int n)
	{
		return Port.sfrM2s[n];
	}

	public String toString()
	{
		return "Port 0-"+numPort;
	}
}
