/**
   $Id: SwingCRT.java 596 2011-05-24 07:12:27Z mviara $

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
package jmce.sim.terminal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmce.sim.*;

/**
 * Swing implementation of CRT.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class SwingCRT extends AbstractCRT implements jmce.sim.SwingHardware
{
	private final int TIME_REFRESH = 20;
	private final int TIME_BLINK = 500;
	
	private Image image = null;
	private FontMetrics fm;
	private Dimension size,sizeChar;
	private Color color = new Color(0,255,0);
	private Color colorHi = new Color(255,255,255);
	private javax.swing.Timer timer1;
	private javax.swing.Timer timer2;
	private boolean blinkDisable = false;
	private JSwingCRT component = null;
	private char line[];
	
	class JSwingCRT extends JComponent implements KeyListener
	{

		private static final long serialVersionUID = 1L;

		public JSwingCRT()
		{
			enableEvents(AWTEvent.KEY_EVENT_MASK);
			enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
			addKeyListener(this);
			setFocusable(true);

			addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					requestFocusInWindow();
				}


			});
		}

		public boolean hasFocus()
		{
			return true;
		}

		public boolean isFocusable()
		{
			return true;
		}

		public void keyPressed(KeyEvent e)
		{
			try
			{
				// Special case ENTER
				if (e.getKeyChar() == KeyEvent.VK_ENTER && e.getKeyChar() == e.getKeyCode())
				{
					try
					{
						
						terminal.write(e.getKeyCode() | 0x10000);
					}
					catch (Exception ignore)
					{
					}
				}
				else
				{
					if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
					{
						terminal.write(e.getKeyCode() | 0x10000);
					}
					else
					{
						terminal.write((int)e.getKeyChar() & 0xff);
					}
				}
			}
			catch (Exception ignore)
			{
			}
			
		}


		public void keyReleased(KeyEvent e)
		{
		}


		public void keyTyped(KeyEvent e)
		{
			
		}

		public Dimension getPreferredSize()
		{
			if (size == null)
			{
				Graphics g = getGraphics();
				g.setFont(terminal.getFont());
				fm = g.getFontMetrics();

				int width = 0;
				int height = fm.getHeight();
				for (int i = 0 ; i < 256 ; i++)
					if (fm.charWidth(i) > width)
						width = fm.charWidth(i);
				sizeChar = new Dimension(width,height);

				size = new Dimension(numCol*width,numRow*height);
			}

			return size;
		}


		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}

		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}


		public void paint(Graphics g)
		{
			if (image == null)
				image = createImage(size.width, size.height);

			draw(image.getGraphics());

			g.drawImage(image, 0, 0, this);

		}


		public void drawLine(Graphics g,int att,int r,int c,int count) throws SIMException
		{
			int x = c * sizeChar.width ;
			int y = sizeChar.height - fm.getDescent() + r * sizeChar.height -1;

			for (int i = 0 ; i < count ; i++)
				line[i] = (char)getChar(r,c+i);

			g.setColor(getBackground(att));
			g.fillRect(x,(sizeChar.height * r),(count) *sizeChar.width,sizeChar.height);
			g.setColor(getForeground(att));

			g.drawChars(line,0 , count, x , y);
			if ((att & Terminal.UNDERLINE) != 0)
			{
				g.fillRect(x,sizeChar.height * (r+1)-4,sizeChar.width*count,1);
			}

		}

		public Color getColor(int att)
		{
			Color c = color;

			if ((att & Terminal.HI) != 0)
				c = colorHi;

			return c;
		}

		public Color getForeground(int att)
		{
			if ((att & Terminal.BLINK) != 0 && blinkDisable)
				return getBackground(att);
			return (att & Terminal.REVERSE) == 0 ? getColor(att) : Color.black;

		}

		public Color getBackground(int att)
		{
			return (att & Terminal.REVERSE) != 0 ? getColor(att) : Color.black;
		}


		public void drawLine(Graphics g,int r)
		{
			try
			{
			int att = getAtt(r,0);
			int count = 0;
			int c = 0;

			while (c < numCol)
			{
				if (att == getAtt(r,c))
					count++;
				else
				{
					drawLine(g,att,r,c - count ,count);
					count = 1;
					att = getAtt(r,c);
				}
				c++;
			}

			drawLine(g,att,r,c - count ,count);


			if (r == terminal.getRow() && terminal.getCursor() == true)
				g.fillRect(sizeChar.width * terminal.getCol(),sizeChar.height * (terminal.getRow()+1)-3,sizeChar.width,2);
			}
			catch (Exception e)
			{
			}

		}

		private void draw(Graphics g)
		{
			Insets insets = getInsets();
			g.translate(insets.left,insets.top);
			g.setFont(terminal.getFont());

			for (int r = 0 ; r < numRow ; r++)
			{
				if (lineChanged[r])
				{
					lineChanged[r] = false;
					drawLine(g,r);
				}
			}

		}


	}
	

	public SwingCRT()
	{
		super("SwingCRT");
		component = new JSwingCRT();
	}
	

	public void setColor(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}

	public void destroy() throws SIMException
	{
		super.destroy();
		
		if (timer1 != null)
		{
			timer1.stop();
			timer1 = null;
		}

		if (timer2 != null)
		{
			timer2.stop();
			timer2 = null;
		}
		
		try
		{
			Thread.sleep(TIME_REFRESH*2);
		}
		catch (Exception e)
		{
		}

	}
	
	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);
		line = new char[numCol];
		timer1 = new javax.swing.Timer(TIME_REFRESH,new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (screenChanged)
				{

					synchronized (lineChanged)
					{
						screenChanged = false;
						component.repaint();

					}
				}
			}
			
		});
		
		timer1.setRepeats(true);
		timer1.start();

		timer2 = new javax.swing.Timer(TIME_BLINK,new ActionListener()
		{
			
			public void actionPerformed(ActionEvent e)
			{
				blinkDisable = !blinkDisable;
				for (int r = 0; r < numRow ; r++)
					for (int c = 0; c < numCol ; c++)
					{
						try
						{
						if ((getAtt(r,c) & Terminal.BLINK) != 0)
						{
							changedLine(r);
							break;
						}
						}
						catch (Exception discard)
						{
						}
					}
				
			}

		});

		timer2.setRepeats(true);
		timer2.start();
		
	}
		

	public Component getComponent()
	{
		return component;
	}
}
