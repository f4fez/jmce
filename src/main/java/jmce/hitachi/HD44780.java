/**
   $Id: HD44780.java 692 2011-09-02 08:38:10Z mviara $

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
package jmce.hitachi;

import jmce.sim.SIMException;
import jmce.sim.Memory;
import jmce.sim.MemoryReadListener;
import jmce.sim.MemoryWriteListener;

/**
 * Hitachi HD44780 display controller<p>
 *
 * This class emulate a character display controller hitachi HD44780
 * and compatible.
 * <p>
 * <p>
 * Supported features :
 * <ul>
 *  <li>4 or 8 bit transfer</li>.
 *  <li>Home command.
 *  <li>Clear command</li>
 *  <li>Memory write</li>
 *  <li>Memory read</li>
 *  <li>Cursor on/off</li>
 *  <li>Cursor blinking</li>
 *  <li>Display on/off</li>
 * </ul>
 * <p>
 * Unsupported features :
 * <ul>
 *  <li>Character generator because the font used do not have the
 *  correct size.</li>
 *  <li>Display shift.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 */
public class HD44780 extends jmce.swing.KLcd implements MemoryReadListener,MemoryWriteListener
{
	private static final long serialVersionUID = 1L;
	private int cmdPort = 0;
	private int dataPort = 1;
	private int ddramAddress;
	private int displayRam[] = new int[128];
	private int halfByte = 0;
	private boolean half = false;
	private boolean use8 = true;
	private boolean autoIncDec = true;
	private boolean autoInc = true;
	private boolean ddram = true;

	/**
	 * Default constructor
	 */
	public HD44780()
	{
		setNumRows(2);
		setNumColumns(16);
		setFont(new jmce.swing.BitmapFont8x14());
	}

	/**
	 * Reset the controller.
	 */
	private void hd44780Reset()
	{
		ddramAddress = 0;
		hd44780Clear();

	}

	/**
	 * Clear display memory
	 */
	private void hd44780Clear()
	{
		for (int i = 0 ; i < displayRam.length ; i++)
			displayRam[i] = ' ';
		hd44780Repaint();
	}

	/**
	 * Repaint the component
	 */
	private void hd44780Repaint()
	{
		for (int r = 0 ; r < getNumRows() ; r++)
		{
			int pos = 0;

			for (int c = 0 ; c < getNumColumns() ; c++)
			{
				switch (r)
				{
					case	0:
						pos = 0 + c;
						break;
					case	1:
						pos = 0x40 + c;
						break;
					case	2:
						pos = 0x14 + c;
						break;
					case	3:
						pos = 0x54 + c;
						break;


				}

				setCursor(r,c);
				putchar(displayRam[pos]);
			}

		}

		/** Set the cursor */
		if     (ddramAddress >= 0x00 && 0x00 + ddramAddress < getNumColumns())
			setCursor(0,ddramAddress);

	}

	/**
	 * Read a byte from the command port
	 */
	private int hd44780Command()
	{
		/** Always not busy */
		return 0;
	}

	/**
	 * Write a byte to the command port
	 */
	private void hd44780Command(int cmd)
	{
		/** Set ddram address */
		if ((cmd & 0x80) != 0)
		{
			ddramAddress = cmd & 0x7f;
			ddram = true;
			hd44780Repaint();
		}
		/** Set cgram Address */
		else if ((cmd & 0x40) != 0)
		{
			ddram = false;
		}
		/** Function set */
		else if ((cmd & 0x20) != 0)
		{
			if ((cmd & 0x10) != 0)
			{
				use8 = true;
			}
			else
			{
				use8 = false;
				half = false;
			}
		}
		/** Shift display / cursor */
		else if ((cmd & 0x10) != 0)
		{
			/** Cursor shift */
			if ((cmd & 0x08) == 0)
			{
				if ((cmd & 0x08) == 0)
				{
					if (++ddramAddress >= displayRam.length)
						ddramAddress = 0;
				}
				else if (--ddramAddress < 0)
					ddramAddress = displayRam.length - 1;
				hd44780Repaint();

			}
		}
		/** Display on / off */
		else if ((cmd & 0x08) != 0)
		{
			setDisplay((cmd & 0x04) != 0);
			setCursor((cmd & 0x02) != 0);
			setCursorBlink((cmd & 0x01) != 0);
		}
		/** Set auto increment */
		else if ((cmd & 0x04) != 0)
		{
			autoIncDec = ((cmd & 0x02) != 0);
			autoInc    = ((cmd & 0x01) == 0);
		}
		/** Home */
		else if ((cmd & 0x02) != 0)
		{
			ddramAddress = 0;
			hd44780Repaint();
		}
		/** Clear */
		else if ((cmd & 0x01) != 0)
		{
			hd44780Reset();
		}
	}

	/**
	 * Read byte from data port
	 */
	int hd44780Data()
	{
		int data = displayRam[ddramAddress];
		
		if (autoIncDec)
		{
			if (autoInc)
			{
				if (++ddramAddress >= displayRam.length)
					ddramAddress = 0;
			}
			else if (--ddramAddress < 0)
				ddramAddress = displayRam.length;
		}

		return data;
	}

	/**
	 * Write byte to data port
	 */
	void hd44780Data(int data)
	{
		if (ddram)
		{
			displayRam[ddramAddress] = data;
			if (autoIncDec)
			{
				if (autoInc)
				{
					if (++ddramAddress >= displayRam.length)
						ddramAddress = 0;
				}
				else if (--ddramAddress < 0)
					ddramAddress = displayRam.length;
			}

			hd44780Repaint();
		}
		/** For now  ignore write on CG address */

	}

	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		/** Manage 8 bit interface */
		if (!use8)
		{
			if (half)
			{
				half = false;
				return halfByte;
			}
		}

		if (address == cmdPort)
			value = hd44780Command();
		else if (address == dataPort)
			value = hd44780Data();

		if (half)
		{
			halfByte = (value << 4) & 0xf0;
			value &= 0xF0;
		}
		
		return value;
	}
	
	public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		/** 4 bit interface */
		if (!use8)
		{
			if (half)
			{
				value = (value >>> 4) | halfByte;
				half = false;
			}
			else
			{
				halfByte = value & 0xf0;
				half = true;
				return;
			}
		}

		if (address == cmdPort)
			hd44780Command(value);
		else if (address == dataPort)
			hd44780Data(value);
	}

}


