/**
   $Id: AbstractCRT.java 431 2010-11-08 08:42:46Z mviara $

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

package jmce.sim.terminal;

import jmce.sim.*;

/**
 * Abstract implementation of CRT.
 *
 * @author Mario Viara
 * @version 1.00
 */
abstract public class AbstractCRT extends jmce.sim.AbstractHardware implements CRT
{
	
	protected Terminal terminal = null;
	protected boolean lineChanged[];
	protected boolean screenChanged;
	protected int numCol,numRow;
	protected Memory charMemory;
	protected Memory attMemory;
	
	public AbstractCRT(String name)
	{
		setName(name);
	}

	public AbstractCRT()
	{
		this("AbstractCRT");
	}


	

	public void init(Hardware parent) throws SIMException
	{
		/** The parent must be a Terminal ! */
		terminal = (Terminal)parent;
		numCol = terminal.getNumCol();
		numRow = terminal.getNumRow();
		lineChanged = new boolean[numRow];
		screenChanged = false;
		
		charMemory = terminal.getCharMemory();
		attMemory  = terminal.getAttMemory();
		charMemory.addMemoryWriteListener(this);
		attMemory.addMemoryWriteListener(this);

		super.init(parent);

	}

	public void writeMemory(Memory m,int address,int value,int oldValue)
	{
		changedLine(address / numCol);
	}


	public void changedLine(int n)
	{
		
		synchronized (lineChanged)
		{
			screenChanged = lineChanged[n] = true;
		}
	}

	public int getChar(int r,int c) throws SIMException
	{
		return charMemory.getMemory(r*numCol+c);
	}

	public int getAtt(int r,int c) throws SIMException
	{
		return attMemory.getMemory(r*numCol+c);
	}


}
