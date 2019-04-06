/**
   $Id: SampleTerminal.java 596 2011-05-24 07:12:27Z mviara $

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

import java.awt.Font;
import java.util.HashMap;
import java.io.*;

import java.awt.event.KeyEvent;

import jmce.util.Logger;

import jmce.sim.*;
import jmce.sim.memory.*;

/**
 * Sample terminal implements only CR,LF,TAB,BACK-SPACE
 * <p>
 * Also implements all low level function used by more sophisticated
 * terminal emulator like insert line, delete line etc.
 * <p>
 * <h2>Properties</h2>
 * <ul>
 *  <li><b>abortKey</b> Set to 30 (CTRL^) as default is the key that when
 *  pressed terminate the emulator. If set to -1 disable the abort key
 *  detection.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class SampleTerminal extends Terminal 
{
	
	private static Logger log = Logger.getLogger(SampleTerminal.class);
	protected int abortKey = 30;
	
	private int numRow,numCol,numStatus;
	private java.awt.Font font = new java.awt.Font("Monospaced",Font.PLAIN,18);
	private int row,col;
	private Memory videoAtt = new PlainMemory("AttMemory");
	private Memory videoChar= new PlainMemory("CharMemory");
	private boolean insertMode = false;
	private int savedRow,savedCol;
	private byte att;
	private HashMap<Integer,String> keys = new HashMap<Integer,String>();
	private  PrintStream ps = null;
	private String filename = null;
	
	@SuppressWarnings("rawtypes")
	static private Class defaultCRT = SwingCRT.class;

	static public void setDefaultCRT(String name) throws SIMException
	{
		try
		{
			setDefaultCRT(Class.forName(name));
		}
		catch (java.lang.ClassNotFoundException e)
		{
			throw new SIMException("Class not found "+name);
		}
		
	}

	@SuppressWarnings("rawtypes")
	static public void setDefaultCRT(Class clazz)
	{
		defaultCRT = clazz;
	}
	
	public SampleTerminal()
	{
		this("Sample terminal");
	}

	public int getFontSize()
	{
		return font.getSize();
	}

	public void setFontSize(int size)
	{
		font = font.deriveFont((float)size);
	}
	
	public SampleTerminal(String name)
	{
		super(name);
		setNumRow(25);
		setNumCol(80);
		setNumStatus(1);

		/**
		 * Default key mapping Wordstar like
		 */
		defineFunctionKey(0x10000 | KeyEvent.VK_UP,(char)('E' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_DOWN,(char)('X' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_LEFT,(char)('S' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_RIGHT,(char)('D' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_INSERT,(char)('V' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_ENTER,(char)('M' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_TAB,(char)('I' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_PAGE_UP,(char)('R' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_PAGE_DOWN,(char)('C' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_INSERT,(char)('V' - '@'));
		defineFunctionKey(0x10000 | KeyEvent.VK_DELETE,(char)(127));
		
	}

	public java.awt.Font getFont()
	{
		return font;
	}

	public void setFont(java.awt.Font font)
	{
		this.font = font;
	}
	

	
	public void write(Integer k) throws SIMException
	{
		//log.info("Pressed "+k);
		
		/**
		 * Check for abort
		 */
		if (k == abortKey && abortKey != -1)
		{
			if (cpu != null)
				cpu.abort("ABORT  pressed.");
			return;
		}
		
		String s = getFunctionKey(k);

		if (s != null)
		{
			for (int i = 0 ; i < s.length() ; i++)
				super.write((int)s.charAt(i));
		}
		else
		{
			if (k >= 0 && k <= 0xff)
				super.write(k);
		}
		
	}

	
	

	public int getNumStatus()
	{
		return numStatus;
	}
	
	public void setNumStatus(int n)
	{
		numStatus = n;
	}

	private void setMemorySize()
	{
		videoAtt.setSize(getScreenSize());
		videoChar.setSize(getScreenSize());
		
	}
	
	public void setNumRow(int row)
	{
		numRow = row;
		setMemorySize();
	}
	
	public void setNumCol(int col)
	{
		numCol = col;
		setMemorySize();
	}
	
	public int getNumRow()
	{
		return numRow;
	}

	
	public int getNumCol()
	{
		return numCol;
	}


	
	public int getWindowSize()
	{
		return numCol * (numRow - numStatus);
	}
	
	public int getScreenSize()
	{
		return numCol * numRow;
	}

	public int getCol()
	{
		return col;
	}

	public int getRow()
	{
		return row;
	}

	public boolean getCursor()
	{
		return true;
	}
	
	public void setCursor(int row,int col)
	{
		
		if (row < 0)
			row = 0;
		else if (row >= numRow)
			row = numRow - 1;

		if (col < 0)
			col = 0;
		else if (col >= numCol)
			col = numCol - 1;
		
		try
		{
			setAtt(this.row,this.col,getAtt(this.row,this.col));
			this.row = row;
			this.col = col;
			setAtt(this.row,this.col,getAtt(this.row,this.col));
		}
		catch (Exception ex)
		{
		}

	}

	void setAtt(int r,int c,int a) throws SIMException
	{
		setAtt(getPos(r,c),a);
		
	}

	void setAtt(int pos,int a) throws SIMException
	{
		videoAtt.setMemory(pos,a);

	}

	public int getAtt()
	{
		return att;
	}
	
	public void setAtt(int n)
	{
		att = (byte)n;
	}

	
	public void setChar(int r,int c,int ch) throws SIMException
	{
		setChar(getPos(r,c),ch);
	}
	
	public void setChar(int pos,int c) throws SIMException
	{
		videoChar.setMemory(pos,c);
	}


	public void setStatusLine(int r,int c, Object o)
	{
		try
		{
			String s = String.valueOf(o);

			if (r < numStatus)
			{
				r += numRow - numStatus;

				for (int i = 0 ; i < s.length() ; i++)
				{
					setAtt(r,c,REVERSE);
					setChar(r,c,s.charAt(i));
				}
			}
		}
		catch (Exception discard)
		{
		}

	}

	public void printStatusLine(int r,Object o) 
	{
		try
		{
			String s = String.valueOf(o);

			if (r < numStatus)
			{
				r += numRow - numStatus;

				for (int c = 0 ; c < numCol ;c++)
				{
					setAtt(r,c,REVERSE);
					setChar(r,c,c < s.length()  ? s.charAt(c) : ' ');
				}
			}
		}
		catch (Exception discard)
		{
		}
	}
	
	public void reset()  throws SIMException
	{
		super.reset();
		setAtt(NORMAL);
		home();
		cls();
		
		for (int i = 0 ; i < numStatus ; i++)
			printStatusLine(i,"");

	}
	
	public void init(Hardware parent) throws SIMException
	{
		CRT crt;
		
		crt = (CRT)getHardware(CRT.class);

		if (crt == null)
		{
			try
			{
				crt = ((CRT)(defaultCRT.newInstance()));
			}
			catch (Exception e)
			{
				throw new SIMException("Error creating CRT class");
			}
			addHardware(crt);
			
		}
		
		super.init(parent);
		
		videoAtt.setSize(getScreenSize());
		videoChar.setSize(getScreenSize());


		log.info(this+" - Initialized");
		addInputConsumer(new DeviceConsumer<Integer>()
		{
			public void consume(Integer c) throws SIMException
			{
				//System.out.println("putchar "+c);
				putchar(c);
			}
			
		});
	}
	



	public int getAtt(int r,int c) throws SIMException
	{
		return getAtt(getPos(r,c));
	}

	public int getAtt(int pos) throws SIMException
	{
		return videoAtt.getMemory(pos);
		
	}
	
	public int getChar(int r,int c) throws SIMException
	{
		return getChar(getPos(r,c));
	}

	public int getChar(int pos) throws SIMException
	{
		return videoChar.getMemory(pos);
	}
	
	int getPos(int row,int col)
	{
		return row * numCol + col;
	}
	
	int getPos(int col)
	{
		return row * numCol + col;
	}
	int getPos()
	{
		return row * numCol + col;
	}

	
	public void home()
	{
		setCursor(0,0);
		
	}

	public void cls()
	{
		try
		{
		for (int i = 0 ; i < getWindowSize() ; i++)
		{
			setChar(i,SPACE);
			setAtt(i,NORMAL);
		}
		}
		catch (Exception ex)
		{
		}

	}

	void setInsertMode(boolean insertMode)
	{
		this.insertMode = insertMode;
	}

	public void saveCursor()
	{
		savedCol = getCol();
		savedRow = getRow();

	}

	public void restoreCursor()
	{
		setCursor(savedRow,savedCol);
	}

	public void clearEol()
	{
		try
		{
			int pos = getPos();

			for (int i = col ; i < numCol ;i++)
			{
				setChar(pos++,SPACE);
			}
		}
		catch (Exception ex)
		{
		}
		

	}

	public void scrollUp(int from,int size,int n)
	{
		int i;

		try
		{

		for (i = 0 ; i < size - n ; i++)
		{
			setChar(from+i,getChar(from+i+n));
			setAtt(from+i,getAtt(from+i+n));
		}

		for (i = 0 ; i < n ; i++)
		{
			videoChar.setMemory(from+size-1-i, SPACE);
			videoAtt.setMemory(from+size-1-i, att);
		}

		}
		catch (Exception ex)
		{
		}
	}

	public void scrollDown(int from,int size,int n)
	{
		try
		{
		int i;

		
		for (i = 0 ; i < size - n ; i++)
		{
			videoChar.setMemory(from+size-i-1,getChar(from+size-i-1-n));
			videoAtt.setMemory(from+size-i-1,getAtt(from+size-i-1-n));
		}

		for (i = 0 ; i < n ; i++)
		{
			videoChar.setMemory(from+i,SPACE);
			videoAtt.setMemory(from+i, att);
		}

		}
		catch (Exception ex)
		{
		}

	}

	public void insertChar()
	{
		int pos = getPos();
		scrollDown(pos,(getNumRow() - getNumStatus())*getNumCol() - pos,1);
	}

	public void insertChar(int c)
	{
		try
		{
		int pos		= getPos();
		insertChar();
		setChar(pos,c);
		}
		catch (Exception ex)
		{
		}
	}

	public void insertLine()
	{
		scrollDown(row*numCol,(numRow - numStatus - row) * numCol,numCol);
	}

	public void deleteChar()
	{
		int pos		= getPos();
		scrollUp(pos,(numRow - numStatus)*numCol - pos,1);

	}

	public void deleteLine()
	{

		scrollUp(row*numCol,(numRow - row - numStatus) * numCol , numCol);
	}

	/**
	 * clear the current line
	 */
	void clearLine()
	{
		try
		{
		int pos = getPos(0);
		for (int c = 0 ; c <= getNumCol () ; c++,pos++)
		{
			setChar(pos,' ');
		}
		}
		catch (Exception ex)
		{
		}

	}

	/**
	 * Clear from the begin of the line to the cursor
	 */
	public void clearFromLine()
	{
		try
		{
		int pos = getPos(0);
		for (int c = 0 ; c <= getCol () ; c++,pos++)
		{
			setChar(pos,' ');
		}
		}
		catch (Exception ex)
		{
		}

	}

	void clearLine(int r)
	{
		try
		{
		int pos = r * numCol;
		for (int c = 0 ; c < numCol ; c++)
		{
			setChar(pos++,' ');

		}
		}
		catch (Exception ex)
		{
		}
		
	}
	

	/**
	 * Clear the display from the begin to the cursor
	 */
	public void clearToCursor()
	{
		clearFromLine();
		for (int r = 0 ; r < row ; r++)
		{
			clearLine(r);
		}

	}

	public void clearEos()
	{
		clearEol();
		for (int r = row + 1 ; r < numRow ; r++)
		{
			clearLine(r);
		}
	}



	public void up()
	{
		if (row > 0)
			setCursor(row-1,col);

	}

	public void down()
	{
		if (row + 1 < numRow - numStatus)
			setCursor(row+1,col);

	}


	public void left()
	{
		if (row != 0 || col != 0)
			if (--col < 0)
			{
				--row;
				col = numCol - 1;
			}
		
		setCursor(row,col);
	}

	public void right()
	{
		if ((row == getNumRow() - 1 - numStatus) && (col == getNumCol() - 1))
			return;

		if (++col >= getNumCol())
		{
			col = 0;
			if (++row >= getNumRow() - numStatus)
				row = getNumRow()- 1 - numStatus;
		}

		setCursor(row,col);
	}

	public void putchar(int c) throws SIMException
	{
		try
		{
		switch (c)
		{
			default:
				//System.out.print("PRT "+row+" "+col+" char "+c);
				if (c < 32 || c > 127)
					c = 32;
				if (insertMode)
					insertChar(c);
				else
				{
					if (ps != null)
						ps.print((char)c);
					setChar(row*numCol+col,c);
					setAtt(row,col,getAtt());
				}
				
				if (col + 1 >= numCol)
				{
					if (row + 1 >= numRow - numStatus)
					{
						scrollUp(0,(numRow-numStatus)*numCol,numCol);
						setCursor(row,0);
					}
					else
						setCursor(row+1,0);
				}
				else
					setCursor(row,col+1);
				break;

			case	9:	// tab
			
				int nc = (col / 8) * 8 + 8;
				if (nc >= numCol)
					nc = numCol -1;
				setCursor(row,nc);

			
				break;
			case	127:
				if (col > 0)
					col--;
				
				setChar(row,col,' ');
				setAtt(row,col,getAtt());
				setCursor(row,col);
				break;	

			case	8:
				if (col  > 0)
					setCursor(row,col-1);
				break;

			case	13:
				setCursor(row,0);
				break;

			case	10:
				if (ps != null)
				{
					ps.println();
					ps.flush();
				}
						
				setCursor(row,0);
				if (row+1 >= numRow - numStatus)
					scrollUp(0,(numRow-numStatus)*numCol,numCol);
				else
					setCursor(row+1,col);
				break;
			case	12:
				cls();
				home();
				break;

			case	7:
				break;
		}
		}
		catch (Exception ex)
		{
		}


	}

	public void defineFunctionKey(int k,char c)
	{
		defineFunctionKey(k,""+c);
	}
	
	public void defineFunctionKey(int k,String s)
	{
		keys.put(k,s);
	}

	public String getFunctionKey(int k)
	{
		return keys.get(k);
	}

	public Memory getCharMemory()
	{
		return videoChar;
	}
	
	
	public Memory getAttMemory()
	{
		return videoAtt;
	}

	public String getEchoFile()
	{
		return filename;
	}
	
	public void setEchoFile(String file)
	{
		this.filename = file;
		try
		{
			ps = new PrintStream(new FileOutputStream(file));
		}
		catch (Exception ex)
		{
			ps = null;
			
		}
	}

	public int getAbortKey()
	{
		return abortKey;
	}

	public void setAbortKey(int n)
	{
		abortKey = n;
	}
	
	public String toString()
	{
		return getName()+" - "+getNumRow()+"x"+getNumCol();
	}
	
}

   