/**
   $Id: P2000T.java 812 2012-03-19 11:57:25Z mviara $

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
import java.io.*;

import jmce.sim.*;
import jmce.sim.memory.*;
import jmce.zilog.z80.CTC;
import jmce.philips.SAA5050;
import jmce.util.Logger;
import jmce.util.Hex;
import jmce.sim.audio.Speaker;
import jmce.util.Timer;
import jmce.util.TimerListener;

/**
 * P2000T cassette.
 * <p>
 * This version is only a draft and do not work because I do not have
 * enough information abount the hardware architecture of P2000T.
 * <p>
 * <h2>Output port</h2>
 * <ul>
 *  <li>0 Write data</li>
 *  <li>1 Write command</li>
 *  <li>2 Rewind</li>
 *  <li>3 Forward</li>
 * <ul>
 * 
 * <h2>Input port</h2>
 * <p>
 * <ul>
 *  <li>3 Cassette enabled</li>
 *  <li>4 Cassette in position</li>
 *  <li>5 Begin / end of tape</li>
 *  <li>6 Read clock<li>
 *  <li>7 Read data<li>
 * <ul>
 *
 * @author Mario Viara
 * @version 1.00
 * @since 1.01
 */
class Cassette extends AbstractPeripheral implements MemoryReadListener,MemoryWriteListener
{
	private static Logger log = Logger.getLogger(Cassette.class);
	private FileInputStream is = null;
	private boolean clock = false;
	private int bit = 0;
	private int data = 0;
	private boolean rewind = false;
	private boolean forward = false;
	private Timer timerRewind;
	private Timer timerForward;

	public Cassette()
	{
		timerRewind = new Timer(500,false,new TimerListener()
		{
			public void timerExpired()
			{
				bit = 8;
				rewind = true;
			}

		});

		timerForward = new Timer(500,false,new TimerListener()
		{
			public void timerExpired()
			{
				bit = 8;
				forward = true;
			}

		});

	}
	
	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		log.info("WR="+Hex.formatByte(value)+" PC="+Hex.formatWord(cpu.pc()));

		
		// Rewind
		if ((value & 0x04) != 0)
		{
			
			try
			{
				is = new FileInputStream("P2000/P2000.cas");
			}
			catch (Exception ex)
			{
				log.info(ex);
				is = null;
			}

			if (timerRewind.isRunning() == false)
			{
				log.info("Start Rewind");
				cpu.addTimerMs(timerRewind);

			}				

					 
		}
		else
			rewind = false;
				
		// Forward
		if ((value & 0x08) != 0)
		{
			if (timerForward.isRunning() == false)
			{
				log.info("Start Forward");
				cpu.addTimerMs(timerForward);
			}

		}
		else
			forward = false;

	}

	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		value = 0x08;

		
		if (is == null) {
		}

		log.info("R="+rewind+" F="+forward+" PC="+Hex.formatWord(cpu.pc()));
		
		if (rewind || forward)
			value |= 0x20;
		else
			value |= 0x10;
		
		clock = !clock;
		
		if (clock)
			value |= 0x40;

		
		if (is != null && !rewind && !forward ) 
		{
			if (clock)
			{
				if (++bit > 7)
				{
					bit = 0;
					try
					{
						data = is.read();
						log.info("Data="+Hex.formatByte(data));
					}
					catch (Exception ex)
					{
						log.info(ex);

					}
					if (data == -1)
					{
						try
						{
							is.close();
						}
						catch (Exception ex)
						{
							log.info(ex);

						}
						is = null;
					}
				}
				
			}

			if (bit > 7)
				bit = 7;
			
			if ((data & (1 << (bit))) != 0)
				value |= 0x40;

			
		}


		log.info("RD="+Hex.formatByte(value));

		return value;
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		
		for (int i = 0 ; i < 0x10 ; i++)
		{
			cpu.addIOWriteListener(0x10+i,this);
			cpu.addIOReadListener(0x20+i,this);
		}

		try
		{
			is = new FileInputStream("p2000/P2000.cas");
		}
		catch (Exception ex)
		{
			log.info(ex);
		}
	}
}


