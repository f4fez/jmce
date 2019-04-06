/**
   $Id: Delay.java 596 2011-05-24 07:12:27Z mviara $

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
 * This class delay  the CPU execution.
 * <p>
 * Writing a value on n thein the port <tt>DELAY_CTRL</tt> suspend the
 * execution of the emulator for n * 10 ms.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Delay extends AbstractPeripheral implements
   Z80PackConstants,MemoryReadListener,MemoryWriteListener
{
	public Delay()
	{
		setName("Delay 10 ms");
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addIOReadListener(DELAY_CTRL,this);
		cpu.addIOWriteListener(DELAY_CTRL,this);


	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		return 0;
	}

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		int count = 10 * value;
		while (count > 0)
			count -= cpu.idle();
	}

}

