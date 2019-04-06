/**
   $Id: SwingKeyboard.java 630 2011-06-09 07:12:05Z mviara $

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
import java.awt.event.*;
import jmce.sim.*;

/**
 * AWT keyboord for spectrum.
 * <p>
 * This device can be child of any <tt>SwingHardare</tt> and implements
 * the spectrum keyboard on the parent component. 
 *
 * @author Mario Viara
 * @version 1.00
 */
public class SwingKeyboard extends AbstractPeripheral implements FocusListener,KeyListener,SpectrumConstants
{
	private Spectrum spectrum;
	
	/** Keyboard mapping */
	static public final int keys[][] =
	{
		{'1','Q','A','0','P',KeyEvent.VK_SHIFT,KeyEvent.VK_ENTER,' '},
		{'2','W','S','9','O','Z','L',KeyEvent.VK_CONTROL},
		{'3','E','D','8','I','X','K','M'},
		{'4','R','F','7','U','C','J','N'},
		{'5','T','G','6','Y','V','H','B'},
	};

	/** Column map because the columns address line are not in order*/
	static final int rows[] ={11,10,9,12,13,8,14,15};

	private int keyState[] = new int[8];
	private Component comp = null;


	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);

		
		if (parent instanceof SwingHardware)
		{
			comp = ((SwingHardware)parent).getComponent();
			
			if (comp != null)
			{
				comp.setFocusable(true);
				comp.addFocusListener(this);
				comp.addKeyListener(this);

			}
			
		}

	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		spectrum = (Spectrum)cpu;
		
		spectrum.addIOReadListener(ULA_PORT,new MemoryReadListener()
		{
			public int	readMemory(Memory m,int add,int value) throws SIMException
			{
				return inb(add,spectrum.getPortHI());
			}

		});
	}


	public int inb(int port,int hi)
	{
		int result = spectrum.getUla() | ULA_KBD;

		port &= 0xff;
		port |= (hi & 0xff) << 8;


		for (int i = 0 ; i < 8 ; i++)
			if ((port & (1 << rows[i])) == 0)
			{
				result &= keyState[i];
			}
		
		return result;
	}

	public void keyPressed(KeyEvent e)
	{
		dokey(e.getKeyCode(),true);
		e.consume();

	}

	public void dokey(int key,boolean mode)
	{
		for (int r = 0 ; r < 5 ; r++)
			for (int c = 0 ; c < 8 ; c ++)
				if (keys[r][c] == key)
				{
					dokey(r,c,mode);
					break;
				}
	}

	public void keyReleased(KeyEvent e)
	{

		dokey(e.getKeyCode(),false);
		e.consume();

	}


	public void keyTyped(KeyEvent e)
	{
	}

	public void dokey(int r,int c,boolean pressed)
	{
		if (pressed)
			keyState[c] &= ~(1 << r);
		else
			keyState[c] |= (1 << r);
	}


	private void keyboardReset()
	{
		for (int i = 0 ; i < 8 ; i++)
			keyState[i] = 0xff;
	}

	public void focusGained(FocusEvent e)
	{
		keyboardReset();
	}

	public void focusLost(FocusEvent e)
	{
		
	}



}
