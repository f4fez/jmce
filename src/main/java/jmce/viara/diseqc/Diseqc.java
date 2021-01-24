/**
   $Id: Diseqc.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.viara.diseqc;

import jmce.sim.*;
import jmce.intel.mcs51.Ports;
import jmce.intel.mcs51.JPorts;
import jmce.intel.mcs51.Serial;

import jmce.sim.terminal.Terminal;

/**
 * Diseqc 1.2 positioner emulator.
 * <p>
 * For more detail see the Diseqc 1.2 Positioner
 * <a href="http://www.viara.eu/diseqc"/>site</a>
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Diseqc extends jmce.philips.lpc764.LPC764
{
	private DiseqcI2cBus i2cbus;
	private DiseqcMotor  motor;

	public Diseqc()
	{
		setClock(11059200);
		setRealTime(true);
	}

				       
	protected void initPeripherals() throws SIMException
	{
		// Add Jport
		Ports ps;

		motor = (DiseqcMotor)getHardware(DiseqcMotor.class);

		if (motor == null)
			addHardware(motor = new DiseqcMotor());

		ps = (Ports)getHardware(Ports.class);
	
		if (ps == null)
		{
			ps = new jmce.philips.lpc764.Ports();
			addHardware(ps);
			ps.addHardware(new JPorts());
		}
		
		
		// Add serial port with Swing terminal
		if (getHardware(Serial.class) == null)
		{
			Serial s = new Serial();
			Terminal t = jmce.sim.terminal.Terminal.createTerminal();
			t.setNumCol(60);
			t.setNumRow(16);
			t.setFontSize(14);
			t.addHardware(new jmce.sim.terminal.SwingCRT());
			s.addHardware(t);
			s.setConnected(t);
			addHardware(s);
		}

		i2cbus = (DiseqcI2cBus)getHardware(DiseqcI2cBus.class);
		
		if (i2cbus == null)
			addHardware(i2cbus = new DiseqcI2cBus());


		// Load the software
		if (getHardware(Loadable.class) == null)
		{
			addHardware(new Loadable("hex/diseqc.hex",0));
		}

		super.initPeripherals();

	}


	public void reset() throws SIMException
	{
		super.reset();
	}

	
}
