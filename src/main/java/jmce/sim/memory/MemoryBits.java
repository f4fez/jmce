/**
   $Id: MemoryBits.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.sim.memory;

import jmce.sim.*;
import jmce.sim.MemoryWriteListener;

/**
 * Map a portion of one memorty location.<p>
 *
 * This class map a portion of m emory location and permiot to read and
 * write only the selected bit.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class MemoryBits 
{
	protected int mask;
	protected int shift;
	private int width;
	private Memory memory;
	private int address;

	public MemoryBits(Memory memory,int address,int shift,int width)
	{
		this.width = width;
		this.mask = (1 << width) - 1;
		this.shift = shift;
		this.address = address;
		this.memory = memory;
	}


	public int getWidth()
	{
		return width;
	}



	public int getBits() throws SIMException
	{
		int value = memory.getMemory(address);
		value >>= shift;
		value &= mask;
		return value;
	}

	public void setBits(int v) throws SIMException
	{
		synchronized(memory)
		{
			int value = memory.getMemory(address);
			v &= mask;
			value &= ~(mask << shift);
			value |= v << shift;
			memory.setMemory(address,value);
		}
	}

	public void addMemoryWriteListener(MemoryWriteListener l)
	{
		memory.addMemoryWriteListener(address,l);
	}
	
	public void removeMemoryWriteListener(MemoryWriteListener l)
	{
		memory.removeMemoryWriteListener(address,l);
	}
}

