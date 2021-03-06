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
package jmce.yaze;

import jmce.util.Logger;
import jmce.util.Hex;

import jmce.sim.*;
import jmce.sim.cpm.*;

/**
 * Yaze  Disk controller.
 * <p>
 * The interface to the FDC is very simple the register file must be
 * filled with the desired sector number and 
 * the dma with the desired memory address 
 * then a command can be written in the register <tt>FDC_CMD> the
 * status of the operation now is available in the <tt>FDC_STATUS</tt>.
 * The status will be 0 if the operation is performed correctly or
 * contains the error number.
 * <p>
 * With the current BIOS the following disk are supported :
 * <ul>
 *  <li>0 - IBM 3740</li>
 *  <li>1 - IBM 3740</li>
 *  <li>2 - Yaze HD 2 Mb</li>
 *  <li>3 - Yaze HD 2 Mb</li>
 * </ul>
 *  
 * 
 * @author Mario Viara
 * @version 1.01
 *
 * @see YazeConstants
 */
public class FDC extends jmce.sim.disk.AbstractDiskController implements YazeConstants,
									MemoryReadListener,
									MemoryWriteListener

{
	private static Logger log = Logger.getLogger(FDC.class);
	private boolean commandInProcess = false;
	protected int     commandResult = FDC_STATUS_SUCCESS;
	private int dma = 0;
	
	public FDC()
	{
		setName("Yaze FDC");

		
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addIOReadListener(FDC_STATUS,this);

		cpu.addIOWriteListener(FDC_DRIVE,this);
		cpu.addIOWriteListener(FDC_CMD,this);
		cpu.addIOWriteListener(FDC_TRACK_LOW,this);
		cpu.addIOWriteListener(FDC_TRACK_HI,this);
		cpu.addIOWriteListener(FDC_SECTOR,this);
		cpu.addIOWriteListener(FDC_DMA_LOW,this);
		cpu.addIOWriteListener(FDC_DMA_HI,this);

	}

	protected void setDma(int dma)
	{
		this.dma = dma;
	}

	private void diskio(int cmd) throws SIMException
	{
		commandInProcess = true;
		commandResult = FDC_STATUS_ERROR;

		if (drive < 0 || drive >= getHardwareCount())
		{
			log.info("Disk not found");
			return;
		}


		byte buffer[] = disk.getBuffer();
		int i;

		switch (cmd)
		{
			case	FDC_CMD_READ:
				cpu.setStatusLine('R');
				if (disk.read() != buffer.length)
					return;

				for (i = 0 ; i < buffer.length ; i ++)
					cpu.setByte(dma+i,buffer[i] & 0xff);
				break;

			case	FDC_CMD_WRITE:
				cpu.setStatusLine('W');
				for (i = 0 ; i < buffer.length ; i ++)
					buffer[i] = (byte)cpu.getByte(dma+i);

				i = disk.write();
				if (i != buffer.length)
					return;
				break;
			default:
				return ;
		}

		commandResult = FDC_STATUS_SUCCESS;

	}

	public int readMemory(jmce.sim.Memory m,int address,int value) throws SIMException
	{
		setLive();
		
		if (commandInProcess)
		{
			commandInProcess = false;
			return commandResult;
		}

		if (drive < 0 || drive >= getDiskCount())
			return FDC_STATUS_ERROR;

		return FDC_STATUS_SUCCESS;

	}

	public void	writeMemory(jmce.sim.Memory m,int address,int value,int oldValue) throws SIMException
	{
		int t;

		setLive();
		
		switch (address)
		{
			case	FDC_DRIVE:
				setDrive(value);
				break;
				
				
			case	FDC_SECTOR:
				disk.setSector(value);
				break;
				
			case	FDC_TRACK_LOW:
				t = disk.getTrack() & 0xff00;
				disk.setTrack(t | value);
				break;
			case	FDC_TRACK_HI:
				t = disk.getTrack() & 0xff;
				disk.setTrack(t | (value << 8));
				break;
				
			case	FDC_DMA_HI:
				dma &= 0x00ff;
				dma |= value << 8;
				setDma(dma);
				break;
			case	FDC_DMA_LOW:
				dma &= 0xff00;
				dma |= value;
				setDma(dma);
				break;
			case	FDC_CMD:
				diskio(value);
				break;

		}
	}

	void addImageDiskYazeHD(String name)
	{
		addImageDisk(name,new DPBYAHD());
	}

	void addDirDiskYazeHD(String name)
	{
		addDirDiskCPM(name,new DPBYAHD());
	}

	
	public String toString()
	{
		return "Yaze FDC AT 0x"+Hex.formatByte(FDC_DRIVE);
	}
	
}
