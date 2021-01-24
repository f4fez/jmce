/**
   $Id: Display.java 601 2011-05-25 08:24:49Z mviara $

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
package jmce.viara.v8052;

import java.awt.*;

import javax.swing.*;
import jmce.sim.*;

/**
 * Display 4x20 connected to the V8052.
 * <p>
 * <ul>
 *  <li>P0..7	8 bit data Display</li>
 *  <li>P1.0	LCD RW (1 Read, 0 Write)</li>
 *  <li>P1.1	LCD RS (0 Cmd, 1 Data)</li>
 *  <li>P1.2	LCD EN (1 Enable display)</li>
 * </ul>
 * <p>
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.02
 * 
 */
public class Display extends AbstractPeripheral implements SwingHardware
{
	static public final int LCD_RW = 0x01;
	static public final int LCD_RS = 0x02;
	static public final int LCD_EN = 0x04;

	private JPanel p = null;

	private jmce.hitachi.HD44780 lcd = new jmce.hitachi.HD44780();

	private int p0,p1;

	public Display()
	{
		setName("Display 4x20");
		lcd.setNumRows(4);
		lcd.setNumColumns(20);

	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addIOWriteListener(V8052.P0,new MemoryWriteListener()
		{
			public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
			{
				writeP0(memory,value);
			}
		});

		cpu.addIOWriteListener(V8052.P1,new MemoryWriteListener()
		{
			public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
			{
				writeP1(memory,value);
			}
		});

		cpu.addIOReadListener(V8052.P0,new MemoryReadListener()
		{
			public int readMemory(Memory memory,int address,int value) throws SIMException
			{
				return p0;
			}
		});


	}

	/**
	 * Write listener called when P0 is written
	 */
	public void writeP0(Memory memory,int value)
	{
		p0 = value;
	}

	/**
	 * Write listener called when P1 is Written
	 */
	public void writeP1(Memory memory,int value) throws SIMException
	{
		p1 = value;


		/** Display enabled ? */
		if ((p1 & LCD_EN) != 0)
		{
			int address = (p1 & LCD_RS) != 0 ? 1 : 0;

			/** Write ? */
			if ((p1 & LCD_RW) == 0)
				lcd.writeMemory(memory,address,p0,~p0);
			else
				p0 = lcd.readMemory(memory,address,p0);
		}
	}

	public java.awt.Component getComponent()
	{
		if (p == null)
		{
			p = new JPanel(new GridBagLayout());
			GridBagConstraints g = new GridBagConstraints();
			g.gridx = 0; g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
			g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE; g.insets = new Insets(2,2,2,2);

			p.add(lcd,g);g.gridx = 0; g.gridy++;


		}

		return p;
	}
}
