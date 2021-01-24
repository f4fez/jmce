/**
   $Id: PairRegister.java 371 2010-09-28 01:41:15Z mviara $

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

import jmce.sim.*;

/**
 * Implementation of register base over a pair of register for example
 * in the Z80 implementation the HL register can be implemented with a pair
 * of register H and L
 *
 * @author Mario Viara
 * @version 1.00
 */
public  class PairRegister extends AbstractHardware implements Register
{
	Register l,h;
	
	public PairRegister(String name,Register l,Register h)
	{
		super(name);
		this.l = l;
		this.h = h;
	}
	
	public void setRegister(int value) throws SIMException
	{
		l.setRegister(value);
		h.setRegister(value >> l.getWidth());
	}
	
	public void setResetValue(int value)
	{
		l.setResetValue(value);
		h.setResetValue(value >> l.getWidth());
	}
	
	public int getRegister() throws SIMException
	{
		return l.getRegister() | (h.getRegister() << l.getWidth());
	}
	
	public int getWidth()
	{
		return l.getWidth() + h.getWidth();
	}
	
	public int getFamily()
	{
		return l.getFamily();
	}
	
	public String hexValue(int value)
	{
		return Hex.formatWord(value);

	}
	
	public String descValue() throws SIMException
	{
		return hexValue();
	}
	
	public String hexValue() throws SIMException
	{
		return hexValue(getRegister());
	}
	
	public void   addRegisterWriteListener(RegisterWriteListener l)
	{
		// fixme
	}

	public void   addRegisterReadListener(RegisterReadListener l)
	{
		// fixme
	}

	public void reset() throws SIMException
	{
		super.reset();
		l.reset();
		h.reset();
	}

}

