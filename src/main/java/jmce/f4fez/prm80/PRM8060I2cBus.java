/**
   $Id: DiseqcI2cBus.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.f4fez.prm80;

import jmce.philips.P80c552;
import jmce.sim.*;
import jmce.atmel.AT24C16;

/**
 * I2C Bus for PRM80.
 * 
 * This class implements the I2C bus for the PRM80 project. The bus is
 * implemented not using one I2C controller buf only 2 open collector
 * pin.
 *
 */
public class PRM8060I2cBus extends I2cBus
{
	private Memory memory = null;


	public void init(Hardware parent) throws SIMException
	{
		memory = (AT24C16)getHardware(AT24C16.class);
		if (memory == null)
			addHardware(memory = new AT24C16());
		super.init(parent);

	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		P80c552 d = (P80c552)cpu;
		setScl(d.getSfrBitOpenCollector(P80c552.P1,6));
		setSda(d.getSfrBitOpenCollector(P80c552.P1,7));
	}

}
