/**
   $Id: VIC20.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.commodore.vic20;

import jmce.sim.*;
import jmce.sim.memory.*;
import jmce.mos.VIA6522;
import jmce.sim.cpu.Binary;


/**
 * Commodore VIC20 emulator.
 * <p>
 * <h2>Emulated peripheral</h2>
 * <p>
 * <ul>
 *  <li>1 x VIC6561</li>
 *  <li>2 x VIC6522</li>
 *  <li>1 x VIC-20 Keyboard</li>
 *  <li>1 x Joystick</li>
 *  <li>1 x Cassette player</li>
 * </ul
 * <p>
 * <h2>Memory map</h2>
 * <p>
 * <ul>
 *  <li>0000-03FF Base memory</li>
 *  <li>0400-0FFF 3 KB RAM expansion # 0</li>
 *  <li>1000-1FFF 4 KB Base memory</li>
 *  <li>2000-3FFF 8 KB RAM/ROM expansion # 1</li>
 *  <li>4000-5FFF 8 KB RAM/ROM expansion # 2</li>
 *  <li>6000-7FFF 8 KB RAM/ROM expansion # 3</li>
 *  <li>8000-8FFF Char rom</li>
 *  <li>9000-9FFF I/O block</li>
 *  <ul>
 *   <li>9000-900F VIC6561</li>
 *   <li>9110-911F VIA6522 # 1</li>
 *   <li>9120-912F VIA6522 # 2</li>
 *  </ul>
 *  <li>A000-BFFF Expansion ROM</li>
 *  <li>C000-DFFF 8K Basic ROM</li>
 *  <li>E000-FFFF 8K Bios ROM</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.01
 */
public class VIC20 extends jmce.mos.M6502
{
	VIA6522 via1,via2;
	VIC6561 vic;
	Keyboard kbd;
	Tape tape;
	
	/*
	 * Memory map expansion.
	 * <p>
	 * This variable determine the expansion ram installed when a
	 * bit is set to 1.
	 * <p>
	 * <ul>
	 *  <li>Bit 0 Ram expansion #0 0400-0FFF</li>
	 *  <li>Bit 1 Ram expansion #1 2000-3FFF</li>
	 *  <li>Bit 2 Ram expansion #1 4000-5FFF</li>
	 *  <li>Bit 3 Ram expansion #1 6000-7FFF</li>
	 * </ul>
	 */
	private Binary ramexp = new Binary(0);
	
	public VIC20()
	{
		setName("VIC20");
		setRealTime(true);
		setClock(985248);
		jmce.swing.Util.setRepaintDelay(20);

	}

	/**
	 * Set the installed ram expansion
	 */
	public void setRamExp(int ramexp)
	{
		this.ramexp.setValue(ramexp);
	}

	/**
	 * Return the installed ram expansion
	 */
	public int getRamExp()
	{
		return ramexp.getValue();
	}

	@Override
	protected void initPeripherals() throws SIMException
	{
		/** If no loader is installed load defaults rom */
		if (getHardware(Loadable.class) == null)
		{
			Loadable boot;
			
			boot = new Loadable("commodore/vic20/char.rom",0x8000);
			addHardware(boot);
			
			boot = new Loadable("commodore/vic20/kernal-pal.rom",0xe000);
			addHardware(boot);
			
			boot = new Loadable("commodore/vic20/basic.rom",0xc000);
			addHardware(boot);

		}

		vic = (VIC6561)getHardware(VIC6561.class);
		if (vic == null)
			addHardware(vic = new VIC6561());

		via1 = (VIA6522)getHardwareForName("VIA1");
		if (via1 == null)
			addHardware(via1 = new VIA6522("VIA1",0x9110));
		
		via2 = (VIA6522)getHardwareForName("VIA2");
		if (via2 == null)
			addHardware(via2= new VIA6522("VIA2",0x9120));

		kbd = (Keyboard)getHardware(Keyboard.class);
		if (kbd == null)
			addHardware(kbd = new Keyboard());

		tape = (Tape)getHardware(Tape.class);
		if (tape == null)
			addHardware(tape = new Tape());
		
		/** connect the keyboard to the screen and the vias */
		kbd.setComponent(vic.getComponent());
		kbd.setVia2(via2);
		kbd.setVia1(via1);
		
		/** connect the tape to the 2 via */
		tape.setVia2(via2);
		tape.setVia1(via1);

		super.initPeripherals();
		

	}

	@Override
	public void reset() throws SIMException
	{
		super.reset();
		
	}
	

	@Override
	protected void initMemories() throws SIMException
	{
		Memory m = getMemoryForName(MAIN_MEMORY);

		if (m == null)
		{
			
			/** 64 KB of adddress space */
			m = new PlainMemory(MAIN_MEMORY,0x10000);


			addHardware(m);


		}
		
		/** Ram expansion block # 0 ? */
		if (!ramexp.getBit(0))
			m.setReadOnly(0x0400,0x0C00);

		/** Ram expansion block # 1 ? */
		if (!ramexp.getBit(1))
			m.setReadOnly(0x2000,0x2000);

		/** Ram expansion block # 2 ? */
		if (!ramexp.getBit(2))
			m.setReadOnly(0x2000,0x2000);

		/** Ram expansion block # 3 ? */
		if (!ramexp.getBit(3))
			m.setReadOnly(0x2000,0x2000);

		/** Char generator */
		m.setReadOnly(0x8000,0x1000);

		/** Expansion rom */
		m.setReadOnly(0xa000,0x2000);

		/** Basic */
		m.setReadOnly(0xc000,0x2000);

		/** Bios */
		m.setReadOnly(0xE000,0x2000);

			
		super.initMemories();
	}
}
