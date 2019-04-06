/**
   $Id: KeyboardPanel.java 611 2011-05-26 15:11:11Z mviara $

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

package jmce.sinclair.spectrum;


import java.awt.*;
import javax.swing.*;

/**
 * Label with a spectrum keyboard
 */
public class KeyboardPanel extends JLabel
{
	private static final long serialVersionUID = 1L;
	Color colorNormal = new Color(255,255,255);
	Color colorPressed = Color.cyan;
	
	static public final int rows_x[][] ={
		{8,67,123,180,238,296,353,408,464,518},
		{34,90,149,209,267,325,380,435,492,544},
		{49,109,166,223,277,338,397,452,505,557},
		{8,79,137,194,252,306,362,420,477,529}
	};
	static public final int rows_y[] = {127,190,257,317};
	
	Font font = new Font("MONOSPACE",Font.PLAIN,10);

	public KeyboardPanel()
	{
		setIcon(jmce.swing.Util.getIcon(KeyboardPanel.class,"zxkey.gif"));
		//enableEvents(AWTEvent.KEY_EVENT_MASK);
		//enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
	}
	

	private boolean pressed[][] = new boolean[4][10];

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setFont(font);
		/*
		for (int r = 0 ; r < 4 ; r++)
			for (int c = 0; c < 10 ; c ++)
			{
				if (pressed[r][c])
					g.setColor(colorPressed);
				else
					g.setColor(colorNormal);
				
				ch[0] = (char)SwingKeyboard.keys[r][c];
				g.drawChars(ch,0,1,rows_x[r][c]+40,rows_y[r]+45);
			}
			*/
	}

	public boolean isFocusable()
	{
		return true;
	}

	public void setPressed(int r,int c,boolean mode)
	{
		pressed[r][c] = mode;
		repaint();
	}

	public void setPressed(boolean mode)
	{
		for (int r = 0 ;r < 4 ; r++)
			for (int c = 0 ; c < 10 ; c ++)
				pressed[r][c] = false;
		repaint();
	}
	
}

