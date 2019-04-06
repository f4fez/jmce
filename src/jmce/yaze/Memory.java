/**
   $Id: Memory.java 632 2011-06-14 11:17:35Z mviara $

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
package jmce.yaze;

import jmce.sim.*;

/**
 * Yaze memory manager implementation.
 * 
 * <p>According to documentation 16
 * bank of 64 KB are installed and are accessed in page of 4 KB each.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Memory extends jmce.sim.memory.BankedMemory implements YazeConstants,MemoryWriteListener, ResetListener

{
	int page = 0;
	
	public Memory()
	{
		setName("MEMORY");

		setSize(64*1024);

		setNumBank(16);

		setPageSize(4096);

		setNumPage(16);

		/** No shared memory */
		setSharedStart(0);
		setSharedSize(0);
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);


		cpu.addIOWriteListener(MMU_PAGE_LOW,this);
		cpu.addIOWriteListener(MMU_PAGE_HI,this);
		cpu.addIOWriteListener(MMU_PAGE_FRAME,this);
		cpu.addResetListener(this);
	}


	public void reset(CPU cpu) throws SIMException
	{
		initMmu();
	}
	
	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		switch (address)
		{
			case	MMU_PAGE_LOW:
				page = (page & 0xff00) | value;
				break;

			case	MMU_PAGE_HI:
				page = (page & 0xff) | (value << 8);
				break;

			case	MMU_PAGE_FRAME:
				setPageMap(getBank(),value,page);
				break;

			default:
		}

	}
}
