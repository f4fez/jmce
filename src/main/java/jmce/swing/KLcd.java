/**
   $Id: KLcd.java 695 2011-09-21 06:09:11Z mviara $

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

import jmce.util.Timer;
import jmce.util.TimerListener;

import java.awt.*;
import javax.swing.*;




/**
 * Swing component for LCD characters display.
 * <p>
 * This component rappresent a LCD alphanumeric display.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class KLcd extends JComponent
{
	private static final long serialVersionUID = 1L;

	/** Number of pages */
	private int numPages = 2;

	/** Current displayed page */
	private int displayPage = 0;

	/** Current active page */
	private int currentPage = 0;
	
	/** Size of 1 LCD dot in pixel */
	private int dotSize = 2;

	/** Numer of rows */
	private int numRows = 4;

	/** Number of columns */
	private int numColumns = 20;

	/** Horizontal space beetween char in dot*/
	private int hSpace = 1;

	/** Vertical space between char in dot */
	private int vSpace = 1;

	/** Current font */
	BitmapFont font = null;


	/** Color used for background */
	private Color colorBack = new Color(0,0,255);

	/** Color used for foreround */
	private Color colorFront = new Color(255,255,255);

	/** Color used for space */
	private Color colorSpace = new Color(0,0,127);
	
	/** Array with an image for all char */
	private Image imageChar[] = new Image[256];

	/** Array with the screen memory */
	private int memory[];
	
	/** Dimension of the component */
	private Dimension size = null;

	/** Current cursor position */
	private int row,col;

	/** Cursor visible */
	private boolean cursor;

	/** Display on */
	private boolean on = true;
	

	/** Cursor blinking */
	private boolean cursorBlink = true;

	/** Cursor status for blink */
	private boolean cursorBlinkStatus = true;
	
	public Dimension getPreferredSize()
	{
		if (size == null)
			initInternal();

		return size;
	}
	
	/**
	 * Initialize all internal variable. This method must be called
	 * when the component is correctly initialized and displayable.
	 */
	private void initInternal()
	{
		/** Determinte the component size */
		size = new Dimension((numColumns*font.getWidth()+(numColumns+1)*hSpace)*dotSize,
				     (numRows*font.getHeight()+(numRows+1)*hSpace)*dotSize);

		/** Initialize the image array */
		for (int i = 0 ; i < 256 ; i++)
		{
			Image img = createImage(font.getWidth()*dotSize,font.getHeight()*dotSize);
			Graphics g = img.getGraphics();
			g.setColor(colorBack);
			g.fillRect(0,0,font.getWidth()*dotSize,font.getHeight()*dotSize);
			g.setColor(colorFront);
			for (int x = 0 ; x < font.getWidth() ; x++)
				for (int y = 0 ; y < font.getHeight() ; y++)
				{
					if (font.getPixel(i,x,y))
						g.fillRect(x*dotSize,y*dotSize,
							dotSize,dotSize);
				}
			imageChar[i] = img;
			
		}

		/** Create the array with the display memory */
		memory = new int[numPages*numRows*numColumns];

		resetDisplay();

		Timer timer = new Timer(410,true,new TimerListener()
		{
			public void timerExpired()
			{
				if (cursorBlink)
				{
					cursorBlinkStatus = !cursorBlinkStatus;
					repaint();
				}
			}
		});

		Timer.addTimer(timer);

	}

	/**
	 * Reset the display. <p>
	 * 
	 * Reset the display set all parameter to default and fill the
	 * display with blank.
	 */
	void resetDisplay()
	{
		for (int i = 0 ; i < memory.length ; i++)
			memory[i] = ' ';

		setCursor(0,0);
		setCursor(true);
		setPage(0);
		repaint();
	}

	/**
	 * Set the cursor position.
	 */
	public void setCursor(int row,int col)
	{
		this.row = row;
		this.col = col;
		if (cursor)
			repaint();
	}

	/**
	 * Set the display on /off.
	 * <p>
	 * Set  the display on / off. If the display is off ir will
	 * continue to work and update the internal ram but do not
	 * display nothing.
	 *
	 * @param mode - If true set the display on.
	 * 
	 */
	public void setDisplay(boolean mode)
	{
		if (on != mode)
		{
			on = mode;
			repaint();
		}
	}

	/**
	 * Set the cursor blinking mode.
	 * <p>
	 * @param mode - If true the display will blink every 406 ms
	 * else it will be displayed as a block.
	 */
	public void setCursorBlink(boolean mode)
	{
		if (mode != cursorBlink)
		{
			cursorBlink = mode;
			repaint();
		}
	}

	/**
	 * Set the cursor on / off.
	 * <p>
	 * @param mode - If true the cursor will be visible.
	 * 
	 * @see #setCursorBlink
	 */
	public void setCursor(boolean mode)
	{
		if (mode != cursor)
		{
			this.cursor = mode;
			repaint();
		}
			
	
	}

	
	public void setPage(int p)
	{
		setCurrentPage(p);
		setDisplayPage(p);
	}

	public void setCurrentPage(int p)
	{
		currentPage = p;
	}

	public void setDisplayPage(int p)
	{
		if (displayPage != p)
		{
			displayPage = p;
			repaint();
		}
	}
	
	public void paintComponent(Graphics g)
	{
		int base = displayPage * numRows * numColumns;
		Insets insets = getInsets();
		g.translate(insets.left,insets.top);
		g.setColor(colorSpace);
		g.fillRect(0,0,size.width,size.height);
		int x,y;

		if (!on)
			return;
		
		y = vSpace * dotSize;
		for (int r = 0 ; r < numRows ; r++)
		{
			
			x = hSpace*dotSize;
			for (int c = 0 ; c < numColumns ; c++)
			{
				g.drawImage(imageChar[memory[base+r*numColumns+c]],x,y,null);
				

				/** Show the cursor */
				if (cursor && c == col && r == row && displayPage == currentPage)
				{
					if (cursorBlink)
					{
						if (cursorBlinkStatus)
						{
							g.setColor(colorFront);
							g.fillRect(x,y,font.getWidth()*dotSize,font.getHeight()*dotSize);
							//g.drawImage(imageChar[32],x,y,null);
						}
					}
					else
					{
						g.setColor(colorFront);
						for (int i = 0 ; i < dotSize ; i++)
						{
							int x1,y1,x2,y2;
							x1 = x;
							y1 = y +(font.getHeight()) * dotSize -i ;
							x2 = x + font.getWidth()*dotSize;
							y2 = y1;
							g.drawLine(x1,y1,x2,y2);
						}

					}
				}

				x += (font.getWidth()+hSpace) * dotSize;

			}

			y += (font.getHeight()+vSpace) * dotSize;
		}
		
	}

	public void putchar(int ch)
	{
		int base = currentPage * numRows * numColumns;

		ch &= 0xff;

		memory[base + row * numColumns + col] = ch;
		if (++col >= numColumns)
		{
			col = 0;
			if (++row >= numRows)
				row = 0;
		}

		if (currentPage == displayPage)
			repaint();
		
	}

	public void setFont(BitmapFont f)
	{
		font = f;
	}
	
	


	public int getNumColumns()
	{
		return numColumns;
	}

	public void setNumColumns(int n)
	{
		numColumns = n;
	}

	public int getNumRows()
	{
		return numRows;
	}

	public void setNumRows(int n)
	{
		numRows = n;
	}

	
	public KLcd()
	{
		setFont(new BitmapFont6x8());
	}
	
	static public void main(String argv[])
	{
		JFrame f = new JFrame("Test KLCD $Id: KLcd.java 695 2011-09-21 06:09:11Z mviara $");
		KLcd lcd = new KLcd();
		KMatrixKeyboard kbd = new KMatrixKeyboard();
		JPanel p = new JPanel();
		p.add(lcd);
		p.add(kbd);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		
		for (int r = 0,i = 65 ; r < lcd.numRows/2 ; r++)
			for (int c = 0 ; c < lcd.numColumns ; c++)
			{
				lcd.putchar(i++);
			}

		lcd.setCursor(1,5);
	}

	
}
