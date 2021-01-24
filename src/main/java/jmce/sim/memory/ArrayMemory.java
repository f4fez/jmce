/**
   $Id: ArrayMemory.java 610 2011-05-26 07:52:15Z mviara $

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


/**
 * Array memory.
 *<p>
 * This type of memory are implemented using one array of memory and
 * only one can be enabled at time. It look like a BankedMemory but
 * each bank can be a different memory and do not support shared page.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class ArrayMemory extends AbstractMemory 
{
	private int index;
	private Memory m;
	
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
		throw new Error("Invalid method");

	}

	public int getSize()
	{
		return m.getSize();
	}

	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);
		setIndex(0);

	}

	/**
	 * Return the index of the current selected memory.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * Set the memory index.
	 */
	public void setIndex(int i)
	{
		index = i;
		m = getMemoryAt(i);
	}
	
	protected final Memory mapMemory(int a)
	{
		return m;
	}

	protected final int mapAddress(int a)
	{
		return a;
	}

	/**
	 * Add virtual memory to the actual list.
	 *
	 * @since 1.02
	 */
	@Override
	public void addMemory(Memory v)
	{
		super.addMemory(v);
		if (vms.getSize() == 1)
			setIndex(0);
	}
}

