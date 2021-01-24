/**
   $Id: AT24C16.java 371 2010-09-28 01:41:15Z mviara $

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

import jmce.util.Logger;
import jmce.util.Hex;

/**
 * Atmel AT24C16 2048 byte EEprom. I2c Memory at address A0-AF
 *
 * @see jmce.sim.I2cBus
 * 
 * @author Mario Viara
 * @version 1.00
 * 
 */
public class AT24C16 extends PersistentMemory implements I2cSlave
{
	private static Logger log = Logger.getLogger(AT24C16.class);
	private int address;

	public AT24C16()
	{
		super("AT24C16","AT24C16.eeprom",2048);
	}

	public boolean i2cAddress(int address)
	{
		return (address & 0xF0) == 0xA0;
	}

	public boolean i2cWrite(int count,int value) throws SIMException
	{
		switch (count)
		{
			default:
				log.fine("Write "+Hex.formatByte(value)+" at "+Hex.formatWord(address));
				setMemory(address++,value);
				break;
			case	0:
				address = ((value >> 1) & 0x07) * 256 | (address & 0xff);
				break;
			case	1:
				address = (address & 0xff00 ) | value;
				break;
		}

		return true;
	}


	public int i2cRead(int count) throws SIMException
	{
		int value = getMemory(address);
		log.fine("Read "+Hex.formatByte(value)+" at "+Hex.formatWord(address));
		address++;
		return value;
	}

}

