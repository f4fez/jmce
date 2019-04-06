/**
   $Id: Speaker.java 624 2011-06-01 17:18:43Z mviara $

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
import jmce.util.Logger;
import javax.sound.sampled.*;

/**
 * ZX spectrum Speaker emulation. Also manage the color of the border.
 * <p>
 * Very simple hardware means very time expensive emulation.
 * <p>
 * @author Mario Viara
 * @version 1.01
 */
public class Speaker extends AbstractPeripheral implements MemoryWriteListener,CycleListener,SpectrumConstants
{
	private static Logger log = Logger.getLogger(Speaker.class);
	private byte buffer[] = new byte[1024];
	private int bufferLen = 0;
	private SourceDataLine line;
	private byte beeper = 0;
	private byte oldBeeper;
	private int noChangeCount = 0;
	private int cycle = 0;
	private boolean installed = false;
	private int sample = 44100;
	private int cycles;
	private long clock;
	private int border = 0;
	private Screen screen = null;
	
	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);
		try
		{
				
			AudioFormat fmt = new AudioFormat(sample ,8,1,false,false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(fmt);
			line.start();
			installed = true;
		}
		catch (Exception ex)
		{
			installed = false;
			log.warning(ex);
		}


	}
	
	public void	writeMemory(Memory m,int port,int value,int oldValue) throws SIMException
	{
		beeper = (byte)((value & ULA_OUT) != 0 ? 0xff : 0x00);
		
		if ((value & ULA_BORDER) != border)
		{
			border = value & ULA_BORDER;
			if (screen != null)
				screen.setBorder(border);
		}
					
	}

	public void cycle(int n)
	{
		/** If the speaker do not change for 1 second ignore it **/
		if (beeper == oldBeeper)
		{
			if (noChangeCount > clock)
				return;
			noChangeCount += n;
		}
		else
		{
			oldBeeper = beeper;
			noChangeCount = 0;
		}
		
		cycle += n;
		
		while (cycle > cycles)
		{
			buffer[bufferLen] = beeper;
			if (++bufferLen >= buffer.length)
			{
				if (line.available() >= bufferLen)
					line.write(buffer,0,bufferLen);
				bufferLen = 0;
			}

			cycle -= cycles;
		}
		
	}

	/**
	 * Set the screen where border must be changed
	 *
	 * @since 1.02
	 */
	void  setScreen(Screen scr)
	{
		this.screen = scr;
	}
	/**
	 * Calculate cycle using the cpu clock
	 */
	private void setClock(long clock)
	{
		this.clock = clock;
		int ncycles = (int)(clock / sample)/2;
		if (ncycles != cycles)
		{
			cycles = ncycles;
			buffer = new byte[sample/100];
			log.info("Cycles="+cycles+" Sample="+sample+" Clock="+clock+" Buffer="+buffer.length);
			
		}
	}
	
	public void registerCPU(CPU _cpu)  throws SIMException
	{
		super.registerCPU(_cpu);

		setClock(cpu.getClock());

		cpu.addIOWriteListener(ULA_PORT,this);
		
		/** Enable listeners if the audio is installed */
		if (installed)
		{
			cpu.addCycleListener(this);
		}

	}


	public String toString()
	{

		return "Speaker driver installed="+installed;
	}
}
