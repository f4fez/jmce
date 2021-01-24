/**
   $Id: I8237.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.intel;

import jmce.sim.*;
import jmce.util.Hex;
import jmce.util.Timer;
import jmce.util.TimerListener;
import jmce.util.Logger;

/**
 * Interface to rappresent a DMA ontroller.
 *
 * @author Mario Viara
 * @version 1.00
 */
interface DmaController
{
	/**
	 * Return the number of channel installed.
	 */
	public int getChannelCount();

	/**
	 * Return a specific DMA channel
	 */
	public DmaChannel getChannelAt(int i);
}

/**
 * Interface to rappresent the generic DMA request.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 */
interface DmaRequest
{
	/**
	 * Return true if the operation is terminated. Normally checked after
	 * any transfer but it is hardware depending. The name is the
	 * name used in Intel 8237 but it was the first DMA controller
	 * implemented.
	 */
	public boolean getDmaEOP();

	/**
	 * Return true if the data request is till active. Normally
	 * perform another cycle but it is hardware depending.
	 */
	public boolean getDmaDREQ();
}

/**
 * Interface to rappresent a DMA read operation.
 * <p>
 * DMA Read operation occours when trasfer is from memory to peripheral.
 *
 * @author Mario Viara
 * @version 1.00
 */
interface DmaRequestRead extends DmaRequest
{
	/**
	 * Set the value from memory to the peripheral.
	 */
	public void setDmaValue(int value);
}

/**
 * Interface to rappresent a DMA write operation.
 * <p>
 * DMA write operation occours when trasfer is from peripheral to
 * memory.
 *
 * @author Mario Viara
 * @version 1.00
 */
interface DmaRequestWrite extends DmaRequest
{
	/**
	 * Read a byte from the peripheral
	 */
	public int getDmaValue();
}

/**
 * Interface to rappresent a DMA master operation.
 * <p>
 * DMA master operation occours when another device take control of the
 * bus. Of course for one simulator means can access to the cpu memory.
 *
 * @author Mario Viara
 * @version 1.00
 */
interface DmaRequestMaster extends DmaRequest
{
	public void dmaMaster(CPU cpu) throws SIMException;
}

/**
 * Interface to rappresent a complete DMA channel.
 *
 * @author Mario Viara
 * @version 1.00
 */
interface DmaChannel
{
	/**
	 * Initiate DMA request for read.
	 * <p>
	 * Data will be transferred from memory to the peripheral.
	 */
	public void dmaRequest(DmaRequestRead read) throws SIMException;

	/**
	 * Initiate DMA request for write.
	 * <p>
	 * Data will be trasferred from peripheral to memory.
	 */
	public void dmaRequest(DmaRequestWrite write) throws SIMException;

	/**
	 * Initiate DMA request master.
	 * <p>
	 * Control will be passed to the caller.
	 */
	public void dmaRequest(DmaRequestMaster master) throws SIMException;
}

/**
 * Intel 8237 Dma Controller
 * <p>
 * This class implements a complete 4 channel Intel 8237 dma controller.
 *<p>
 * <h2>Limitations :</h2>
 * <p>
 * <ul>
 *  <li>Timing are only partially repected and only for single
 *  transfer</li>
 * </ul>
 * <p>
 *
 * <h2>Resources used :</h2>
 * <p>
 * <ul>
 *  <li>16 consecutive R/W location of memory starting from the selected
 *  <tt>port</tt>.</li>
 * </ul
 * 
 * @author Mario Viara
 * @version 1.00
 */
public class I8237 extends AbstractPeripheral implements DmaController
{
	private static Logger log = Logger.getLogger(I8237.class);

	/** Mask mode in mode register */
	static public final int MODE_DMA_MASK = 0xc0;

	/** Mode demand */
	static public final int MODE_DMA_DEMAND = 0x00;

	/** Mode Single transfer */
	static public final int MODE_DMA_SINGLE = 0x40;

	/** Mode block transfer */
	static public final int MODE_DMA_BLOCK  = 0x80;

	/** Cascade mode trasfer */
	static public final int MODE_DMA_CASCADE= 0xc0;

	/** Address decremenent if not set address are incrmented */
	static public final int MODE_DECREMENT = 0x20;

	/** Auto initialize on TC */
	static public final int MODE_AUTO = 0x10;

