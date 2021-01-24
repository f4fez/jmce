/**
   $Id: BufferedDeviceProducer.java 596 2011-05-24 07:12:27Z mviara $

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

import jmce.util.RingBuffer;

/**
 * Buffer implementation of device producer.  If one or more
 * DeviceConsumer are installed the data are transmitted are sent
 * directly as a </code>SampleDeviceProducer</code> but if no DeviceConsumer
 * is installed data are stored in one RingBuffer and later can be
 * received.
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class BufferedDeviceProducer<E> extends SampleDeviceProducer<E> 
{
	private static final long serialVersionUID = 1L;
	private RingBuffer<E> buffer;

	/**
	 * Default contructor.
	 *
	 * Create s new instance wih the ring buffer of the default
	 * size.
	 */
	public BufferedDeviceProducer()
	{
		buffer = new RingBuffer<E>();
	}

	public BufferedDeviceProducer(int size)
	{
		buffer = new RingBuffer<E>(size);
	}

	public void addConsumer(DeviceConsumer<E> c) 
	{
		super.add(c);
		try
		{
			consumeBuffer();
		}
		catch (Exception ignore)
		{
		}
	}

	private void consumeBuffer() throws SIMException
	{
		if (size() > 0)
			while (!buffer.isEmpty())
				consume(buffer.get());
	}
	
	public void produce(E c) throws SIMException
	{
		//System.out.println("Produce "+c);
		buffer.put(c);
		consumeBuffer();
	}

	public boolean isFull()
	{
		return buffer.isFull();
	}

	public boolean isEmpty()
	{
		return buffer.isEmpty();
	}


	public E consume()
	{
		if (buffer.isEmpty())
			throw new java.lang.Error("Consume empty buffer");
		E c =  buffer.get();
		//System.out.println("Consume "+c);

		return c;
	}
}
