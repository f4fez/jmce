/**
   $Id: Keyboard.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.commodore.vic20;
import java.awt.event.*;

import jmce.sim.*;
import jmce.sim.cpu.Binary;


/**
 * VIC20 Keyboard / Joystick
 * <p>
 * This class emulate the keyboard / joystick for VIC20.
 * 
 * <h2>Keyboard</h2>
 * All letter key are mapped to the same key and the special commodore
 * key are mapped :
 * <p>
 * <table border="1" cellpadding="2">
 *  <tr>
 *   <th>Commodore key</th>
 *   <th>Java Key</th>
 *  </tr>
 *  <tr>
 *   <td>Run/Stop</td>
 *   <td>Esc</td>
 *  </tr>
 *  
 * </table>
 * <p>
 * <h2>Joystick</h2>
 * <p>
 * The joystick is alternative to the keyboard and can be enable
 * pressing the ALT key. When the joystick is enabled the arrow key
 * simulate a joystick direction button and any other key the fire
 * button.
 * 
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
*/
public class Keyboard extends jmce.swing.KeyboardMatrix implements MemoryWriteListener
{
	private jmce.mos.VIA6522 via2,via1;
	private boolean joystick;
	private boolean up,down,left,right,fire;
	
	public Keyboard()
	{
		setNumRows(8);
		setNumCols(8);

		setKey(0,0,'1','3','5','7','9','-','\\',KeyEvent.VK_BACK_SPACE);
		setKey(1,0,KeyEvent.VK_LEFT,'W','R','Y','I','P','[',KeyEvent.VK_ENTER);
		setKey(2,0,KeyEvent.VK_CONTROL,'A','D','G','J','L',';',KeyEvent.VK_RIGHT);
		setKey(3,0,KeyEvent.VK_ESCAPE,KeyEvent.VK_SHIFT,'X','V','N',',','/',KeyEvent.VK_DOWN);
		setKey(4,0,' ','Z','C','B','M','.',KeyEvent.VK_SHIFT,KeyEvent.VK_F1);
		setKey(5,0,KeyEvent.VK_TAB,'S','F','H','K',']','=',KeyEvent.VK_F3);
		setKey(6,0,'Q','E','T','U','O','\'',KeyEvent.VK_UP,KeyEvent.VK_F5);
		setKey(7,0,'2','4','6','8','0','`',KeyEvent.VK_HOME,KeyEvent.VK_F7);

		setKeyLocation(3,1,KeyEvent.KEY_LOCATION_LEFT);
		setKeyLocation(4,6,KeyEvent.KEY_LOCATION_RIGHT);

	}

	public void setVia1(jmce.mos.VIA6522 via1)
	{
		this.via1 = via1;
	}
	
	public void setVia2(jmce.mos.VIA6522 via2)
	{
		this.via2 = via2;
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		via2.addPortBWriteListener(this);
	}

	

	public void writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		int row = (~value) & 0xff;
		value = 0xff;

		for (int i = 0 ; i < 8 ; i ++)
			if ((row & (1 << i)) != 0)
				value &= getRow(i);

		via2.writePortA(value);
	}

	/**
	 * Update the joystick state on the VIA
	 */
	private void updateJoystick()
	{
		/**
		 * Ignore joystick if not initialized
		 */
		if (via1 == null || via2 == null)
			return;
		
		try
		{
			Binary b = new Binary(via2.readPortB());
			b.setBit(7,!right);
			via2.writePortB(b.getValue());

			b.setValue(via1.readPortA());
			b.setBit(2,!up);
			b.setBit(3,!down);
			b.setBit(4,!left);
			b.setBit(5,!fire);
			via1.writePortA(b.getValue());
		}
		catch (SIMException ignore)
		{
		}
	}
	
	/**
	 * Reset the joystick. No button pressed and update the
	 * registers.
	 */
	protected void resetJoystick()
	{
		joystick = false;
		left = right =up = down = fire = false;
		updateJoystick();
	}

	@Override
	public void keyEvent(KeyEvent e,boolean pressed)
	{
		if (e.getKeyCode() == KeyEvent.VK_ALT)
		{
			if (pressed)
				joystick = !joystick;
			if (!joystick)
				resetJoystick();
			else
				up = down = left = right = fire = false;
		}

		if (joystick)
		{
			switch (e.getKeyCode())
			{
				case	KeyEvent.VK_UP:
					up = pressed;
					break;
				case	KeyEvent.VK_DOWN:
					down = pressed;
					break;
				case	KeyEvent.VK_LEFT:
					left = pressed;
					break;
				case	KeyEvent.VK_RIGHT:
					right = pressed;
					break;
				default:
					fire = pressed;
					break;
			}
			updateJoystick();
		}
		else
			super.keyEvent(e,pressed);
	}
	
	@Override
	protected void resetKeyboard()
	{
		super.resetKeyboard();
		resetJoystick();
	}
}