	/** Transfer mask */
	static public final int MODE_TRANSFER_MASK = 0x0C;

	/** Verify mode */
	static public final int MODE_TRANSFER_VERIFY = 0x00;

	/** Read mode */
	static public final int MODE_TRANSFER_READ  = 0x08;

	/** Write mode */
	static public final int MODE_TRANSFER_WRITE = 0x04;
	

	/**
	 * Class to rappresent a 16 bit register that can be readed /
	 * written at 8 bit at time.
	 *
	 * @author Mario Viara
	 * @version 1.00
	 */
	public class WordRegister
	{
		private boolean ptr;
		int value;
		int savedValue;

		WordRegister()
		{
			ptr = false;
			value = 0;
		}

		void write(int v)
		{
			if (ptr)
				value = (value & 0x00ff) | ((v & 0xff) << 8);
			else
				value = (value & 0xff00) | (v & 0xff);

			ptr = !ptr;

			savedValue = value;
		}

		int read()
		{
			int v;

			if (ptr)
				v = value >>> 8;
			else
				v = value & 0xff;

			ptr = !ptr;

			return v;
		}

		void clear()
		{
			ptr = false;
		}

		int getValue()
		{
			return value;
		}

		void setValue(int v)
		{
			this.value = v;
		}

		void restore()
		{
			value = savedValue;
		}

		void decrement()
		{
			value = (value - 1) & 0xffff;
		}

		void increment()
		{
			value = (value + 1) & 0xffff;
		}
	}

	/**
	 * Inner class for I8237 channel
	 *
	 * @author Mario Viara
	 * @version 1.00
	 */
	public class I8237Channel implements DmaChannel
	{
		int ch;
		WordRegister base = new WordRegister();
		WordRegister count = new WordRegister();
		private int mode;
		private boolean tc;
		private boolean request;
		private boolean enabled;
		private boolean hold;
		private boolean mask;
		private Timer timerRead;
		private Timer timerWrite;
		private DmaRequestRead requestRead = null;
		private DmaRequestWrite requestWrite = null;

		I8237Channel(int ch)
		{
			this.ch = ch;
			reset();

			timerRead = new Timer(10,false,new TimerListener()
			{
				public void timerExpired() throws SIMException
				{
					ackRead();
				}
			});

			timerWrite = new Timer(10,false,new TimerListener()
			{
				public void timerExpired() throws SIMException
				{
					ackWrite();
				}
			});

		}

		void setMode(int mode)
		{
			this.mode = mode;
		}

		void setEnabled(boolean b)
		{
			this.enabled = b;
		}

		void clear()
		{
			base.clear();
			count.clear();
		}

		/**
		 * Dump thru log the current configuration.
		 */
		void dump()
		{
			log.info("Ch # "+ch+" Mode="+Hex.formatByte(mode)+" Base="+Hex.formatWord(base.getValue())+" Count="+Hex.formatWord(count.getValue()));
			log.info("Ch # "+ch+" Hold="+hold+" Request="+request+" TC="+tc+" Enabled="+enabled+" Mask="+mask);
		}
		
		/**
		 * Set address hold flag
		 * <p>
		 * If this flag is set the <tt>base</tt> is not
		 * incremented / decremented is legal only for channel
		 * 0.
		 */
		void setHold(boolean b)
		{
			hold = b;
		}

		boolean getTc()
		{
			return tc;
		}

		void request(boolean b)
		{
			request = b;
		}

		boolean request()
		{
			return request;
		}

		void reset()
		{
			setMask(true);
			clear();
			writeBase(0);
			writeBase(0);
			writeCount(0);
			writeCount(0);
			tc = request = hold =false ;
			enabled = true;
			lastByte = 0;
			log.info("Ch #"+ch+" reset");
			dump();
		}

		void setMask(boolean mask)
		{
			this.mask = mask;
		}

		int readBase()
		{
			return base.read();
		}

		int readCount()
		{
			return count.read();
		}

		void writeBase(int v)
		{
			base.write(v);
		}

		void writeCount(int v)
		{
			count.write(v);
		}

		public boolean checkReady()
		{
			if (!enabled)
			{
				log.info("Ch#"+ch+" not enabled");
				return false;
			}

			if (mask)
			{
				log.info("Ch#"+ch+" maskered");
				return false;
			}

			return true;
		}
		
