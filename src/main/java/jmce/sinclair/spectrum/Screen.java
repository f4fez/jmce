/**
   $Id: Screen.java 811 2012-03-18 12:16:14Z mviara $

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

import jmce.sim.*;

import java.awt.*;
import javax.swing.*;

/**
 * Spectrum screen.
 *<p>
 * This device implements the Screen and the keyboard for the spectrum
 * and can optionally show the Spectrum keyboard if the property
 * <tt>keyboardVisible</tt> is true.
 * 
 * @author Mario Viara
 * @version 1.01
 * 
 * @see #setKeyboardVisible
 */
public class Screen extends AbstractPeripheral implements SwingHardware, SpectrumConstants
{
	Component comp = null;
	ScreenPanel	sp = new ScreenPanel();
	private int scale = 1;
	private boolean keyboardVisible = false;
	private int border;

	/**
	 * Default constructor
	 */
	public Screen()
	{
		this(1);
	}

	/**
	 * Constructor with specified scale
	 */
	public Screen(int scale)
	{
		setName("Screen 256x192");
		setScale(scale);
	}

	public boolean getKeyboardVisible()
	{
		return keyboardVisible;
	}

	public void setKeyboardVisible(boolean b)
	{
		keyboardVisible = b;
	}
		
	public int getScale()
	{
		return scale;
	}

	/**
	 * Set the color of screen border
	 *
	 * @since 1.02
	 */
	public void setBorder(int border)
	{
		sp.setBorder(border);
	}
	
	public void setScale(int scale)
	{
		this.scale = scale;
		sp.setScale(scale);
	}
	
	public java.awt.Component getComponent()
	{
		return comp;
	}

	public void setMemory(Memory m)
	{
		sp.setMemory(m);
	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		SpectrumMemory m = (SpectrumMemory)cpu.getHardware(SpectrumMemory.class);
		if (m == null)
			throw new SIMSWException("No SpectrumMemory installed!");

		setMemory(m.getVideoMemory());
		
		jmce.util.Timer timer = new jmce.util.Timer(320,true,new jmce.util.TimerListener()
		{
			public void timerExpired()
			{
				sp.updateBlink();
			}
		});

		cpu.addTimerMs(timer);

	}

	public void init(Hardware parent) throws SIMException
	{


		JPanel p = new JPanel();
		p.add(sp);

		if (keyboardVisible)
			p.add(new KeyboardPanel());
		
		comp = p;

		if (getHardware(SwingKeyboard.class) == null)
			addHardware(new SwingKeyboard());

		super.init(parent);

	}
	
	
}


