/**
   $Id: PlainMemory.java 695 2011-09-21 06:09:11Z mviara $

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

import java.util.Arrays;
import jmce.sim.*;

/*
 * Standard memory implemetation.
 *
 * <p>This implementation use simple array to implements memory.
 * 
 * @author Mario Viara
 * @version 1.01
 *
 */
public class  PlainMemory extends AbstractMemory
{
	/** Array with memory data */
	protected int[]	memory	= allocMemory(0);

	/** Temporary array pointer for memory copy operation */
	protected int[]	tmpMemory = allocMemory(0);
	
	
	/** Default constructor */
	public PlainMemory()
	{
	}

	/** Constructor with memory name */
	public PlainMemory(String name)
	{
		super(name);
			
	}

	/**
	 * Constructor with memory name and memory size
	 */
	public PlainMemory(String name,int size)
	{
		super(name);
		setSize(size);
	}

	protected void set(int a,int v)
	{
		memory[a] = v;
	}

	protected int get(int a)
	{
		return memory[a] & 0xff;
	}

	/**
	 * Reset the memory to initial state. Fill the memory with 0xff
	 *
	 * @since 1.02
	 */
	@Override
	public void reset() throws SIMException
	{
		super.reset();
		Arrays.fill(memory,(byte)0xff);
	}
	

	@Override
	public void setSize(int newSize)
	{
		super.setSize(newSize);

		if (memory.length != newSize)
		{
			tmpMemory = allocMemory(newSize);
			int oldSize = memory.length;
			copyMemory(memory,0,tmpMemory,0,oldSize > newSize ? newSize : oldSize);
			memory = tmpMemory;
			
		}
	}

	/**
	 * Function used by subclass to allocate memory. The memory is
	 * filled with 0xff. So the real type of memory is confinated
	 * to this class.
	 *
	 * @since 1.02
	 */
	static  protected int[] allocMemory(int size)
	{
		int m[] = new int[size];
		Arrays.fill(m,(byte)0xff);
		return m;
	}

	/**
	 * Function used by subclass to copy memory. So the real type
	 * of memory is confinated to this class.
	 *
	 * @since 1.02
	 */
	static protected void copyMemory(int source[],int soffset,int dest[],int doffset,int size)
	{
		System.arraycopy(source,soffset,dest,doffset,size);
	}
}

