/**
   $Id: Ports.java 454 2010-12-01 18:33:52Z mviara $

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

package jmce.philips.lpc900;

import jmce.sim.*;

/**
 * LPC90 Input output port.
 *
 * LPC900 have 3 I/O port but the last have only bit from 2 to 7.<p>
 *
 * Port at default are initialized in input because the standard 8051
 * port are initialized as semi bidirectional.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Ports extends jmce.intel.mcs51.Ports implements LPC900Constants,ResetListener
{
	Ports()
	{
		setNumPort(3);
		setDisableMask(2,0xFC);
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		cpu.addResetListener(this);
		super.registerCPU(cpu);

	}

	public void reset(CPU _cpu) throws SIMException
	{
		LPC900 cpu = (LPC900)_cpu;
		cpu.sfr(P0M1,0xff);
		cpu.sfr(P0M2,0x00);
		cpu.sfr(P1M1,0xff);
		cpu.sfr(P1M2,0x00);
		cpu.sfr(P2M1,0xff);
		cpu.sfr(P2M2,0x00);
	}
}
