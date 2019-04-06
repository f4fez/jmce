/**
   $Id: AT89C51RD2.java 694 2011-09-02 12:01:08Z mviara $

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
package jmce.atmel;

import jmce.sim.*;
import jmce.sim.memory.*;
import jmce.util.Hex;
import jmce.util.Logger;
import jmce.intel.mcs51.JPorts;
import jmce.intel.mcs51.Ports;

/**
 *
 * Atmel 89C51Rxx cpu.
 *
 * @author Mario Viara
 * @version 1.01
 *
 */
public class AT89C51RD2 extends jmce.intel.mcs51.MCS52 implements AT89C51RD2Constants,CallListener
{
	private static final Logger log = Logger.getLogger(AT89C51RD2.class);

	/**
	 * Cosntructor
	 */
	public AT89C51RD2()
	{
		setName("AT89C51RD2");
		setAuxrDptrEnabled();
	}

	@Override
	protected void initMemories()
	{
		Memory m;
		
		m = getMemoryForName(CODE_MEMORY);

		if (m == null)
		{
			m = new PersistentMemory(CODE_MEMORY,getName()+".flash",64*1024);
			addHardware(m);
		}


		m = getMemoryForName(XDATA_MEMORY);
		if (m == null)
			addHardware(new PlainMemory(XDATA_MEMORY,2048));

		super.initMemories();


	}

	@Override
	protected void initNames()
	{
		super.initNames();
		setSfrName(P4,"P4");
		setSfrName(P5,"P5");
		setSfrName(CKCON,"CKCON");
		
	}

	@Override
	protected void initPeripherals() throws SIMException
	{
		if (getHardware(Ports.class) == null)
		{
			jmce.intel.mcs51.Port.sfrPorts[4] = P4;
			jmce.intel.mcs51.Port.sfrPorts[5] = P5;
			Ports p = new Ports(6);
			addHardware(p);
			p.addHardware(new JPorts());
		}

		super.initPeripherals();

	}

	@Override
	protected void initListeners() throws SIMException
	{
		super.initListeners();
		
		addIOWriteListener(CKCON,new MemoryWriteListener()
		{
			public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
			{
				setClockPerCycle((value & 1) != 0 ? 6 : 12);

			}
		});
		
		setCallListener(0xfff0,this);
		
	}
	
	

	public int call(CPU cpu,int pc) throws SIMException
	{
		log.info("CALL AT "+Hex.formatWord(pc)+" R1="+r(1));
		
		switch (r(1))
		{
			case	9:
				int dest   = getDptr(0);
				int source = getDptr(1);

				for (int i = 0 ; i < acc() ; i++)
					code(dest+i,xdata(source+i));
				acc(0);
				break;
			default:
				throw new SIMException("API R1 "+r(1)+" A = "+acc()+" DPTR0 = "+Hex.formatWord(getDptr(0))+" DPTR1 = "+Hex.formatWord(getDptr(1)));
				
		}

		return 0;
		
	}
	
}
