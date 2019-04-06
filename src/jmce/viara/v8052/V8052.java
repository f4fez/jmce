/**
   $Id: V8052.java 601 2011-05-25 08:24:49Z mviara $

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
package jmce.viara.v8052;

import java.awt.*;

import javax.swing.*;
import jmce.sim.*;
import jmce.intel.mcs51.Ports;
import jmce.intel.mcs51.JPorts;
import jmce.intel.mcs51.Serial;

import jmce.sim.terminal.Terminal;


/**
 * Virtual machine base over Intel 8052.
 * <p>
 * Like the Z80pack for Zilog Z80 this machine is a completly  new brand
 * machine base over Intel 8052. 
 *
 * <p>
 * <h2>Internal peripheral</h2>
 * <ul
 *  <li>1 x Intel 8052 at 11.059.200,00 Hz.</li>
 *  <li>1 x 64K CODE.</li>
 *  <li>1 x 64K XDATA.</li>
 *  <li>1 x 256 Byte IDATA.</li>
 *  <li>2 x Timer (0/1).</li>
 *  <li>1 x Timer2.</li>
 *  <li>4 x I/O port.</li>
 *  <li>1 x Serial port connected to a terminal.</li>
 * </ul>
 * <p>
 * <p>
 * <h2>External peripheral</h2>
 * <ul
 *  <li>1 x LCD display 4 x 20</li>
 * </ul>
 * <p>
 * 
 *
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.02
 */
public class V8052 extends jmce.intel.mcs51.MCS52 implements SwingHardware
{
	private Display Display;
	
	public V8052()
	{
		setName("v8052");
		setClock(11059200);
		setRealTime(true);
	}

	public Component getComponent()
	{
		
		SwingHardware s;
		JPanel p= new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.fill  = GridBagConstraints.BOTH;g.insets = new Insets(2,2,2,2);
		g.anchor = GridBagConstraints.CENTER;
		

		p.add(Display.getComponent(),g);
		g.gridy++;
		
		// Search for swing component of JPort
		s = (SwingHardware)getHardwareTree(Ports.class,JPorts.class);

		if (s != null)
		{
			p.add(s.getComponent(),g);
			g.gridy++;
			s.getComponent().getPreferredSize();
		}
		

		
		// Search for swing component on serial
		s = (SwingHardware)getHardwareTree(Serial.class,Terminal.class,SwingHardware.class);

		p.add(s.getComponent(),g);
		g.gridy++;
		

		return p;
	}
	
				       
	protected void initPeripherals() throws SIMException
	{
		// Add Jport
		Ports ps;
		
		ps = (Ports)getHardware(Ports.class);
	
		if (ps == null)
		{
			ps = new Ports(4);
			addHardware(ps);
			ps.addHardware(new JPorts());
		}
		
		
		// Add serial port with Swing terminal
		if (getHardware(Serial.class) == null)
		{
			Serial s = new Serial();
			Terminal t = new jmce.sim.terminal.VT100();
			t.setNumRow(16);
			t.setNumCol(60);
			t.setFontSize(14);
			t.addHardware(new jmce.sim.terminal.SwingCRT());
			s.addHardware(t);
			s.setConnected(t);
			addHardware(s);
		}

		if (getHardware(Display.class) == null)
		{
			Display = new Display();
			addHardware(Display);
		}
		

		// Load the software
		if (getHardware(Loadable.class) == null)
		{
			addHardware(new Loadable("hex/v8052.hex",0));
		}

		super.initPeripherals();

	}



	public String toString()
	{
		return getName();
	}
}