		public void dmaRequest(DmaRequestMaster m) throws SIMException
		{
			if (!checkReady())
				return;

			if ((mode & MODE_DMA_MASK) != MODE_DMA_CASCADE)
				throw new SIMException("Mode="+Hex.formatByte(mode)+" is not cascade");
			
			request = true;
			m.dmaMaster(cpu);
			request = false;
		}
		
		
		public void dmaRequest(DmaRequestRead read) throws SIMException
		{
			if (!checkReady())
				return;

			
			if ((mode & MODE_DMA_MASK) == MODE_DMA_CASCADE)
				throw new SIMException("Mode="+Hex.formatByte(mode)+" can not be cascade");

			if ((mode & MODE_TRANSFER_MASK) != MODE_TRANSFER_READ &&
			    (mode & MODE_TRANSFER_MASK) != MODE_TRANSFER_VERIFY)
				throw new SIMException("Only READ and VERIFY transfer allowed");
			
			requestRead = read;
			request = true;
			cpu.addTimerCycle(timerRead);

		}


		public void dmaRequest(DmaRequestWrite write) throws SIMException
		{
			if (!checkReady())
				return;


			if ((mode & MODE_DMA_MASK) == MODE_DMA_CASCADE)
				throw new SIMException("Mode="+Hex.formatByte(mode)+" can not be cascade");


			if ((mode & MODE_TRANSFER_MASK) != MODE_TRANSFER_WRITE &&
			    (mode & MODE_TRANSFER_MASK) != MODE_TRANSFER_VERIFY)
				throw new SIMException("Only WRITE and VERIFY transfer allowed");
			
			requestWrite = write;
			request = true;
			cpu.addTimerCycle(timerWrite);

		}

		/**
		 * Called when a timer for dma read expire
		 */
		private void ackRead() throws SIMException
		{
			for (;;)
			{
				// Read a byte from memory
				if ((mode & MODE_TRANSFER_MASK) == MODE_TRANSFER_READ)
				{
					int v = transferFromMemory(this,base.getValue());
					requestRead.setDmaValue(v);
				}
				
				// Increment / decrement address if requested
				if (hold == false)
				{
					if ((mode & MODE_DECREMENT) != 0)
						base.decrement();
					else
						base.increment();

				}

				if (isTerminate(requestRead))
				{
					terminateDma();
					return;
				}

				// In single transfer wait another dreq
				if ((mode & MODE_DMA_MASK) == MODE_DMA_SINGLE)
				{
					if (requestRead.getDmaDREQ() == false)
					{
						request = false;
						return;
					}
				}

			}
		}



		/**
		 * Called when a timer for dma read expire
		 */
		private void ackWrite() throws SIMException
		{
			for (;;)
			{
				// Read a byte from device
				if ((mode & MODE_TRANSFER_MASK) == MODE_TRANSFER_WRITE)
				{
					int v =requestWrite.getDmaValue();
					transferToMemory(this,base.getValue(),v);
				}

				// Increment / decrement address if requested
				if (hold == false)
				{
					if ((mode & MODE_DECREMENT) != 0)
						base.decrement();
					else
						base.increment();

				}

				if (isTerminate(requestWrite))
				{
					terminateDma();
					return;
				}

				// In single transfer wait another dreq
				if ((mode & MODE_DMA_MASK) == MODE_DMA_SINGLE)
				{
					if (requestRead.getDmaDREQ() == false)
					{
						request = false;
						return;
					}
				}

			}
		}


		boolean isTerminate(DmaRequest req)
		{
			count.decrement();

			if (count.getValue() == 0)
				return true;

			if (req.getDmaEOP())
				return true;

			switch (mode & MODE_DMA_MASK)
			{
				case	MODE_DMA_DEMAND:
					if (req.getDmaDREQ() == false)
						return true;
					break;
			}

			return false;
		}

		void terminateDma()
		{
			tc = true;
			request = false;

			// Check for auto initialize
			if ((mode & MODE_AUTO) != 0)
			{
				tc = false;
				count.restore();
				base.restore();
			}
		}

	};

	I8237Channel channels[];
	private int lastByte;
	private int port;

	I8237()
	{
		this(0);
	}
	
