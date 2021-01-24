/**
   $Id: Console.java 510 2011-01-18 09:25:07Z mviara $

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

import jmce.sim.CPU;
import jmce.sim.MemoryWriteListener;
import jmce.sim.MemoryReadListener;

/**
 * Z80Pack Console implementations.
 *<p>
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Console extends jmce.sim.Serial implements Z80PackConstants,MemoryReadListener,MemoryWriteListener
{

	public Console()
	{
		setName("Z80pack Console");
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addIOWriteListener(CONDAT,this);
		cpu.addIOReadListener(CONDAT,this);
		cpu.addIOReadListener(CONSTA,this);
	}

	public int	readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		switch (address)
		{
			case	CONSTA:
				setIdle();
				if (readyRead())
					return 0xff;
				return 0;
				
			case	CONDAT:
				setLive();
				return read();
			default:
				return 0;
		}
	}
	

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		switch (address)
		{
			case	CONDAT:
				setLive();
				write(value);
				break;
		}
	}

	public String toString()
	{
		return "Z80Pack console Status="+CONSTA+" Data="+CONDAT;
	}
}

