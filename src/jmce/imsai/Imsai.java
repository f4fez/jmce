/**
   $Id: Imsai.java 634 2011-06-16 07:49:34Z mviara $

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
package jmce.imsai;

import java.awt.*;
import javax.swing.*;
import jmce.sim.*;

import jmce.sim.terminal.*;



/**
 * IMSAI 8080.
 * <p>
 * This class implements the IMSA 8080.
 * <p>
 * <h2>Implemented peripheral</h2> :
 * <ul>
 *  <li>Z80 cpu</li>
 *  <li>1 x Serial port connected to the console.</li>
 *  <li>8 x Status led.</li>
 *  <li>Load imsai/lightdemo.hex at reset.</li>
 * </ul>

 * @author Mario Viara
 * @version 1.00
 */
public class Imsai extends jmce.intel.i8080.I8080 implements jmce.sim.SwingHardware
{
	JFrame frame;
	
	public Imsai()
	{
		super("Imsai 8080");
		setRealTime(true);
	}

	public void initPeripherals() throws SIMException
	{
		if (getHardware(Console.class) == null)
		{
			Console c = new Console();
			jmce.sim.terminal.Terminal t = new jmce.sim.terminal.VT100();
			t.setNumCol(60);
			t.setNumRow(16);
			t.setFontSize(14);
			c.addHardware(t);
			c.setConnected(t);
			t.addHardware(new jmce.sim.terminal.SwingCRT());
			addHardware(c);
		}


		if (getHardware(Leds.class) == null)
			addHardware(new Leds());

		if (getHardware(Loadable.class) == null)
			addHardware(new Loadable("imsai/lightdemo.hex"));
		super.initPeripherals();
	}

	public java.awt.Component getComponent()
	{
		JPanel p = new JPanel(new BorderLayout());

		// Console on center
		Console c = (Console)getHardware(Console.class);
		Terminal t = (Terminal)c.getHardware(Terminal.class);
		
		SwingCRT s = (SwingCRT)t.getHardware(SwingCRT.class);
		p.add(s.getComponent(),BorderLayout.CENTER); 

		// Led on bottom
		Leds l = (Leds)getHardware(Leds.class);
		p.add(l.getComponent(),BorderLayout.SOUTH);

		return p;
	}

	public String toString()
	{
		return "IMSAI 8080";
	}

}
