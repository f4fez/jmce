/**
   $Id: JPorts.java 694 2011-09-02 12:01:08Z mviara $

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
package jmce.intel.mcs51;


import java.awt.*;

import javax.swing.*;

import jmce.sim.*;

import jmce.swing.KBitField;

/**
 * Intel 8051 ports swing renderer.
 * <p>
 *
 * Attach this peripheral to one <code>Ports</code> to show one swing
 * panel where is possible see and change the value for the standard
 * MCS51 ports.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class JPorts extends AbstractPeripheral implements jmce.sim.SwingHardware
{
	Ports ports;
	JPanel p = null;

	/**
	 * Default constructor.
	 */
	public JPorts()
	{
	}
	
	public Component getComponent()
	{
		if  (p != null)
			return p;
		
		p = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0; g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE; g.insets = new Insets(0,0,0,0);

		for (int i = 0 ; i < ports.getNumPort() ; i++)
		{
			p.add(new JPort(cpu,ports,i),g);
			g.gridy++;
		}

		return p;
	}

	@Override
	public void init(Hardware parent) throws SIMException
	{
		ports = (Ports)parent;

		super.init(parent);
	}
	
	
}

/**
 * Helper GUI class for single port
 */
class JPort extends JPanel
{
	private static final long serialVersionUID = 1L;

	int p,m1,m2;

	KBitField jp,jm1,jm2;

	public JPort(CPU cpu,Ports ports,int n)
	{
		super(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0; g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE;
		g.insets = new Insets(0,0,0,0);

		jm1 = new KBitField("P"+n+"M1");
		jm2 = new KBitField("P"+n+"M2");
		jp  = new KBitField("P"+n);

		add(jm1,g);g.gridx++;add(jm2,g);g.gridx++;add(jp,g);

		if (ports.getSfrM1(n) >= 0)
		{
			cpu.addIOWriteListener(ports.getSfrM1(n),new MemoryWriteListener()
			{
				public void writeMemory(Memory m,int r,int v,int oldValue)
				{
					m1 = v;
					jm1.setValue(v);
				}
			});
		}
		else
			jm1.setEditable(false);


		if (ports.getSfrM2(n) > 0)
		{
			cpu.addIOWriteListener(ports.getSfrM2(n),new MemoryWriteListener()
			{
				public void writeMemory(Memory m,int r,int v,int oldValue)
				{
					m2 = v;
					jm2.setValue(v);
				}
			});
		}
		else
			jm2.setEditable(false);

		cpu.addIOWriteListener(ports.getSfrP(n),new MemoryWriteListener()
		{
			public void writeMemory(Memory m,int r,int v,int oldValue)
			{
				jp.setValue(v);
			}
		});


	}
}
