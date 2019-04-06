/**
   $Id: Z19.java 588 2011-05-18 06:58:09Z mviara $

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

import jmce.sim.*;

/**
 * Zenith Z19 terminal emulator.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Z19 extends SampleTerminal
{
	int state;
	int cursorRow;

	public Z19()
	{
		super("Z19");
		setNumRow(25);
		setNumCol(80);
		state = 0;
	}

	/**
	 * Process a ESC x sequence
	 */
	@SuppressWarnings("fallthrough")
	private void escape(int c) throws SIMException
	{
		switch (c)
		{
				// Normal mode
			case	'q':
				setAtt(NORMAL);
				break;

				// Reverse mode
			case	'p':
				setAtt(REVERSE);
				break;

				// Overwrite mode
			case	'O':
				setInsertMode(false);
				break;

				// Insert mode
			case	'@':
				setInsertMode(true);
				break;

				// Delete char
			case	'N':
				deleteChar();
				break;

			case	'M':
				deleteLine();
				setCursor(getRow(),0);
				break;

			case	'L':
				insertLine();
				setCursor(getRow(),0);
				break;

				// Clear to end of line
			case	'K':
				clearEol();
				break;

			case	'J':
				clearEos();
				break;

				// Erase the current line
			case	'l':
				clearLine();
				break;

				// Clear from beginning to cursor
			case	'o':
				clearToCursor();
				break;

			case	'E':
				cls();
						
			case	'H':
				home();
				break;
			case	'C':
				right();
				break;
			case	'D':
				left();
				break;
			case	'B':
				down();
				break;
			case	'A':
				up();
				break;
			case	'I':
				if (getRow() == 0)
				{
					scrollUp(0,getWindowSize(),getNumCol());

				}
				else
					up();
				break;
			case	'n':
				writeInput(27);
				writeInput((int)'Y');
				writeInput(getRow()+32);
				writeInput(getCol()+32);
				break;

				// Save cursor position
			case	'j':
				saveCursor();
				break;
				
				// Restore cursor position
			case	'k':
				restoreCursor();
				break;

				//  Set cursor position
			case	'Y':
				state = 2;
				break;
		}
		if (state == 1)
			state = 0;
	}

	public void putchar(int c) throws SIMException
	{
		switch (state)
		{
			case	0:
				switch (c)
				{
					case	27:
						state =1;
						break;
					default:
						super.putchar(c);
						break;
				}
				break;
			case	1:
				escape(c);
				break;
			case	2:
				cursorRow = c - 32;
				state++;
				break;
			case	3:
				setCursor(cursorRow,c - 32);
				state = 0;
				break;
		}
	}


}
