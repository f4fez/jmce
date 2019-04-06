/**
   $Id: TapeData.java 630 2011-06-09 07:12:05Z mviara $

   Copyright (c) 2011, Mario Viara

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

package jmce.sim.tape;

import jmce.sim.*;
import jmce.util.Logger;
import jmce.util.FastArray;

/**
 * Cassette data.
 * This class rappresent a cassette tape loaded normally from file.
 *
 * @author Mario Viara
 * @since 1.02
 */
public class TapeData extends FastArray<TapePulse>
{
	private int clock;

	/**
	 * Constructor without clock
	 */
	public TapeData()
	{
		this(0);
	}

	/**
	 * Constructor with specified clock
	 */
	public TapeData(int clock)
	{
		super(100000,0);
		this.clock = clock;
		
	}

	/**
	 * Return the clock
	 */
	public int getClock()
	{
		return clock;
	}
}
