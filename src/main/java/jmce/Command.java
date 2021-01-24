/**
   $Id: Command.java 510 2011-01-18 09:25:07Z mviara $

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

import jmce.sim.*;

/**
 * Abstract command.
 * <p>
 * Abstract class to implements a monitor command.
 *
 * @author Mario Viara
 * @version 1.01
 */
abstract public class Command
{
	StringToken st;
	Monitor monitor;
	private String help = "";
	private String cmd;

	public CPU getCPU()
	{
		return monitor.getCPU();
	}
	
	public Command(Monitor m,String s,String help)
	{
		this.monitor = m;
		this.help = help;
		int n = s.indexOf('\t');
		
		if (n != -1)
		{
			st = new StringToken(s.substring(0,n));
			cmd = s.substring(0,n)+" "+s.substring(n+1);
		}
		else
		{
			st = new StringToken(s);
			cmd = s;
		}
		
	}		

	abstract public void exec(StringToken cmdLine) throws Exception;

	public boolean pressToContinue(int row) throws Exception
	{
		if (((row + 1) % 23) == 0)
		{
			print("ESC-Quit");
			if (getch() == 27)
			{
				println();
				return true;
			}
			print("\r        \r");

		}

		return false;
	}
	
	public boolean compare(StringToken cmdLine)
	{
		for (int i = 0 ; i < st.getTokenCount() ; i++)
		{
			if (i >= cmdLine.getTokenCount())
				return false;
			String s1 = st.getTokenAt(i);
			String s2 = cmdLine.getTokenAt(i);

			if (s1.length() > s2.length())
				s1 = s1.substring(0,s2.length());
			if (!s1.equalsIgnoreCase(s2))
				return false;
		}

		return true;

	}

	public int getch() throws Exception
	{
		return monitor.getch();
	}
	
	/**
	 * Method duplicate from monitor
	 */
	public void print(Object o)
	{
		monitor.print(o);
	}

	public void println()
	{
		monitor.println();
	}

	public void println(Object o)
	{
		monitor.println(o);
	}

	public void getLine(String prompt,StringBuffer sb) throws Exception
	{
		monitor.getLine(prompt,sb);
	}

	int getHexNumber(StringToken st,int i) throws Exception
	{
		return monitor.getHexNumber(st,i);
	}

	public String getCmd()
	{
		return cmd;
	}

	public String getHelp()
	{
		return help;
	}
	
	public String toString()
	{
		return st.toString();
	}

	
}
