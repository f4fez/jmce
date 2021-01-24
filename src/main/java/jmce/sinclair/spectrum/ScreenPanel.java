/**
   $Id: ScreenPanel.java 814 2012-03-29 11:07:49Z mviara $

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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JComponent;

import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.util.Logger;
import jmce.util.RingBuffer;

/**
 * JComponent to rappresent the spectrum screen.
 *
 * @author Mario Viara
 * @version 1.02
 */
public class ScreenPanel extends JComponent  implements SpectrumConstants,ActionListener,MemoryWriteListener
{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ScreenPanel.class);

	/** Screen definition */
	private static final int screenWidth	= 256;
	private static final int screenHeight	= 192;
	private static final int topBorder	= 24;
	private static final int bottomBorder   = 24;
	private static final int leftBorder	= 24;
	private static final int rightBorder	= 24;
	

	private int pixelScale			= 2;


	// R,G,B Value used for color
	public static final int COLOR		= 0xbf;

	// R,G,B Value used for bright color
	public static final int COLORBRIGHT	= 0xff;

	private Dimension size = null;
	private Image image = null;
	private HashMap<Integer,Image> patterns = new HashMap<Integer,Image>();

	/** Color map */
	static private Color colors[] = new Color[16];


	/** Ring buffer with video memories changes */
	private RingBuffer<Integer> memoryChanged = new RingBuffer<Integer>(SCREEN_MEMORY_SIZE*4);
	
	/** New color border */
	private Color colorBorder = new Color(0,0,0);

	/** Current color border */
	private Color oldBorder = new Color(0,0,0);
	
	// Flash state
	private boolean flash = false;

	/** Screen memory */
	private Memory m;
	

	/** At first load define the color map */
	static
	{
		/** Normal color */
		colors[0] = new Color(0,0,0);
		colors[1] = new Color(0,0,COLOR);
		colors[2] = new Color(COLOR,0,0);
		colors[3] = new Color(COLOR,0,COLOR);
		colors[4] = new Color(0,COLOR,0);
		colors[5] = new Color(0,COLOR,COLOR);
		colors[6] = new Color(COLOR,COLOR,0);
		colors[7] = new Color(COLOR,COLOR,COLOR);

		/** Bright color */
		for (int i = 0 ; i < 8 ; i++)
			colors[8+i] = new Color(colors[i].getRed()	!= 0 ? COLORBRIGHT : 0,
						colors[i].getGreen()	!= 0 ? COLORBRIGHT : 0,
						colors[i].getBlue()	!= 0 ? COLORBRIGHT : 0);

	}

	/** Default and unique constructor */
	public ScreenPanel()
	{

		javax.swing.Timer timer = new javax.swing.Timer(20,this);
		timer.setRepeats(true);
		timer.start();
		size = new Dimension((screenWidth+leftBorder+rightBorder)*pixelScale,(screenHeight+topBorder+bottomBorder)*pixelScale);
		setPreferredSize(size);
	}

	/**
	 * Set the border
	 *
	 * @since 1.02
	 */
	public void setBorder(int border)
	{
		colorBorder = colors[border];
	}


	private Image getImage(JComponent comp ,int pixel,int color)
	{
		/** Check for flash */
		if (flash & (color & ATTRIBUTE_FLASH) != 0)
		{
			int c1 = color & 7;
			int c2 = (color >> 3) & 7;
			color &= 0xC0;
			color |= c1 << 3 | c2;
		}

		color &= 0x7f;

		// Calculate hashing using pixel and color
		int hashValue = pixel << 8 | color;
		Integer keyImage = new Integer(hashValue);
		Image img = patterns.get(keyImage);


		// Image not found draw it
		if (img == null)
		{
			int base = 0;
			Color bg;
			Color fg;

			if ((color & ATTRIBUTE_BRIGHT) != 0)
				base = 8;


			// Get color
			bg = colors[base+((color >> 3) & 7)];
			fg = colors[base+(color & 7)];

			// Create one image
			img = comp.createImage(8*pixelScale,1*pixelScale);
			Graphics g = img.getGraphics();

			// Draw the pattern
			for (int c = 0 ; c < 8 ; c++)
			{
				Color clr = bg;

				if ((pixel & (0x80 >> c)) != 0)
					clr = fg;
				g.setColor(clr);
				g.fillRect(c*pixelScale,0,pixelScale,1*pixelScale);

			}

			patterns.put(keyImage,img);
		}

		return img;
	}


	public void	writeMemory(Memory m,int add,int value,int oldValue) throws SIMException
	{
		if (add >= SCREEN_MEMORY_START && add <= SCREEN_MEMORY_END)
			repaintScreen(add - SCREEN_MEMORY_START);
		else if (add >= SCREEN_ATTRIBUTE_START && add <= SCREEN_ATTRIBUTE_END)
			repaintAttribute(add - SCREEN_ATTRIBUTE_START);

	}

	/**
	 * Set the video memory
	 *
	 * @param m - The new video memory
	 */
	public void setMemory(Memory m)
	{
		this.m = m;
		m.removeMemoryWriteListener(this);
		m.addMemoryWriteListener(this);

		/** Repaint the whole screen */
		for (int i = 0 ; i < SCREEN_MEMORY_SIZE ; i++)
			repaintScreen(i);

	}

	private int getMemory(int offset)
	{

		try
		{
			return m.getMemory(offset);
		}
		catch (Exception e)
		{
			log.warning(e);
		}

		return 0xff;
	}

	private void drawByte(int addr,Graphics g)
	{
		int pixel = getMemory(addr);
		int x = ((addr    & 0x001f) << 3);
		int y = (((addr   & 0x00e0)) >> 2) + 
			  (((addr & 0x0700)) >> 8) +
			  (((addr & 0x1800)) >> 5);
		int X = (x+leftBorder)*pixelScale;
		int Y = (y+topBorder)*pixelScale;

		int attr = getMemory(SCREEN_MEMORY_SIZE+ (addr&0x1f) + ((y>>3)*32) ) & 0xff;
		Image chars = getImage(this,pixel,attr);
		g.drawImage(chars,X,Y,null);
		
	}

	private void drawScreen()
	{
		Graphics g = image.getGraphics();

		/**
		 * Draw the border if changed
		 */
		if (colorBorder != oldBorder)
		{
			oldBorder = colorBorder;
			
			g.setColor(colorBorder);
			g.fillRect(0,0,leftBorder*pixelScale,size.height);
			g.fillRect(0,0,size.width,pixelScale*topBorder);
			g.fillRect(size.width - rightBorder*pixelScale,0,rightBorder*pixelScale,size.height);
			g.fillRect(0,size.height  - bottomBorder*pixelScale,size.width,pixelScale*bottomBorder);
		}	

		for (Integer n = memoryChanged.get() ; n != null ; n = memoryChanged.get())
			drawByte(n,g);
	}

	public void paint(Graphics g)
	{
		if (image == null)
			image = createImage(size.width, size.height);

		drawScreen();

		g.drawImage(image, 0, 0, this);

	}



	/**
	 * Polling called every 320 ms
	 */
	public void updateBlink()
	{
		// Reverse flash attribute
		flash = ! flash;
		
		for (int i = 0; i < SCREEN_ATTRIBUTE_SIZE ; i++)
		{

			if ((getMemory(i+SCREEN_MEMORY_SIZE) & ATTRIBUTE_FLASH) != 0)
			{
				repaintAttribute(i);
			}
		}

	}

	void repaintAttribute(int addr)
	{
		int        scrAddr   = ((addr & 0x300) << 3) | (addr & 0xff);

		for (int i = 0 ; i < 8 ; i++)
		{
			repaintScreen(scrAddr);

			// Next address in memory
			scrAddr += 256;
		}
	}

	void repaintScreen(int add)
	{
		memoryChanged.put(add);
	}

	/**
	 * Timer called every 20 ms to update the screen
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (memoryChanged.getSize() > 0 || colorBorder != oldBorder)
			repaint();
	}


	void setScale(int scale)
	{
		pixelScale = scale;
	}

	public String toString()
	{
		return "Spectrum Screen";
	}
	

}
