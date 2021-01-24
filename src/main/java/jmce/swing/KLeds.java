/**
   $Id: KLeds.java 808 2012-03-08 20:25:47Z mviara $

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
package jmce.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Panel of multiple LEDS.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class KLeds extends JPanel
{
	private static final long serialVersionUID = 1L;
	static public final Font labelLedFont = new Font("Monospaced",Font.PLAIN,10);
	static public final Font labelTitleFont = new Font("Monospaced",Font.BOLD,12);
	KLed leds[];
	JLabel labels[];

	public KLeds(String title,int n)
	{
		super(new GridBagLayout());

		setOpaque(true);
		setBackground(java.awt.Color.black);
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0; g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE; 
		g.weightx = 1.0;g.weighty = 1.0;g.insets = new Insets(0,0,0,0);

		leds = new KLed[n];
		labels = new JLabel[n];

		for (int i = 0 ; i < n ; i++)
		{
			String s =""+i;
			if (n > 10 && i < 10)
				s = "0"+s;
			labels[i] = new JLabel(s,SwingConstants.CENTER);
			labels[i].setFont(labelLedFont);
			labels[i].setForeground(Color.white);
			labels[i].setBackground(Color.black);
			labels[i].setOpaque(true);
			leds[i] = new KLed();
		}
		
		for (int i = 0 ; i < n ; i++)
		{
			g.gridy = 0;
			add(labels[n - i - 1],g);
			g.gridy = 1;
			add(leds[n - i - 1],g);
			g.gridx++;
			
		}


		TitledBorder t = Util.setTitle(this,title,java.awt.Color.cyan);
		t.setTitleFont(labelTitleFont);

	}

	public void setPolarity(boolean mode)
	{
		for (int i = 0 ; i < leds.length ; i++)
			setPolarity(i,mode);
	}

	public void setPolarity(int i,boolean mode)
	{
		leds[i].setPolarity(mode);
	}

	public void invertLeds()
	{
		for (int i = 0 ; i < leds.length ; i++)
			invertLed(i);
	}

	public void invertLed(int i)
	{
		leds[i].setLed(!leds[i].getLed());
	}
	
	public void setPaintLabels(boolean mode)
	{
		for (int i = 0 ; i < labels.length ; i++)
			labels[i].setVisible(mode);
	}

	public boolean getPaintLabels()
	{
		return labels[0].isVisible();
	}

	public void setLedColors(Color color)
	{
		for (int i = 0; i < leds.length ; i++)
			setLedColor(i,color);
	}

	public void setLedColor(int i,Color color)
	{
		
		leds[i].setForeground(color);
	}

	public void setLeds(int value)
	{
		int mask = 1;
		for (int i = 0; i < leds.length ; i++)
		{
			setLed(i,(value & mask) != 0);
			mask <<= 1;
		}
	}
	
	public void setLeds(boolean mode)
	{
		for (int i = 0; i < leds.length ; i++)
			setLed(i,mode);
	}

	public void setLed(int i,boolean mode)
	{
		leds[i].setLed(mode);
	}

	public boolean getLed(int i)
	{
		return leds[i].getLed();
	}

	public int getLedCount()
	{
		return leds.length;
	}

	static public void main(String argv[])
	{
		final KLeds address = new KLeds("Address",16);
		final KLeds test = new KLeds("No address",20);
		
		test.setPaintLabels(false);
		for (int i = 0 ; i < test.getLedCount() ; i++)
		{
			if (i >= 0 && i <= 4)
				test.setLedColor(i,new Color(255,0,0));
			if (i >= 4 && i <= 8)
				test.setLedColor(i,new Color(0,255,0));
			if (i >= 8 && i <= 12)
				test.setLedColor(i,new Color(0,0,255));
			if (i >= 12 && i <= 16)
				test.setLedColor(i,new Color(255,255,255));
			if (i >= 16 && i <= 20)
				test.setLedColor(i,new Color(255,255,0));
		}
		
		JPanel p = new JPanel();
		p.add(address);
		p.add(test);

		JFrame f = new JFrame("Led Test");
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);

		javax.swing.Timer t =new javax.swing.Timer(500,new ActionListener()
		{
			int count = 0;
			public void actionPerformed(ActionEvent e)
			{
				address.invertLeds();
				test.invertLeds();

				if (++count == 10)
					test.setLeds(true);
			}
		});

		t.setRepeats(true);
		t.start();


	}


}
