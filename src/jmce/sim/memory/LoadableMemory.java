/**
   $Id: LoadableMemory.java 610 2011-05-26 07:52:15Z mviara $

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
import jmce.util.FastArray;

/**
 * Sample loadable mmory.
 * <p>
 * A loadable memory is normal memory but at the reset the memory
 * contents will be readed from a file. At default the readOnly
 * property is set to true.
 *
 * @author Mario Viara
 * @version 1.02
 */
public class LoadableMemory extends PlainMemory implements ResetListener
{
	private String filename;
	private FastArray<LoadableListener> listeners = new FastArray<LoadableListener>();

	public LoadableMemory()
	{
		setReadOnly();
	}

	public LoadableMemory(String filename,int size)
	{
		setReadOnly();
		setFilename(filename);
		setSize(size);
	}
	
	
	public LoadableMemory(String name,int size,String file)
	{
		setReadOnly();
		setName(name);
		setSize(size);
		setFilename(file);
	}


	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addResetListener(this);
	}
	
	public void reset(CPU cpu) throws SIMException
	{
		for (int i = 0 ; i < listeners.getSize() ; i++)
			listeners.get(i).startLoad(cpu);
		LoadInfo info = new LoadInfo();;
		cpu.load(this.getFilename(),0,info);
		for (int i = 0 ; i < listeners.getSize() ; i++)
			listeners.get(i).endLoad(cpu,info);
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}


	/**
	 * Return the filename used for read/write memory.
	 */
	protected String filename()
	{
		return getFilename();
	}


	/**
	 * Add a new LoadableListener
	 *
	 * @since 1.02
	 */
	public void addLoadableListener(LoadableListener l)
	{
		listeners.add(l);
	}

	public String toString()
	{
		return super.toString()+" ["+filename+"]";
	}

}
