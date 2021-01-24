/**
   $Id: KBitField.java 692 2011-09-02 08:38:10Z mviara $

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
import java.util.Vector;


/**
 * 
 * CheckBox used to implement groups of bits.
 * 
 *
 * @author Mario Viara
 * @version 1.00
 *
 */
public class KBitField extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * Inner class for a single bit
	 */
	class KBit extends JCheckBox implements Repaintable
	{
		private static final long serialVersionUID = 1L;
		boolean value;

		public void set(boolean value)
		{
			if (this.value == value)
				return;

			this.value = value;
			if (value != isSelected())
				Util.repaintLater(this);
		}

		public void updateComponent()
		{
			setSelected(value);
		}


	}

	private KBit bits[] = new KBit[8];
	private Vector<ActionListener> listeners = new Vector<ActionListener>();
	private int value;

	public KBitField(String title)
	{
		this(title,true);
	}

	public KBitField(String title,boolean bit)
	{
		super(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0; g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE; g.insets = new Insets(0,0,0,0);

		for (int i = 0  ; i < bits.length ; i ++)
		{
			String label = ""+(7-i);
			g.gridy = 0;

			if (bit)
			{
				JLabel l = new JLabel(label);
				l.setFont(Util.fontLabel);
				add(l,g);
				g.gridy = 1;
			}

			bits[7-i] = new KBit();
			bits[7-i].setMargin(new Insets(0,0,0,0));
			bits[7-i].addActionListener(this);
			bits[7-i].setToolTipText(label);
			add(bits[7-i],g);
			g.gridx++;
		}

		Util.setTitle(this,title);
		setValue(0);

	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		for (int i = 0  ;i < listeners.size() ; i++)
		{
			ActionListener l = listeners.elementAt(i);
			l.actionPerformed(e);
		}

	}

	public void setBitName(int bit,String name)
	{
		bits[bit].setToolTipText(name);

	}

	public void setDisabled(int bit,boolean mode)
	{
		bits[bit].setEnabled(!mode);
	}

	public void setValue(int value)
	{
		if (value == this.value)
			return;

		this.value = value;
		
		for (int i = 0  ; i < bits.length ; i ++)
		{
			boolean newBit = (value & (1 << i)) != 0;

			bits[i].set(newBit);
		}

	}


	public int getValue()
	{
		int value = 0;

		for (int i = 0  ; i < bits.length ; i ++)
		{
			if (bits[i].isSelected())
				value |= 1 << i;
		}

		return value;
	}

	public void setEditable(boolean mode)
	{
		for (int i = 0  ; i < bits.length ; i ++)
		{
			bits[i].setEnabled(mode);
		}
	}

	public void addActionListener(ActionListener l)
	{
		listeners.add(l);
	}


}
