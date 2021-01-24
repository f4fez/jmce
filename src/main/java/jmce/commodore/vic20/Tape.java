/**
   $Id: Tape.java 628 2011-06-08 09:57:43Z mviara $

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

import jmce.sim.*;
import jmce.util.Logger;
import jmce.util.Hex;


/**
 *
 * Tape interface for commodore VIC-20
 * <p>
 * This version support only play.
 * <p>
 * @author Mario Viara
 * @version 1.01
 *
 * @since 1.01
 */
public class Tape extends jmce.sim.tape.TapeFile implements jmce.sim.TapeEventListener,CycleListener,MemoryWriteListener
{
	private static Logger log = Logger.getLogger(Tape.class);
	private jmce.mos.VIA6522 via2,via1;
	private int pulse;
	public Tape()
	{
		super("VIC20-Tape");
		
		addDecoder(new jmce.sim.tape.TapeFileC64());

		/** Set default config */
		setConfig("commodore/vic20/invaders.tap");
		
		addTapeEventListener(this);
	}

	public void setVia2(jmce.mos.VIA6522 via)
	{
		this.via2 = via;
	}

	public void setVia1(jmce.mos.VIA6522 via)
	{
		this.via1 = via;
	}

	public void tapeStop(jmce.sim.Tape tape)
	{
		log.info("Stop");
		setSense(false);
	}
	
	public void tapePlay(jmce.sim.Tape tape)
	{
		setSense(true);
		log.info("Play");
		
	}

	public void tapePower(jmce.sim.Tape tape)
	{
	}
	
	public void tapeRec(jmce.sim.Tape tape)
	{
		log.info("Rec");
		setSense(true);
	}

	public void reset() throws SIMException
	{
		super.reset();
		pulse= 0;
		stop();
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addCycleListener(this);
		via2.addPortBWriteListener(this);
		
		via1.addCA2MemoryWriteListener(new MemoryWriteListener()
		{
			public void writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
			{
				if ((value == 0) != getPower())
				{
					setPower(value == 0);
				}
			}

		});
	}

	public void cycle(int n) throws SIMException
	{
		if (!isPlay() || !getPower())
			return;

		if (pulse < 0)
			return;
		
		while (n > 0)
		{
			if (pulse >= n)
			{
				pulse -= n;
			 	n = 0;
			}
			else
			{
				n -= pulse;
				pulse = 0;
			}

			if (pulse == 0)
			{
				via2.writeCA1(false);
				via2.writeCA1(true);
				
				TapePulse tp =nextPulse();
				
				if (tp == null)
				{
					pulse = -1;
					break;
				}
				pulse = tp.getWidth();
			}
		}
	}

	private void setSense(boolean mode)
	{
		try
		{
			int a = via1.readPortA();
			if (!mode)
				a |= 0x40;
			else
				a &= ~0x40;
			via1.writePortA(a);
		}
		catch (SIMException ignore)
		{
		}
	}

	long cycle;
	
	public void writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		if (!isRecording())
			return;
		
		if ((value & 0x08) != (oldValue & 0x08))
		{
			boolean edge = (value & 0x08) != 0;
			if (edge == false)
			{
				long now = cpu.getCycle();
				int delta = (int)(now - cycle)/8;
				log.info("Rec "+edge+" delta "+delta+" "+Hex.formatWord(delta));
				cycle = now;
			}
		}
	}

}
