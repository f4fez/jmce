/**
   $Id: VT100.java 376 2010-09-29 06:10:30Z mviara $

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
package jmce.sim.terminal;

import jmce.util.Logger;
import jmce.sim.*;

/**
 * Terminal VT100
 * <p>
 * Only a subset of the original VT100 is supported<p>
 * <p>
 * Supported sequence :
 * <pre>
 *  ESC[0m		Normal attribute.
 *  ESC[1m		Intense attribute.
 *  ESC[7m		Reverse attribute.
 *  ESC[2J		Clear screen.
 *  ESC[0J		Clear end of screen.
 *  ESC[row;colH	Set cursor position.
 *  ESC[row;colf	Set cursor position
 *  ESC[M		Delete line.
 *  ESC[L		Insert line.
 *  ESC[0K		Clear to end of line.
 *
 *  Special sequence to support ladder under CP/M :
 *  
 *  ESC*<R><C>		Set the cursor to R,C base 1
 * </pre>
 * 
 * @author Mario Viara
 * @version 1.00
 */
public class VT100 extends SampleTerminal
{
	private static Logger log = Logger.getLogger(VT100.class);
	int argc = 0;
	int argv[] = new int [16];
	
	private enum State
	{
		NORMAL,ESCAPE,ARGS,LOCATE_ROW,LOCATE_COL
	};

	State state = State.NORMAL;
	
	public VT100()
	{
		super("VT100");
		setNumRow(25);
		setNumCol(80);
	}

	private void putcharCSI(int c)
	{
		state = State.NORMAL;
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("CSI "+(char)c);
		for (int i = 0 ; i <= argc ; i++)
			sb.append(" ARGC#"+i+"="+argv[i]);

		log.fine(sb.toString());
		
		switch (c)
		{
			default:
				log.info("Unsupported CSI "+(char)c);
				break;

			case	'm':
				
				for (int i = 0 ; i <= argc ; i++)
				{

					switch (argv[i])
					{
						case	7:
							setAtt(getAtt()|REVERSE);
							break;
						case	1:
							setAtt(getAtt()|HI);
							break;
						case	0:
							setAtt(NORMAL);
							break;
					}
				}
				break;
			case	'M':
				deleteLine();
				break;
			case	'L':
				insertLine();
				break;
			case	'K':
				switch (argv[0])
				{
					case	0:
						clearEol();
						break;
				}
				break;
			case	'J':
				switch (argv[0])
				{
					case	0:
						clearEos();
						break;
					case	1:
						break;
					case	2:
						cls();
						setCursor(0,0);
						break;
				}
				break;

			case	'H':
			case	'f':
				setCursor(argv[0] - 1,argv[1] - 1);
				break;
		}
	}
	
	private void putcharArgs(int c)
	{
		if (c >= '0' && c <= '9')
		{
			argv[argc] = argv[argc] * 10 + (c - '0');
		}
		else if (c == ';')
		{
			argc++;
			argv[argc] = 0;
		}
		else
			putcharCSI(c);
			
	}
	
	private void putcharEscape(int c) throws SIMException
	{
		if (c == '[')
		{
			state = State.ARGS;
			argc = 0;
			argv[0] = 0;
		}
		else if (c == '*')
		{
			state = State.LOCATE_ROW;
		}
		else
		{
			super.putchar(27);
			super.putchar(c);
			state = State.NORMAL;
		}
			
	}
	private int savedLocateRow;
	
	public void putchar(int c) throws SIMException
	{
		switch (state)
		{
			case	LOCATE_COL:
				setCursor(savedLocateRow  -1, c -1 );
				state = State.NORMAL;
				break;
						
			case	LOCATE_ROW:
				savedLocateRow = c;
				state = State.LOCATE_COL;
				break;
				
			case	NORMAL:
				if (c == 27)
					state = State.ESCAPE;
				else
					super.putchar(c);
				break;
				
			case	ESCAPE:
				putcharEscape(c);
				break;

			case	ARGS:
				putcharArgs(c);
				break;
						
		}
					       
				
	}


}

