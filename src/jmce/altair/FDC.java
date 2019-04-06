/**
   $Id: FDC.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.altair;

import jmce.util.Logger;
import jmce.util.Hex;
import jmce.sim.*;
import jmce.sim.disk.ImageDisk;

/**
 * Altair 8080  Floppy disk controller. <p>
 *
 * Simulate Altair 88_DISK controller based over FD-400 hard-sectored
 * floppy disk drive. Can support up to 16 floppy disk with 77 tracks
 * and 32 sector of 137 byte. The Z80 version support disk with 254
 * track.
 * <p>
 * 
 * FDC use 3 consucutive I/O port AT 0x08,0x09,0x0A :
 *<p>
 *<ul>
 * <li>08 W Select and enable controller and drive. (SELECT)</li>
 * <li>08 R Drive and controller status. (SELECT)</li>
 * <li>09 W Control disk function (CTRL)</li>
 * <li>09 R Current sector position (CTRL)</li>
 * <li>0A W Write data (DATA)</li>
 * <li>0A R Read data (DATA)</li>
 *</ul>
 *
 * Control disk function register (CTRL) :
 * <p>
 * <ul>
 *  <li> BIT0 I - When 1 steps the head IN one track.</li>
 *  <li> BIT1 O - When 1 steps the head OUT one track.</li>
 *  <li> BIT2 H - When 1 load head to disk surface.</li>
 *  <li> BIT3 U - When 1 unload the head from disk surface.</li>
 *  <li> BIT4 E - Enabled interrupt.</li>
 *  <li> BIT5 D - Disabled interrupt.</li>
 *  <li> BIT6 C - When 1 lowers head current.</li>
 *  <li> BIT7 W - When  starts Write enable sequence.</li>
 * </ul>
 * 
 *<p>
 * <b>THIS VERSION DO NOT SUPPORT SECTOR WRITING.</b>
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class FDC extends jmce.sim.disk.AbstractDiskController implements MemoryReadListener,
MemoryWriteListener,
AltairConstants

{
	
	private int status = 0xff;
	private boolean sectorReady = false;
	private int counter = -1;
	
	private static Logger log = Logger.getLogger(FDC.class);
	
	public FDC()
	{
		setName("Altair 8800 FDC");
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addIOReadListener(SELECT,this);
		cpu.addIOReadListener(CTRL,this);
		cpu.addIOReadListener(DATA,this);

		cpu.addIOWriteListener(SELECT,this);
		cpu.addIOWriteListener(CTRL,this);
		cpu.addIOWriteListener(DATA,this);

	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		setLive();
		switch (address)
		{
			case	SELECT:
				value = status;
				if (disk.getTrack() == 0)
					value &= ~0x40;
				else
					value |= 0x40;
				break;

			case	CTRL:
				if (sectorReady)
				{
					int s = disk.getSector();
					s = s+1;
					if (s > disk.getNumSector())
						s = 1;
					log.fine("Set sector to "+s);
					disk.setSector(s);
					counter = -1;
				}
				
				value = (disk.getSector() - 1) << 1;
				sectorReady = !sectorReady;
				if (sectorReady)
				{
					value |= 0x01;
				}
				break;

			case	DATA:
				cpu.setStatusLine('R');
				if (counter == -1)
				{
					log.fine("Read T="+disk.getTrack()+" S="+disk.getSector());
					disk.read();
					
					counter = 0;
				}

				log.finest("Read byte at "+counter);
				value = disk.getBuffer()[counter] & 0xff;
				if (++counter >= disk.getSectorSize())
					counter = 0;
				break;
				
		}
		
		log.finer("RD AT "+Hex.formatByte(address)+" = "+Hex.formatByte(value)+" ST="+Hex.formatByte(status));
		return value;
	}

	
	public void writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		setLive();
		log.finer("WR "+Hex.formatByte(value)+" AT "+Hex.formatByte(address));
		switch (address)
		{
			case	SELECT:
				if ((value & 0x80) == 0)
				{
					value &= 0x0f;
					if (value < getDiskCount())
					{
						setDrive(drive);
						log.fine("Select drive "+disk.getName()+"="+value);
						status = 0xF5;
						sectorReady = false;
						counter = -1;
					}
					else
					{
						log.info("Invalid drive "+value);
						disk = nullDisk;
						status = 0xff;
					}
				}
				else
				{
					log.info("Unselect drive");
					status = 0xff;
					disk = nullDisk;
				}
				break;
				
			case	CTRL:
				if (disk == nullDisk)
					break;
				
				// Load ?
				if ((value & 0x04) != 0)	
				{
					log.fine("Load head");
					
					/// Head loaded
					status &= ~0x04;

					// Data avail
					status &= ~0x80;
					
				}

				// Track ++

				if ((value & CTRL_STEP_IN) != 0)
				{
					int t = disk.getTrack() + 1;
					if (t >= disk.getNumTrack())
						t = disk.getNumTrack() - 1;
					log.fine("Set track="+t);
					disk.setTrack(t);
				}

				// Track --
				if ((value & CTRL_STEP_OUT) != 0)
				{
					int t = disk.getTrack() - 1;
					if (t < 0)
						t = 0;
					disk.setTrack(t);
					log.fine("Set track="+t);
				}
		}
	}

	
	void addAltairFD(String disk)
	{
		ImageDisk d = new ImageDisk(disk);
		d.setNumTrack(77);
		d.setNumSector(32);
		d.setSectorSize(137);
		addHardware(d);
	}

}
