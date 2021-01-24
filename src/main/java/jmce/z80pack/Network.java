/**
   $Id: Network.java 632 2011-06-14 11:17:35Z mviara $

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
package jmce.z80pack;

import jmce.sim.*;
import jmce.util.Hex;

/**
 * Network interface.
 * <p>
 * 
 * Each network interface use a pair of consecutive register the first
 * one  indicate a status and the second one as data. Bit 0 of status
 * indicate data ready for read and bit 1 indicate that it is possible
 * write.
 *
 * @see #setBase	
 * 
 *  
 * @author Mario Viara
 * @version 1.01
 */
public class Network extends jmce.sim.Serial implements MemoryReadListener,MemoryWriteListener
{
	private int base;

	public Network()
	{
		setName("Network");
		setBase(0xff);
	}
	
	public Network(String name,int base)
	{
		setName(name);
		setBase(base);
	}

	/**
	 * Set the base address.
	 * 
	 *<p>
	 * Specify the base addres of this network interface. The 2
	 * user register will be base+0 for the status and base+1 for
	 * the data.
	 *
	 * @param base
	 */
	public void setBase(int base)
	{
		this.base = base;
	}

	/**
	 * Retrun the base address.
	 */
	public int getBase()
	{
		return base;
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addIOWriteListener(base+1,this);
		cpu.addIOReadListener(base+0,this);
		cpu.addIOReadListener(base+1,this);
	}

	public int	readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		value = 0;
		
		if (address == (base + 0))
		{
			setIdle();
			if (readyRead())
				value |= 0x01;
			if (readyWrite())
				value |= 0x02;
		}
		else if (address == base + 1)
		{
			setLive();
			value = read();
			cpu.setStatusLine('r');
		}

		return value;
	}


	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		setLive();
		write(value);
		cpu.setStatusLine('w');
	}


	public String toString()
	{
		return "Network at 0x"+Hex.formatByte(base);
	}
}
   