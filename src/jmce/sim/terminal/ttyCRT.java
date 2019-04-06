/**
   $Id: ttyCRT.java 501 2011-01-13 17:06:41Z mviara $

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


import java.awt.event.KeyEvent;

import jmce.sim.*;


/**
 * TTY implementation of CRT.
 * 
 * <p>Native class for tty CRT. 
 *
 * Current implementation support :
 * <ul>
 *  <li>Win32 console.</li>
 *  <li>Unix (curses> console.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 */
public class ttyCRT extends AbstractCRT implements Runnable
{
	
	public final int UP	= 0x4800;
	public final int DOWN	= 0x5000;
	public final int LEFT	= 0x4b00;
	public final int RIGHT	= 0x4d00;
	public final int KEY_F10= 0x4400;
	public final int KEY_DEL= 0x5300;
	
	private native void	ttyPutchar(byte chars[],int len,int att);
	private native void	ttySetCursorPosition(int row,int col);
	private native void	ttySetCursor(boolean mode);
	private native boolean	ttyKbhit();
	private native int	ttyGetch();
	private native int	ttyGetNumRow();
	private native int	ttyGetNumCol();
	private native boolean	ttyInit();
	private native void	ttyReset();
	private int NROW,NCOL;
	private byte		line[];
	private Thread thread = null;
	
	public ttyCRT()
	{
		System.loadLibrary("jmce");
		ttyInit();
		NROW = ttyGetNumRow();
		NCOL = ttyGetNumCol();
	}

	


	public void init(Hardware parent) throws SIMException
	{
		terminal = (Terminal)parent;
		int r = terminal.getNumRow() - terminal.getNumStatus();


		if (NROW > r)
			terminal.setNumStatus(NROW - r);
		else
			terminal.setNumStatus(0);
		
		
		terminal.setNumRow(NROW);
		terminal.setNumCol(NCOL);
		

		super.init(parent);
		line = new byte[NCOL];

		thread = new Thread(this);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
		
	}


	public void destroy()
	{
		if (thread != null)
			thread.interrupt();
		thread = null;
		try
		{
			Thread.sleep(100);
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Runner for polling the keyboard and if the screen is changed
	 * update the CRT.
	 */
	public void run()
	{
		while (thread != null)
		{
			try
			{
				Thread.sleep(20);
			}
			catch (Exception e)
			{
				
			}

			if (thread == null)
				continue;
			
			if (ttyKbhit())
			{

				int c = ttyGetch();
				
				switch (c)
				{
					case	KEY_DEL:
						c = 0x10000 | KeyEvent.VK_DELETE;
						break;
						    
					case	KEY_F10:
							c = 0x10000 | KeyEvent.VK_F10;
							break;
					case	UP:
							c = 0x10000 | KeyEvent.VK_UP;
							break;
					case	DOWN:
							c = 0x10000 | KeyEvent.VK_DOWN;
							break;
					case	LEFT:
							c = 0x10000 | KeyEvent.VK_LEFT;
							break;
					case	RIGHT:
							c = 0x10000 | KeyEvent.VK_RIGHT;
							break;
				}

				try
				{
					terminal.write(c);
				}
				catch (Exception ignore)
				{
				}

			}


			if (!screenChanged)
				continue;
			
			synchronized (lineChanged)
			{
				try
				{
				screenChanged = false;

				ttySetCursor(false);

				for (int r = 0 ; r < NROW ; r++)
				{

					if (lineChanged[r])
					{
						lineChanged[r] = false;
						int count = 0;
						int pos = r * NCOL;
						int att = getAtt(r,0);

						ttySetCursorPosition(r,0);

						for (int c = 0 ; c < NCOL ;c++,pos++)
						{
							//System.out.println("pos "+pos+" count "+count+" r "+r+" c "+c+" NROW "+NROW+" MNCOL "+NCOL);

							if (att != getAtt(r,c))
							{
								ttyPutchar(line,count,att);
								att = getAtt(r,c);
								count = 0;
							}

							line[count++] = (byte)getChar(r,c);
						}

						ttyPutchar(line,count,att);
					}
				}

				if (terminal.getCursor() == true)
				{
					ttySetCursorPosition(terminal.getRow(),terminal.getCol());
					ttySetCursor(true);
				}
				}
				catch (Exception ex)
				{
				}

			}
		}

		ttyReset();
		
	}
	
}
