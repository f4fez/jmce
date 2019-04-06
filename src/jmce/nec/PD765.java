/**
   $Id: PD765.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.nec;

import jmce.sim.*;
import jmce.sim.disk.*;
import jmce.util.RingBuffer;

/**
 * NEC 765 A / B Floppy disk controller<p>
 * <p>
 * 
 */
public class PD765 extends AbstractDiskController implements PD765Constants
{
	
	
	private RingBuffer<Integer> result = new RingBuffer<Integer>(9*1024);
	private RingBuffer<Integer> data = new RingBuffer<Integer>(9*1024);
	private RingBuffer<Integer> cmds = new RingBuffer<Integer>(16);
	private int base = 0;
	private int msr;
	private int cmd;
	private boolean reset = false;
	
	
	/**
	 * Set the base port for this instance.
	 */
	public void setBase(int base)
	{
		this.base = base;
	}

	/**
	 * Return the base port of this instance
	 */
	public int getBase()
	{
		return base;
	}

	public void setReset(boolean mode)
	{
		reset = mode;
		
		if (mode)
		{
			msr = MSR_READY;
			result.purge();
			data.purge();
			cmds.purge();
		}
	}

	public void	writeMemory(jmce.sim.Memory m,int address,int v,int oldValue) throws SIMException
	{
		if (reset)
			return;
		
		if ((msr & MSR_BUSY) == 0)
		{
			if (cmds.isEmpty())
				cmd = v;
			cmds.put(v);

			switch (cmd)
			{
				case	CMD_RECALIBRATE:
					if (cmds.count() == 2)
						cmdRecalibrate();
					break;
			}
		}
			
	}

	boolean checkDrive(int drive)
	{
		if (drive >= getDiskCount())
			return false;

		try
		{
			setDrive(drive);
			if (disk.mount() == false)
				return false;
		}
		catch (SIMException e)
		{
			return false;
		}


		return true;
	}
	
	public void cmdRecalibrate()
	{
		cmds.get();

		drive = cmds.get() & 0x03;
		msr |= MSR_BUSY;
		msr |= ~MSR_READY;
		
		if (checkDrive(drive) == false) {
		} else {
		}
		
	}
	
	public int readMemory(jmce.sim.Memory m,int a,int v) throws SIMException
	{
		if (reset)
			return v;
		
		if (a == (base + MSR))
			v = msr;
		else if (a == (base + DATA))
		{
			if ((msr & MSR_EXM) != 0)
				v = sendByte();
			else
				v = sendResult();
		}

		return v;
	}

	private void endCommand()
	{
		msr &= ~(MSR_BUSY|MSR_EXM);
	}
	
	public int sendByte()
	{
		int v = data.get();
		
		if (data.isEmpty())
		{
			msr &= ~MSR_DIO;
			endCommand();
		}

		return v;
	}

	public int sendResult()
	{
		int v = 0;
		
		if (!result.isEmpty())
		{
			v = result.get();
			if (result.isEmpty())
				msr &= ~MSR_DIO;
		}

		return v;
	}
}
