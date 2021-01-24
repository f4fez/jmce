/**
   $Id: AbstractRegister.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.sim.cpu;

import jmce.util.Hex;
import jmce.util.FastArray;

import jmce.sim.*;

/**
 * Abstract implementation of interface Register
 *
 * @author Mario Viara
 * @version 1.00
 */
public abstract class AbstractRegister extends AbstractHardware implements Register
{
	private int family;
	private int width;
	private int reset;
	protected FastArray<RegisterWriteListener> wl = new FastArray<RegisterWriteListener>();
	protected FastArray<RegisterReadListener> rl = new FastArray<RegisterReadListener>();
	protected int mask;
	
	AbstractRegister(String name,int family,int width,int reset)
	{
		super(name);
		this.family = family;
		this.width = width;
		this.mask = (1 << width) - 1;
		this.reset = reset;
	}

	public void setResetValue(int reset)
	{
		this.reset = reset;
	}
	

	public int getWidth()
	{
		return width;
	}

	public int getFamily()
	{
		return family;
	}

	public void reset() throws SIMException
	{
		super.reset();
		setRegister(reset);
	}

	public String hexValue()  throws SIMException
	{
		return hexValue(getRegister());
	}

	public String descValue() throws SIMException
	{
		return hexValue();
	}
	
	public String hexValue(int value)
	{
		if (width < 16)
			return Hex.formatByte(value);
		else
			return Hex.formatWord(value);

	}

	public void addRegisterWriteListener(RegisterWriteListener l)
	{
		wl.add(l);
	}

	public void addRegisterReadListener(RegisterReadListener l)
	{
		rl.add(l);
	}

}
