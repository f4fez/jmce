/**
   $Id: Hazeltine1500.java 510 2011-01-18 09:25:07Z mviara $

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
 * Terminal Hazeltime 1500
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Hazeltine1500 extends SampleTerminal
{
	private int state = 0;
	private int setRow,setCol;

	/**
	 * Default constructor
	 */
	public Hazeltine1500()
	{
		super("Hazeltine1500");
		setNumRow(25);
		setNumCol(80);
		setNumStatus(1);
	}

	/**
	 * Processing leadin character
	 */
	private void leadin(int c)
	{
		switch (c)
		{
			case	18:
				home();
				break;
			case	12:
				if (getRow() > 0)
					setCursor(getRow()-1,getCol());
				break;
			case	11:
				if (getRow() < (getNumRow() - 1)) // fixme
					setCursor(getRow()+1,getCol());
				break;
			case	17:
				state++;
				return;
					//break;
			case	28:
				cls();
				home();
				break;
			case	29:
					// FIXME clear only reverse ?
				cls();	
				break;

			case	15:
				clearEol();
				break;

			case	23:
				// FIXME Clear background ?
				clearEos();
				break;

			case	24:
				// FIXME Clear foregound ?
				clearEos(); 
				break;

			case	25:			
				setAtt(NORMAL);
				break;

			case	31:			
				setAtt(HI);
				break;

			case	19:
				deleteLine();
				setCursor(getRow(),0);
				break;
			case	26:
				insertLine();
				setCursor(getRow(),0);
				break;

		}

		state = 0;
	}

	public void putchar(int c) throws SIMException
	{
		switch (state)
		{
			case	0:
				switch (c)
				{
					case	126:
						state = 1;
						break;
					case	16:
						right();
						break;
					default:
						super.putchar(c);
						return;
				}
				break;
			case	1:
				leadin(c);
				break;
			case	2:
				setCol = c;
				setCol = setCol % 96;
				state++;
				break;
			case	3:
				setRow = c;
				setRow &= 0x1f;
				state = 0;
				if (setCol < 0)
					setCol = 0;
				if (setCol >= getNumCol())
					setCol = getNumCol() - 1;
				if (setRow < 0)
					setRow = 0;
				if (setRow >= getNumRow())
					setRow = getNumRow() - 1;
				setCursor(setRow,setCol);
				break;


		}

	}

}
