/**
   $Id: Memory.java 596 2011-05-24 07:12:27Z mviara $

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

/**
 * Z80Pack memory manager implementation.
 * 
 * <p>
 * According to documentation 16
 * bank of 64 KB are installed and the shared memory can be change in
 * block of 256 bytes. The default value is to have 16KB of shared
 * memory starting at 48 KB.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Memory extends jmce.sim.memory.BankedMemory implements	Z80PackConstants,
									MemoryReadListener,
									MemoryWriteListener

{

	/**
	 * Default constructor.
	 */
	public Memory()
	{
		setName(CPU.MAIN_MEMORY);
		
		setSize(64*1024);
		
		setNumBank(16);
		
		setPageSize(256);
		
		setNumPage(256);
		
		// Default shared size is 48-64K
		setSharedStart(192);
		setSharedSize(64);
	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		
		cpu.addIOWriteListener(MMU_INIT,this);
		cpu.addIOWriteListener(MMU_BANK,this);
		cpu.addIOWriteListener(MMU_SEGMENT,this);
	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		switch (address)
		{
			case	MMU_INIT:
				return isMmuInitialized() ? getNumBank() : 0;

			case	MMU_BANK:
				return getBank();

			case	MMU_SEGMENT:
				return 256 - getSharedSize();

			default:
				return 0;
		}
	}

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		switch (address)
		{
			case	MMU_INIT:
				if (!isMmuInitialized())
				{
					setNumBank(value);
					initMmu();
				}
				break;
				
			case	MMU_BANK:
				setBank(value);
				break;
				
			case	MMU_SEGMENT:
				setSharedSize(256 - value);
				setSharedStart(value);
				break;

			default:
		}
		
	}
}