	I8237(int port)
	{
		this("i8237",port);
		
	}
	I8237(String name,int port)
	{
		setName(name);
		setPort(port);
		channels = new I8237Channel[4];

		for (int i = 0 ; i < 4 ; i++)
		{
			channels[i] = new I8237Channel(i);
		}
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
	
	public int readMemory(jmce.sim.Memory m,int a,int v) throws SIMException
	{
		int i;

		switch (a - port)
		{
				// Read base channel # 0
			case	0x00:
				v = channels[0].readBase();
				break;
				// Read count channel # 0
			case	0x01:
				v = channels[0].readCount();
				break;

				// Read base channel # 1
			case	0x02:
				v = channels[1].readBase();
				break;
				// Read count channel # 1
			case	0x03:
				v = channels[1].readCount();
				break;

				// Read base channel # 2
			case	0x04:
				v = channels[2].readBase();
				break;
				// Read count channel # 2
			case	0x05:
				v = channels[2].readCount();
				break;

				// Read base channel # 3
			case	0x06:
				v = channels[3].readBase();
				break;
				// Read count channel # 3
			case	0x07:
				v = channels[3].readCount();
				break;

				// Read status register
			case	0x08:
				v = 0;
				for (i = 0 ; i < 4 ; i++)
				{
					if (channels[i].getTc())
						v |= 0x01 << i;
					if (channels[i].request())
						v |= 0x10 << i;
				}
				break;

				// Read temporary register
			case	0x0d:
				v = lastByte;
				break;
		}

		return v;
	}

	public void writeMemory(jmce.sim.Memory m,int a,int v,int oldValue) throws SIMException
	{
		int i;
		I8237Channel c;

		switch (a - port)
		{
				// Write base channel # 0
			case	0x00:
				channels[0].writeBase(v);
				break;
				// Write count channel # 0
			case	0x01:
				channels[0].writeCount(v);
				break;

				// Write base channel # 1
			case	0x02:
				channels[1].writeBase(v);
				break;
				// Write count channel # 1
			case	0x03:
				channels[1].writeCount(v);
				break;

				// Write base channel # 2
			case	0x04:
				channels[2].writeBase(v);
				break;
				// Write count channel # 2
			case	0x05:
				channels[2].writeCount(v);
				break;

				// Write base channel # 3
			case	0x06:
				channels[3].writeBase(v);
				break;
				// Write count channel # 3
			case	0x07:
				channels[3].writeCount(v);
				break;


				// Write command register
			case	0x08:
			for (i = 0 ; i < 4 ; i++)
					channels[i].setEnabled((v & 0x04) == 0);

				channels[0].setHold((v & 0x02) != 0);
				break;

				// Write all mask register
			case	0x0f:
				for (i = 0 ; i < 4 ; i++)
					if ((v & (1 << i)) != 0)
						channels[i].setMask(true);
					else
						channels[i].setMask(false);
				break;

				// Write request register
			case	0x09:
				c = channels[v & 0x03];
				if ((v & 0x04) != 0)
					c.request(true);
				else
					c.request(false);
				break;

				// Mask single channel
			case	0x0a:
				c = channels[v & 0x03];
				if ((v & 0x04) != 0)
					c.setMask(true);
				else
					c.setMask(false);
				break;

				// Write mode register
			case	0x0b:
				c = channels[v & 0x03];
				c.setMode(v);
				break;

				// Clear byte pointer
			case	0x0c:
				for (i = 0 ; i < 4 ; i++)
					if ((v & (1 << i)) != 0)
						channels[i].clear();
				break;

				// Master clear
			case	0x0d:
				for (i = 0 ; i < 4 ; i++)
					channels[i].reset();
			lastByte = 0;
				break;

		}

	}

	/**
	 * Transfer a byte to memory
	 */
	void transferToMemory(I8237Channel c,int address ,int value) throws SIMException
	{
		lastByte = value;
		cpu.setByte(address,value);
	}

	/**
	 * Transfer a byte from memory
	 */
	int transferFromMemory(I8237Channel c,int address) throws SIMException
	{
		return (lastByte = cpu.getByte(address));
	}

	public int getChannelCount()
	{
		return 4;
	}

	public DmaChannel getChannelAt(int i)
	{
		return channels[i];
	}
		
	public String toString()
	{
		return getName()+" AT "+Hex.formatWord(port);
	}
}
