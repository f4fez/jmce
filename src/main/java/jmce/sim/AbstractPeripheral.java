/**
   $Id: AbstractPeripheral.java 596 2011-05-24 07:12:27Z mviara $

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
 * Abstract implementation of Peripheral
 * <p>
 * To support idle loop detection and save cpu time the peripheral
 * must call the method setIdle when a status operation is made
 * and setLive when a read / write operation is made. So after 10
 * seconds of only status operation any subseguent call to setIdle release the
 * cpu.
 *
 * @author Mario Viara
 * @version 1.01
 */
abstract public class AbstractPeripheral extends AbstractHardware implements Peripheral
{
	static private final jmce.util.Timeout timeout = new jmce.util.Timeout(10000);
	protected CPU cpu = null;
		
	public void registerCPU(CPU cpu) throws SIMException
	{
		this.cpu = cpu;

	}

	/**
	 * If a CPU is registred call the idle method oterwise use  the
	 * <code>Thread.yield</code> method to wait for few ms.
	 *
	 * @return the number of ms elapsed.
	 */
	public int idle() throws SIMException
	{
		/**
		 * If the CPU is connected call the idle method of the
		 * cpu oterwise use <code>Thread.yeald()</code>.
		 */
		if (cpu != null)
			return cpu.idle();
		else
		{
			long startTime = System.currentTimeMillis();
			Thread.yield();
			int n =  (int)(System.currentTimeMillis() - startTime);
			return n;
		}
	}

	/**
	 * Constructor with name
	 */
	public AbstractPeripheral(String name)
	{
		super(name);
	}

	/**
	 * Default constructor
	 */
	public AbstractPeripheral()
	{
		super("Peripheral");

	}

	/**
	 * Set idle.
	 * <p>
	 * Called from sub class when the cpu make a status I/O
	 * operation. After 10 seconds of only status operation the
	 * method call automatically idle.
	 * 
	 * @since 1.01
	 * 
	 * @see #idle
	 * @see #setLive
	 */
	protected final void setIdle() throws SIMException
	{
		/**
		 * Id the timeout is expired and the cpu is not running
		 * in realtime call the idle method.
		 */
		if (timeout.isExpired())
		{
			if (cpu == null || cpu.getRealTime() == false)
				idle();
		}
	}

	/**
	 * Set live.
	 * <p>
	 * Called from sub class when the cpu make a read / write
	 * opearation. 
	 *
	 * @since 1.01
	 *
	 * @see #setIdle
	 */
	protected final void setLive()
	{
		timeout.restart();
	}
}
