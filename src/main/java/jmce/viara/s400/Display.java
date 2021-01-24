/**
   $Id: Display.java 692 2011-09-02 08:38:10Z mviara $

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
package jmce.viara.s400;

import java.awt.*;

import javax.swing.*;
import jmce.sim.*;
import jmce.swing.*;


/**
 * S400 Display<p>
 *
 * This class rappresent a display , keyboard and led from the
 * Bytechnology S400.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Display extends AbstractPeripheral implements MemoryWriteListener,MemoryReadListener,SwingHardware
{
	private static final int DISPLAY_RS   = 0x01;
	private static final int DISPLAY_READ = 0x02;
	private static final int DISPLAY_EN   = 0x04;
	private jmce.hitachi.HD44780 lcd = new jmce.hitachi.HD44780();
	S400 s4;
	private JPanel p = new JPanel(new GridBagLayout());
	private KLed redLed,greenLed;
	private KMatrixKeyboard kbd = new KMatrixKeyboard();
	
	public Display()
	{
		setName("Lcd 2x16");

		redLed = new KLed(Color.RED);
		greenLed = new KLed(Color.GREEN);
		
		kbd.setNumRows(4);
		kbd.setNumCols(5);
		
		kbd.setKey(0,0,'1');
		kbd.setKey(0,1,'2');
		kbd.setKey(0,2,'3');
		kbd.setComponent(0,3,greenLed);
		kbd.setKey(0,4,'E');

		kbd.setKey(1,0,'4');
		kbd.setKey(1,1,'5');
		kbd.setKey(1,2,'6');
		kbd.setComponent(1,3,redLed);
		kbd.setKey(1,4,'T');

		kbd.setKey(2,0,'7');
		kbd.setKey(2,1,'8');
		kbd.setKey(2,2,'9');
		kbd.setKey(2,3,'0');
		kbd.setKey(2,4,'E');

		jmce.swing.Util.setBox(lcd);
		jmce.swing.Util.setBox(kbd);
		
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0; g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE; g.insets = new Insets(2,2,2,2);

		p.add(lcd,g);g.gridx = 0; g.gridy++;
		p.add(kbd,g);

	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		s4 = (S400)cpu;
		s4.addIOWriteListener(S400.P4,this);
		s4.addIOWriteListener(S400.P5,this);
		s4.addIOWriteListener(S400.P2,this);
	}

	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		return value;
	}


	public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{

		if (address == S400.P5)
		{

			// Check for Enable transaction
			if ((value & DISPLAY_EN) != 0 &&
			    (oldValue & DISPLAY_EN) == 0)
			{
				boolean read = (value & DISPLAY_READ) != 0;
				int a = value & DISPLAY_RS;
				int data;
				
				if (read)
				{
					data = lcd.readMemory(memory,a,0);
					//log.info("Read A="+a+" DATA="+Hex.formatByte(data));
					s4.sfr(S400.P4,data);
				}
				else
				{
					data = s4.sfr(S400.P4);
					//log.info("Write A="+a+" DATA="+Hex.formatByte(data));
					lcd.writeMemory(memory,a,data,0);
				}
			}
			
		}
		/** Check for led on/off */
		else if (address == S400.P2)
		{
			redLed.setLed((value & 0x04) != 0);
			greenLed.setLed((value & 0x08) != 0);
		}
	}

	public java.awt.Component getComponent()
	{
		return p;
	}

}
