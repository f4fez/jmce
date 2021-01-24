/**
   $Id: Loadable.java 610 2011-05-26 07:52:15Z mviara $

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
package jmce.sim;

import jmce.util.Logger;
import jmce.util.Hex;
import jmce.util.FastArray;

/**
 * File loader.
 * 
 * Peripheral to load a file in memory after the CPU is reset. The periheral must
 * be added directly to the involved CPU. 
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Loadable extends AbstractPeripheral implements ResetListener
{
	private static Logger log = Logger.getLogger(Loadable.class);
	private String filename = "";
	private int address = 0;
	private FastArray<LoadableListener> listeners = new FastArray<LoadableListener>();
	
	public Loadable(String filename,int address)
	{
		setFileName(filename);
		setAddress(address);
	}

	public Loadable(String filename)
	{
		this(filename,0);
	}
	public Loadable()
	{
		this("",0);
	}

	public void setFileName(String s)
	{
		filename =s;
	}

	public String getFileName()
	{
		return filename;
	}

	public void setAddress(int n)
	{
		address = n;
	}

	public int getAddress()
	{
		return address;
	}
	
			
	public void registerCPU(CPU cpu)
	{
		cpu.addResetListener(this);
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
	
	public void reset(CPU cpu) throws SIMException
	{
		LoadInfo info = new LoadInfo();

		for (int i = 0 ; i < listeners.getSize() ; i++)
			listeners.get(i).startLoad(cpu);
		
		cpu.load(filename,address,info);

		for (int i = 0 ; i < listeners.getSize() ; i++)
			listeners.get(i).endLoad(cpu,info);

		log.info(filename+" loaded at 0x"+Hex.formatWord(info.start)+"-0x"+Hex.formatWord(info.end));
	}

	public String toString()
	{
		return "Loadable "+filename+" at 0x"+Hex.formatWord(address);
	}
	
}
