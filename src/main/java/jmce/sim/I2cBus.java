/**
   $Id: I2cBus.java 588 2011-05-18 06:58:09Z mviara $

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
package jmce.sim;

import jmce.util.Logger;
import jmce.util.Hex;
import jmce.sim.memory.*;

/**
 * I2C Bus manager.
 *
 * <p>
 * This peripheral manage all the operation between
 * one master (the CPU) and one or more slave connected using the I2C
 * protocol. To implement the interface the <tt>CPU</tt> must have two
 * <tt>OpenCollectorMemoryBit<//tt> mapped to phisical register.
 * 
 * <p>The following bus condition are supported:
 * <ul>
 *   <li> Start
 *   <li> Repeated start
 *   <li> Send byte
 *   <li> Recv byte
 *   <li> Stop
 * </ul>
 * <p>
 * This version is not multi master can support only one master
 * connected to one or more slave devices.
 *
 * @author Mario Viara
 * @version 1.00
 *
 * @see I2cSlave
 */
public class I2cBus extends jmce.sim.AbstractPeripheral implements MemoryWriteListener
{
	private static Logger log = Logger.getLogger(I2cBus.class);

	/** Memory bit connected to SCL wire */
	protected MemoryBit	SCL;
	
	/** Memory bit connected to SDA Wire */
	protected MemoryBit	SDA;
	
	private	boolean oldScl,oldSda;
	private	int bitCount;
	private	int value;
	private	I2cSlave ph;
	private	boolean ack;
	private	boolean read;
	private	boolean ignore;
	private	int byteCount;
	private	boolean lastByte;

	/**
	 * Default constructor
	 */
	public I2cBus()
	{
		super("I2CBUS");
	}

	/**
	 * Set the memory bit used for SCL
	 *
	 * @param scl - Memory bit.
	 */
	public void setScl(OpenCollectorMemoryBit scl)
	{
		this.SCL = scl;
		SCL.addMemoryWriteListener(this);
	}

	/**
	 * Set the memory bit used for SDA
	 *
	 * @param sda - Memory bit
	 */
	public void setSda(OpenCollectorMemoryBit sda)
	{
		this.SDA = sda;
		SDA.addMemoryWriteListener(this);
	}


	public void reset() throws SIMException
	{
		super.reset();
		SDA.set(true);
		SCL.set(true);
		oldSda = SDA.get();
		oldScl = SCL.get();

		i2cIdle();
	}

	/**
	 * Set the interface to idle. Forget any bus condition.
	 */
	private void i2cIdle()
	{
		bitCount = 0;
		ph = null;
		ack = false;
		read = false;
		ignore = false;
		lastByte = false;
		byteCount = 0;
	}

	/**
	 * Called when a start or repeted start condition is detected
	 */
	protected void i2cStart()
	{
		bitCount = 0;
		if (ph != null)
		{
			I2cSlave tmp = ph;
			log.fine("Repeated start condition");
			i2cIdle();
			ph = tmp;
		}

		else
		{
			log.fine("Start condition");
			i2cIdle();
		}
	}

	/**
	 * Called when the stop condition is detected
	 */
	protected void i2cStop()
	{
		log.fine("Stop condition");
		i2cIdle();
	}

	/**
	 * Search a slave with the specified address. Return true if
	 * one device is found or false if no device is found.
	 */
	private boolean searchSlave(int a)
	{
		// Use only the address
		a &= 0xFE;

		for (int i = 0 ; ; i++)
		{
			ph = (I2cSlave)getHardware(I2cSlave.class,i);
			if (ph == null)
				break;
			if (ph.i2cAddress(a))
				return true;
		}

		return false;
	}


	/**
	 * Process one received byte
	 */
	protected void i2cRecv(int value) throws SIMException
	{
		log.fine("I2CRECV "+Hex.formatByte(value));

		if (ph == null)
		{
			if (searchSlave(value))
			{
				log.fine("Found "+ph);
				ack = ph.i2cWrite(byteCount++,value);
			}
			else
				log.info("Not found Peripheral at "+Hex.formatByte(value));
		}
		else
			ack = ph.i2cWrite(byteCount++,value);


	}

	/**
	 * Called when is necessary to send one byte.
	 */
	@SuppressWarnings("fallthrough")
	protected void i2cSend(boolean sda) throws SIMException
	{
		if (lastByte)
			return;

		switch (bitCount)
		{
			case	0:
				value = ph.i2cRead(byteCount++) & 0xff;
			default:
				if ((value & 0x80) != 0)
				{
					SDA.set(true);
				}
				else
				{
					SDA.set(false);
				}
				value <<= 1;
				bitCount++;
				break;
			case	8:
				lastByte = sda;
				log.finer("Last byte "+lastByte);
				bitCount++;
				break;
		}

		log.finer("Send Clock "+bitCount+" Bit "+SDA.get()+" value "+Hex.formatByte(value));

	}

	/**
	 * Called when a new bit is received
	 */
	@SuppressWarnings("fallthrough")
	protected void i2cRecv(boolean sda) throws SIMException
	{
		switch (bitCount)
		{
			case	0:
				value = 0;
			default:
				value <<= 1;
				if (sda)
					value |= 1;
				if (++bitCount == 8)
					i2cRecv(value);

				break;
			case	8:
				bitCount++;
				break;
		}
	}

	/**
	 * Called on the rising edge of the clock
	 */
	protected void i2cRise(boolean bit) throws SIMException
	{
		log.finer("I2CRise "+bit);

		if (read)
			i2cSend(bit);
		else
			i2cRecv(bit);
	}

	/**
	 * Called on the failing edge od the clock.
	 */
	protected void i2cFail(boolean bit) throws SIMException
	{
		log.finer("I2CFail "+bit);
		if (bitCount == 8)
		{
			if (read)
			{
				log.finer("Release SDA");
				SDA.set(true);
			}
			else
			{
				if (ack)
				{
					log.finer("ACK SDA");
					SDA.set(false);
				}
				else
				{
					SDA.set(true);
					log.finer("NACK SDA");
				}
			}

		}
		else if (bitCount == 9)
		{
			ack = false;
			if (!read)
				SDA.set(true);
			bitCount = 0;

			if (byteCount == 1)
			{
				read = (value & 1) != 0 ? true : false;
				if (read)
					log.finer("Read operation");
				else
					log.finer("Write operation");

			}

		}
	}

	/**
	 * Bus manager.
	 * 
	 * This function is the main implementation of the I2C bus and
	 * process the bit change.
	 *
	 * @param scl - Current value of clock.
	 * @param sda - Current value of data.
	 */
	protected void bus(boolean scl,boolean sda) throws SIMException
	{
		if (ignore)
			return;


		if (oldScl == scl && sda == oldSda)
			return;

		ignore = true;

		log.finest("SCL="+scl+",SDA="+sda+" SCL="+oldScl+",SDA="+oldSda);



		if (scl && oldScl && oldSda && !sda)
			i2cStart();

		if (scl && oldScl && !oldSda && sda)
			i2cStop();

		if (!oldScl && scl)
			i2cRise(sda);

		if (oldScl && !scl)
			i2cFail(sda);

		oldScl = SCL.get();
		oldSda = SDA.get();
		ignore = false;
	}

	/**
	 * Implementation of listener to receive write on the bit
	 * mapped to the wire of the I2cBUS.
	 */
	public void writeMemory(Memory m,int address,int value,int oldValue) throws SIMException
	{
		bus(SCL.get(),SDA.get());
	}
}

