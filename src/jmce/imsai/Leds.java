/**
   $Id: Leds.java 692 2011-09-02 08:38:10Z mviara $

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
package jmce.imsai;

import jmce.swing.KLeds;
import jmce.sim.*;

/**
 * 
 * Front leds panel.
 * <p>
 * This class provide implements a SwingHardware to display in one
 * JPanel the status of the led.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Leds extends AbstractPeripheral implements jmce.sim.SwingHardware,MemoryWriteListener,MemoryReadListener
{
	private KLeds leds = new KLeds("Data port",8);
	private int value;
	
	public Leds()
	{
		setName("Leds");
		leds.setPolarity(false);
	}

	public javax.swing.JComponent getComponent()
	{
		return leds;
	}
	
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addIOWriteListener(0xff,this);
		cpu.addIOReadListener(0xff,this);
	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		return 1;
	}

	public void writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		if (value != this.value)
		{
			this.value = value;
			leds.setLeds(value);
		}
	}


}

   

   