/**
 * Philips P2000T computer.
 * <p>
 * <h2>Implemented peripheral</h2> :
 * <ul>
 *  <li>Z80 cpu at 2.5 Mhzu</li>
 *  <li>1 x SAA5050 character controller</li>
 *  <ii>1 x Z80 CTC</li>
 *  <li>1 x 9 x 8 matrix keyboard</li>
 *  <li>1 x Speaker</li>
 *  <li>1 x Rom bios loaded at reset</li>
 *  <li>1 x BASIC loaded at reset</li>
 * </ul>
 *   
 * <p>
 * P2000T Memory map :
 * <p>
 * <pre>
 * 0000-0FFF  ROM BIOS
 * 1000-4FFF  Application program
 * 5000-57FF  Video RAM (T-version)
 * 5000-5FFF  Video RAM (M-version)
 * 6000-9FFF  System RAM
 * A000-FFFF  Extension board
 * </pre>
 * <p>
 * @author Mario Viara
 * @version 1.00
 * @since 1.01
 */
public class P2000T extends jmce.zilog.z80.Z80 
{
	private SAA5050 sa;
	private CTC ctc;
	private Speaker spk;

	/** Default constructor */
	public P2000T()
	{
		setName("P2000T");
		setClock(2500000);
		setRealTime(true);
	}
	
	protected void initMemories()
	{

		if (getMemoryForName(MAIN_MEMORY) == null)
		{
			Memory m = new PlainMemory(MAIN_MEMORY,0x10000);

			/** Rom */
			m.setReadOnly(0,4*1024);

			/** No memory */
			m.setReadOnly(22*1024,2*1024);

			addHardware(m);

			Loadable load;
			
			load = new Loadable("p2000/basic.bin",0x1000);
			addHardware(load);

			load = new Loadable("p2000/p2000rom.bin",0x0000);
			addHardware(load);
		}

		super.initMemories();
	}

	protected void initPeripherals() throws SIMException
	{
		ctc = (CTC)getHardware(CTC.class);

		if (ctc == null)
		{
			ctc = new CTC();
			ctc.setBase(0x88);
			addHardware(ctc);
		}
		

		/** Configure the screen */
		sa = (SAA5050)getHardware(SAA5050.class);
		
		if (sa == null)
		{
			sa= new SAA5050();
		
			addHardware(sa);
		}
		
		for (int i = 0x30 ; i <= 0x3f ; i++)
		{
			addIOWriteListener(i,new MemoryWriteListener()
			{
				public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
				{
					sa.setScroll(value);
				}
			});

			addIOReadListener(i,new MemoryReadListener()
			{
				public int readMemory(Memory memory,int address,int value) throws SIMException
				{
					return sa.getScroll();
				}
			});

		}

		sa.setMemory(getMemoryForName(CPU.MAIN_MEMORY));
		sa.setAddress(0x5000);

		Keyboard k = (Keyboard)getHardware(Keyboard.class);


		if (k == null)
		{
			k = new Keyboard();
			addHardware(k);
		}
		

		/** Configure CTC e GUI component */
		k.setCtc(ctc);
		k.setComponent(sa.getComponent());


		spk = (Speaker)getHardware(Speaker.class);
		
		if (spk == null)
		{
			spk = new Speaker();
			addHardware(spk);
		}

		
		for (int i = 0x50 ; i <= 0x5f ; i++)
		{
			addIOWriteListener(i,new MemoryWriteListener()
			{
				public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
				{
					if ((value & 0x01) != 0)
						spk.setFreq(440);
					else
						spk.setFreq(0);
				}
			});
		}
		
		super.initPeripherals();

	}
	
}
