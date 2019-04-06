/**
   $Id: StringToken.java 371 2010-09-28 01:41:15Z mviara $

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
package jmce;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Utility class to parse command
 *
 * @author Mario Viara
 * @version 1.00
 */
class StringToken extends StringTokenizer
{
	Vector<String> tokens = new Vector<String>();
	private String cmd;
	StringToken(String cmd)
	{
		super(cmd);
		this.cmd = cmd;
		while (hasMoreTokens())
			tokens.add(nextToken());
	}

	public int getTokenCount()
	{
		return tokens.size();
	}

	public int getTokenInt(int i)
	{
		return Integer.parseInt(getTokenAt(i));
	}

	public int getTokenHex(int i)
	{
		return Integer.parseInt(getTokenAt(i),16);
	}

	public String getStringAt(int i)
	{
		return tokens.elementAt(i);
	}

	public String getTokenAt(int i)
	{
		return tokens.elementAt(i);
	}

	public String toString()
	{
		return cmd;
	}

}

