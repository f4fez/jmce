/**
   $Id: Keyboard.java 810 2012-03-15 00:31:07Z mviara $

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
package jmce.philips.p2000;
import java.awt.event.*;
import jmce.sim.*;
import jmce.zilog.z80.CTC;

/**
 * Philips P2000T Keyboard.
 *
 * <p>
 * The interrupt generation of the keyboard is a little bit complex
 * because I think that the output of the keyboard scanner is connected
 * to the clock of the channel 3 of a CTC so it will generated one
 * interrupt at ever keyboard event. I'm not sure because I do not have
 * the schematics but it appear to work.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 * @since 1.01
 */
class Keyboard extends jmce.swing.KeyboardMatrix implements MemoryReadListener,MemoryWriteListener
{
	int outputRegister = 0;
	CTC ctc = null;

	public Keyboard()
	{
		setNumRows(10);
		setNumCols(8);

		setKey(0,0,KeyEvent.VK_LEFT,'6',KeyEvent.VK_UP,'Q','3','5','7','4');
		setKey(1,0,KeyEvent.VK_TAB,'H','Z','S','D','G','J','F');
		setKey(2,0,KeyEvent.VK_DECIMAL,' ',-1,-1,-1,KeyEvent.VK_DOWN,',',KeyEvent.VK_RIGHT);
		setKey(3,0,KeyEvent.VK_CONTROL,'N','<','X','C','B','M','V');
		setKey(4,0,KeyEvent.VK_F1,'Y','A','W','E','T','U','R');
		setKey(5,0,-1,'9',KeyEvent.VK_ADD,KeyEvent.VK_SUBTRACT,KeyEvent.VK_BACK_SPACE,'0','1','-');
		setKey(6,0,KeyEvent.VK_NUMPAD9,'O',KeyEvent.VK_NUMPAD8,KeyEvent.VK_NUMPAD7,KeyEvent.VK_ENTER,'P','8','@');
		setKey(7,0,KeyEvent.VK_NUMPAD3,'.',KeyEvent.VK_NUMPAD2,KeyEvent.VK_NUMPAD1,-1,'/','K','2');
		setKey(8,0,KeyEvent.VK_NUMPAD6,'L',KeyEvent.VK_NUMPAD5,KeyEvent.VK_NUMPAD4,-1,';','I',':');
		
		setKey(9,0,KeyEvent.VK_SHIFT);
		setKey(9,7,KeyEvent.VK_SHIFT);

		setKeyLocation(9,0,KeyEvent.KEY_LOCATION_LEFT);
		setKeyLocation(9,7,KeyEvent.KEY_LOCATION_RIGHT);
	}

	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		int i;

		super.registerCPU(cpu);

		for (i = 0 ; i <= 0x1F ; i++)
			cpu.addIOReadListener(i,this);

		for (i = 0x10 ; i <= 0x1f ; i++)
			cpu.addIOWriteListener(i,this);


	}

	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		if ((outputRegister & 0x40) != 0)
		{
			value = 0xff;
			for (int i = 0 ; i < 10 ; i++)
				value &= getRow(i);

		}
		else
		{
			if (address > 9)
				value = 0xff;
			else
				value = getRow(address);
		}


		return value;
	}

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		outputRegister = value;
	}

	@Override
	protected void keyEvent(int r,int c,boolean pressed)
	{
		/** Process the key */
		super.keyEvent(r,c,pressed);

		/**
		 * If the interrupt is enable pulse the counter of the
		 * channel 3 of the CTC.
		 */
		if ((outputRegister & 0x40) != 0)
		{
			try
			{
				ctc.count(3,1);
			}
			catch (Exception ignore)
			{
			}
		}
	}

	public void setCtc(CTC ctc)
	{
		this.ctc = ctc;
	}
}

