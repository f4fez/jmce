/**
   $Id: CombinedMemory.java 814 2012-03-29 11:07:49Z mviara $

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

import jmce.sim.Hardware;
import jmce.sim.Memory;
import jmce.sim.SIMException;

/**
 * Memory made from other memory.
 * <p>
 * This type of memory is the result of addtion of other memory
 * for example if we add one memory of 4 KB and another of 20 KB the
 * total size of the memory will be 24 KB.
 * <p>
 * <h2>Limitations</h2>
 * <p>
 * <ul>
 *  <li>Added memory will not initialized so if are not used in other
 * peripheral must be added using the <tt>addHardware</tt> method.</li>
 *  <li>The registered specific memory listener when called will have the
 * address set relative to the selected memory.</li>
 * </ul>
 * <p>
 * @author Mario Viara
 * @version 1.01
 *
 * @see #addMemory
 */
public class CombinedMemory extends AbstractMemory implements Memory
{

	private Memory memory[];
	private int address[];
	
	public CombinedMemory()
	{
	}

	public CombinedMemory(String name)
	{
		super(name);
	}
	

	protected void set(int a,int v)
	{
		throw new Error("Invalid method");
	}
	

	protected int get(int a) 
	{
		throw new Error("Invalid method");

	}
	
	public void setSize(int newSize)
	{
		
	}

	public int getSize()
	{
		int size = 0;
		
		for (int i = 0 ; i < getMemoryCount() ; i++)
		{
			Memory m = getMemoryAt(i);
			size += m.getSize();
		}
		
		return size;
	}

	protected final Memory mapMemory(int a)
	{
		return memory[a];
	}

	protected final int mapAddress(int a)
	{
		return address[a];
		
	}

	@Override
	public void init(Hardware parent) throws SIMException
	{
		int size = getSize();
		memory = new Memory[size];
		address = new int[size];
		int pos = 0;

		for (int i = 0 ; i < getMemoryCount() ; i++)
		{
			Memory m = getMemoryAt(i);
			size = m.getSize();
			
			for (int j = 0 ; j < size ; j++)
			{
				memory[pos] = m;
				address[pos] = j;
				pos++;
			}
		}

		
		super.init(parent);
	}
	

	public String toString()
	{
		return getName()+" CombinedMemory Size "+getSize()+" Count "+getMemoryCount();
	}
	

}


