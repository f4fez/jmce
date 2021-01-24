/**
   $Id: Memory128K.java 814 2012-03-29 11:07:49Z mviara $

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
package jmce.sinclair.spectrum;

import jmce.intel.i8080.I8080;
import jmce.sim.CPU;
import jmce.sim.LoadInfo;
import jmce.sim.LoadableListener;
import jmce.sim.Memory;
import jmce.sim.MemoryReadListener;
import jmce.sim.MemoryWriteListener;
import jmce.sim.ResetListener;
import jmce.sim.SIMException;
import jmce.sim.SIMSWException;
import jmce.sim.memory.ArrayMemory;
import jmce.sim.memory.CombinedMemory;
import jmce.sim.memory.LoadableMemory;
import jmce.sim.memory.PlainMemory;
import jmce.util.Logger;

/**
 * Spectrum 128K Memory.
 * 
 * <p>
 * The spectrum 128K memory look like this :
 * <p>
 * <pre>
 * 0FFFF
 * 0C000	Bank0	Bank1	Bank2	Bank3	Bank4	Bank5	Bank6	Bank7
 * 08000	Bank2
 * 04000	Bank5
 * 00000	Rom0	Rom1
 * SCREEN	Bank5	Bank7
 * </pre>
 * <p>
 * The Rom0 is the editor and Rom1 the Basic. Bank0-7 are bank of 16K
 * memory. Bank5 is the default screen memory and Bank7 the alternate
 * screen. To control the memory manager one byte register at
 * 0x7ffd is used.
 * <p>
 * MMU Control register at 7FFD
 * <pre>
 *	Bit5	If set the MMU is disable till a new reset.
 *	Bit4	0 Select Rom0 at 0000 1 Select Rom1.
 *	Bit3	0 Select Bank5 as display memory 1 Select Bank7
 *	Bit2-0	Select Bank mapped at  C000.
 * </pre>
 *
 * @author Mario Viara
 * @version 1.00
 * @since 1.02
 */
public class Memory128K extends CombinedMemory implements MemoryWriteListener,
SpectrumConstants,SpectrumMemory,ResetListener,MemoryReadListener
{
	private static Logger log = Logger.getLogger(Memory128K.class);
	private Memory bank[] = new Memory[8];
	private LoadableMemory rom0,rom1;
	private ArrayMemory roms = new ArrayMemory();
	private ArrayMemory banks = new ArrayMemory();
	private I8080 i8080;
	private int mmu;
	private Screen screen;
	
	public Memory128K()
	{
		super(CPU.MAIN_MEMORY);
		
		
		for (int i = 0 ; i < 8 ; i++)
		{
			bank[i] = new PlainMemory("Bank"+i,16*1024);
			banks.addHardwareMemory(bank[i]);
		}

		addHardware(banks);

		rom0 = new LoadableMemory("Rom0",16*1024,"sinclair/128a.rom");
		rom0.addLoadableListener(new LoadableListener()
		{
			public void startLoad(CPU cpu)
			{
				writeMMU(0);
			}

			public void endLoad(CPU cpu,LoadInfo info)
			{
			}

		});

		rom1 = new LoadableMemory("Rom1",16*1024,"sinclair/128b.rom");
		rom1.addLoadableListener(new LoadableListener()
		{
			public void startLoad(CPU cpu)
			{
				writeMMU(MMU_ROM);
			}

			public void endLoad(CPU cpu,LoadInfo info)
			{
				writeMMU(0);
			}

		});

		roms.addHardwareMemory(rom0);
		roms.addHardwareMemory(rom1);

		addHardware(roms);

		/** Now create the layout for the main combined memory*/
		addMemory(roms);
		addMemory(bank[5]);
		addMemory(bank[2]);
		addMemory(banks);

		log.info("Memory "+toString());
	}
	
	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		this.i8080 = (I8080)cpu;

		cpu.addResetListener(this);
		cpu.addIOReadListener(this);
		cpu.addIOWriteListener(this);
		
		screen = (Screen)cpu.getHardware(Screen.class);
		
		if (screen == null)
			throw new SIMSWException("No Spectrum Screen installed!");

	}

	/**
	 * Write MMU register
	 **/
	public void writeMMU(int value)
	{
		int old = mmu;
		
		/** If the MMU is disable do nothing */
		if ((mmu & MMU_DISABLE) != 0)
			return;
		
		mmu = value;

		/** Select the video memory */
		if ((mmu & MMU_VIDEO) != (old & MMU_VIDEO))
			screen.setMemory(getVideoMemory());

		/** Select bank at c000 */
		banks.setIndex(mmu & 0x07);

		/** Select rom at 0x0000 */
		roms.setIndex((mmu & MMU_ROM) != 0 ? 1 : 0);

	}

	/** Global I/O read listener */
	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		address |= i8080.getPortHI() << 8;

		if (address == MMU_PORT)
			value = mmu;

		return value;
	}
	
	/** Global I/O write listener */
	public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		address |= i8080.getPortHI() << 8;

		
		if (address == MMU_PORT)
			writeMMU(value);
	}

	public Memory getVideoMemory()
	{
		return (mmu & MMU_VIDEO) == 0 ? bank[5] : bank[7];
	}

	public void reset(CPU cpu) throws SIMException
	{
		mmu &= ~MMU_DISABLE;
		writeMMU(0);
	}
}