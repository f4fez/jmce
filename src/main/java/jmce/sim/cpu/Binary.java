/**
   $Id: Binary.java 694 2011-09-02 12:01:08Z mviara $

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

/**
 * Sample class to deal with binary number.
 * <p>
 * This class make easy set / reset single bit in one integer number.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Binary
{
	private int n;

	/**
	 * Default constructor.
	 */
	public Binary()
	{
		this(0);
	}

	/**
	 * Constructor with specific number
	 */
	public Binary(int n)
	{
		setValue(n);
	}

	/**
	 * Set the current value.
	 */
	public void setValue(int n)
	{
		this.n = n;
	}

	/**
	 * Return the current value.
	 */
	public int getValue()
	{
		return n;
	}

	/**
	 * Set a value for specific bit.
	 *
	 * @param b - Bit number 0 .. 31
	 * @param v - Bit value (true = 1, false = 0)
	 */
	public void setBit(int b,boolean v)
	{
		int mask = 1 << b;

		if (v)
			n |= mask;
		else
			n &= ~mask;
	}


	/**
	 * Return the specified bit.
	 *
	 * @param b - Number of bit 0 .. n
	 *
	 * @return true if the specified bit is set.
	 */
	public boolean getBit(int b)
	{
		return (n & (1 << b)) != 0 ? true : false;
	}
}
