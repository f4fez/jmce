/**
   $Id: KLed.java 692 2011-09-02 08:38:10Z mviara $

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
import javax.swing.*;

/**
 * Swing component to rappresent a LED.
 *
 * When on a filled circle is drawed when off draw only the border
 * always using the current foregorund color.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class KLed extends JComponent implements Repaintable
{
	private static final long serialVersionUID = 1L;
	
	private int width = 16;
	private int border = 3;
	private boolean led = false;
	private boolean polarity = true;

	/* Default color */
	static private Color defaultColor = Color.RED;
	
	/**
	 * Default constructor
	 */
	public KLed()
	{
		this(defaultColor);
	}

	/**
	 * Constructor with the specificed color
	 */
	public KLed(Color color)
	{
		this(color,20);
	}

	/**
	 * Constructor with the color and led width.
	 */
	public KLed(Color color ,int w)
	{
		this(color,w,w/5);
	}

	/**
	 * Complete constructor.
	 *
	 * @param color - Color of the led.
	 * @param w - Total width.
	 * @param b - Border size;
	 */
	public KLed(Color color,int w,int b)
	{
		setForeground(color);
		setBackground(Color.black);
		width = w;
		border = b;
		setOpaque(true);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(width,width);
	}

	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension(width,width);
	}

	
	public final void paintComponent(Graphics g)
	{
		Insets insets = getInsets();

		g.translate(insets.left,insets.top);

		g.setColor(getBackground());
		g.fillRect(0,0,width,width);
		g.setColor(getForeground());

		if (led == polarity)
			g.fillOval(border,border,width - border * 2,width - border * 2);
		else
			g.drawOval(border,border,width - border * 2,width - border * 2);
	}

	/**
	 * Set the led status.
	 */
	public final void setLed(boolean mode)
	{
		if (mode != led)
		{
			led = mode;
			Util.repaintLater(this);
		}

	}

	/**
	 * Return the led status
	 */
	public final boolean getLed()
	{
		return led;
	}


	/**
	 * Set the polarity of the led.
	 *
	 * @param mode - True the led is on when true, false the led is
	 * on when false.
	 */
	public final void setPolarity(boolean mode)
	{
		polarity = mode;
		repaint();
	}


	public void updateComponent()
	{
		repaint();
	}

	
}

