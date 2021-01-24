/**
   $Id: Device.java 695 2011-09-21 06:09:11Z mviara $

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


/**
 * The core class for device implementation.
 * <p>
 * 
 * This class implement a standard method to send / receive data between two
 * or more devices.
 * 
 * <p>The class Device use two <code>DeviceProducer</code>
 * to implements full duplex communications between the two or more class
 * involved to produce and consume data.
 *
 * @author Mario Viara
 * @version 1.01
 * 
 * @see DeviceProducer
 * @see DeviceConsumer
 *
 */
public class  Device<E> extends AbstractPeripheral
{
	private Device<E> connected = null;
	
	/**
	 * Buffered implementation for input
	 */
	private BufferedDeviceProducer<E> input  = new BufferedDeviceProducer<E>();

	/**
	 * Buffer implementation for output
	 */
	private BufferedDeviceProducer<E> output = new BufferedDeviceProducer<E>();


	/**
	 * Remove an input consumer
	 *
	 * @since 1.01
	 */
	public void removeInputConsumer(DeviceConsumer<E> c)
	{
		input.removeConsumer(c);
	}
	
	/**
	 * Add a new consumer for data received.
	 *
	 */
	public void addInputConsumer(DeviceConsumer<E> c)
	{
		input.addConsumer(c);

	}

	
	/**
	 * Add a new producer for input
	 *
	 */
	public void addInputProducer(DeviceProducer<E> c) throws SIMException
	{
		
		c.addConsumer(new DeviceConsumer<E>()
		{
			public void consume(E c) throws SIMException
			{
				input.produce(c);
			}
		});

	}

	/**
	 * Remove a consumer from the output queue.
	 */
	public void removeOutputConsumer(DeviceConsumer<E> c)
	{
		output.removeConsumer(c);
	}
	
	/**
	 * Add new output consumer
	 *
	 */
	public void addOutputConsumer(DeviceConsumer<E> c) throws SIMException
	{
		
		output.addConsumer(c);
	}

	/**
	 * Add new output producer
	 *
	 */
	public void addOutputProducer(DeviceProducer<E> c) throws SIMException
	{
		c.addConsumer(new DeviceConsumer<E>()
		{
			public void consume(E c) throws SIMException
			{
				output.produce(c); 
			}

		});
	}

	/**
	 * Check if data area available on input.
	 *
	 * @return  true - If data are avaiilable.
	 *
	 */
	public boolean readyRead()
	{
		return !input.isEmpty();
	}

	private E read(BufferedDeviceProducer<E> stream) throws SIMException
	{
		while (stream.isEmpty())
		{
			idle();
		}

		return stream.consume();

	}
	

	/**
	 * Read data from input.
	 * <p>Read the next available data. If no data is available
	 * wait for new one.
	 *
	 *
	 * @return the next available data.
	 */
	public E read() throws SIMException
	{
		return read(input);
	}

	/**
	 * Check if the output buffer is full.
	 *
	 * @return  true if the output buffer is full.
	 */
	public boolean readyWrite()
	{
		return !output.isFull();
	}

	private void write(BufferedDeviceProducer<E> stream,E c) throws SIMException
	{
		while (stream.isFull())
			idle();
		stream.produce(c);
	}
	
	/**
	 * Write data in the output buffer
	 */
	public void write(E c) throws SIMException
	{
		write(output,c);
	}

	public boolean readyReadOutput()
	{
		return !output.isEmpty();
	}

	public E readOutput() throws SIMException
	{
		return read(output);
	}
	
	/**
	 * Write data in the input buffer
	 */
	public void writeInput(E c) throws SIMException
	{
		write(input,c);
		
	}

	/**
	 * Constructor with name
	 */
	public Device(String name)
	{
		super(name);
	}

	/**
	 * Default constructor
	 */
	public Device()
	{
		this("Device");
	}


	/**
	 * Return the connected device
	 *
	 * @return Connected device.
	 */
	public Device<E> getConnected()
	{
		return connected;
	}

	
	/**
	 * Connect another device to this. After this method is called
	 * all data <code>produced</code> from this Device sill be
	 * sent to the connected device and all data produced from the
	 * connected device will be sent to this device.
	 *
	 * @param c Device to connect.
	 */
	public void setConnected(Device<E> c)  throws SIMException
	{
		/**
		 * TODO IN the future may be can be nice to handle
		 * multiple connected device.
		 */
		if (this.connected != null)
			throw new java.lang.Error("Multiple connected device not supported");
			
		this.connected = c;
		
		addOutputConsumer(new DeviceConsumer<E>()
		{
			public void consume(E c) throws SIMException
			{
				connected.writeInput(c);
			}
		});

		
		connected.addOutputConsumer(new DeviceConsumer<E>()
		{
			public void consume(E c) throws SIMException
			{
				writeInput(c);
			}

		});
	}
}

