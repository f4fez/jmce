/**
   $Id: Port.java 694 2011-09-02 12:01:08Z mviara $

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
 * Standard 8051 I/O port.
 * <p>
 * Each I/O port in the 8051 have 3 register Px,PxM1 and PxM2. Px is
 * the data register and PxM1 and PxM1 determine the data direction for
 * any bit. Some implementation of 8051 can have limitation on some bit
 * and not all bit can be used in all mode.
 * <p>
 * Up to 8 port are supported but only P0-3 are configured if one
 * processor want to use port P4-7 must configure the register with the
 * static method provided.
 *
 * <h2>I/O Port configuration</h2>
 * <p>
 * <pre>
 * Each nit in the data port (Px) have 2 bit to register (PxM1,PxM2) to
 * define  the mode.
 * 
 * PxM1 PxM2
 *  0    0   Quasi bidirectional.
 *  0    1   Push pull.
 *  1    0   Input only.
 *  1    1   Open drain.
 *  
 *</pre>
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Port extends AbstractPeripheral implements MCS51Constants,MemoryWriteListener
{
	/* Up to 8 ports are supported but only 4 have register defined */
	public static int sfrPorts[]	= {P0,P1,P2,P3,-1,-1,-1,-1,-1,-1};
	public static int sfrM1s[]	= {P0M1,P1M1,P2M1,P3M1,-1,-1,-1,-1,-1,-1};
	public static int sfrM2s[]	= {P0M2,P1M2,P2M2,P3M2,-1,-1,-1,-1,-1,-1};
	
	private int sfrPort,sfrM1,sfrM2;
	private int port,m2,m1;
	private int disableMask;
	private int portNum;

	/**
	 * Default constructor
	 */
	public Port()
	{
		this(0);
	}

	/**
	 * Constructor with port number.
	 *
	 * @param port = Port number 0 .. 7
	 */
	public Port(int port)
	{
		setPortNum(port);
	}

	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		if (sfrPort != -1)
			cpu.addIOWriteListener(sfrPort,this);
		if (sfrM1 != -1)
			cpu.addIOWriteListener(sfrM1,this);
		if (sfrM2 != -1)
			cpu.addIOWriteListener(sfrM2,this);
	}

	public void setPortNum(int n)
	{
		setName("Port"+n);
		this.portNum = n;
		this.sfrPort = sfrPorts[n];
		this.sfrM1 = sfrM1s[n];
		this.sfrM2 =sfrM2s[n];
	}

	public int getPortNum()
	{
		return portNum;
	}
	
	public void setDisableMask(int mask)
	{
		disableMask = mask;
	}

	public int getDisableMask()
	{
		return disableMask;
	}

	protected void setPort(int value)
	{
		this.port = value;
	}
	

	protected void setM1(int value)
	{
		this.port = value;
	}

	protected void setM2(int value)
	{
		this.port = value;
	}

	protected int getPort()
	{
		return port;
	}

	protected int getM1()
	{
		return m1;
	}

	protected int getM2()
	{
		return m2;
	}
	
	public void writeMemory(Memory m,int sfr,int value,int oldValue) throws SIMException
	{
		if (sfr == sfrPort)
			setPort(value);
		else if (sfr == sfrM1)
			setM1(value);
		else if (sfr == sfrM2)
			setM2(value);
	}

	public int readMemory(Memory m,int sfr,int v) throws SIMException
	{
		if (sfr == sfrPort)
			return getPort();
		else if (sfr == sfrM1)
			return getM1();
		else if (sfr == sfrM2)
			return getM2();
		else
			return 0;
	}


}



		   