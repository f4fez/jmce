/**
   $Id: KeyboardMatrix.java 615 2011-05-31 09:25:43Z mviara $

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
package jmce.swing;

import jmce.sim.*;
import jmce.util.Logger;
import java.awt.event.*;

/**
 * Keyboard matrix.
 * <p>
 * This swing peripheral can be connected to any AWT component and
 * implements one matrix keyboard.
 * <p>
 * The matrix is organized in rows and columns. Normally the row are
 * scanned one at once and if a key is pressed the relative columns bit
 * is set to 0.
 *
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.01
 */
public class KeyboardMatrix extends AbstractPeripheral implements FocusListener,KeyListener
{
	private static Logger log = Logger.getLogger(KeyboardMatrix.class);
	
	protected int numRows = 16;
	private int numCols = 16;
	private int keysMap[][] = new int[16][16];
	protected int keyState[] = new int[16];
	protected int keysLoc[][] = new int[16][16];
	
	/**
	 * Standard constructor
	 */
	public KeyboardMatrix()
	{
		setName("Matrix Kbd");
		initKeyboard();
		
	}

	/**
	 * Initialize all data to default value.
	 * <p>
	 * Called by the constructor and when the size the keyboard is
	 * changed.
	 */
	private void initKeyboard()
	{
		keysMap = new int[numRows][numCols];
		keysLoc = new int[numRows][numCols];
		keyState = new int[numRows];

		for (int r = 0 ; r < numRows ; r++)
		{
			for (int c = 0 ; c < numCols ; c++)
			{
				keysMap[r][c] = KeyEvent.VK_UNDEFINED;
				keysLoc[r][c] = KeyEvent.KEY_LOCATION_UNKNOWN;
			}
		}

		resetKeyboard();
		

	}

	/**
	 * Reset the state of the keyboard.
	 * <p>
	 */
	protected void resetKeyboard()
	{
		for (int i = 0 ; i < numRows ; i++)
			keyState[i] = (1 << numCols) - 1;
	}

	/**
	 * Set the number of row for the matrix
	 */
	public void setNumRows(int r)
	{
		numRows = r;
		initKeyboard();

	}

	/**
	 * Set the number of columns of the matrix.
	 */
	public void setNumCols(int c)
	{
		numCols = c;
		initKeyboard();
	}

	public void setComponent(java.awt.Component c)
	{
		c.setFocusable(true);
		c.addFocusListener(this);
		c.addKeyListener(this);

	}

	/**
	 * Return the key state for the specified row.
	 */
	public int getRow(int r)
	{
		return keyState[r];
	}

	/**
	 * Set the location of specified key.
	 * <p>
	 * The location is used when the same key appear more than one
	 * time on the keyboard and can generate more than one code. A
	 * typical example are the LEFT / RIGHT shift.
	 */
	public void setKeyLocation(int r,int c,int ... keys)
	{
		for (int k : keys)
			keysLoc[r][c++] = k;
	}
	
	public void setKey(int r,int c,int ... keys)
	{
		for (int k : keys)
			keysMap[r][c++] = k;
	}

	public void focusGained(FocusEvent e)
	{
		resetKeyboard();
	}

	public void focusLost(FocusEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		keyEvent(e,true);
		e.consume();

	}

	public void keyReleased(KeyEvent e)
	{

		keyEvent(e,false);
		e.consume();

	}

	public void keyTyped(KeyEvent e)
	{
	}


	public void keyEvent(KeyEvent e,boolean pressed)
	{
		int key = e.getKeyCode();
		int loc = e.getKeyLocation();
		
		for (int r = 0 ; r < numRows ; r ++)
			for (int c = 0 ; c < numCols ; c++)
			{
				// Check for defined key
				if (keysMap[r][c] == KeyEvent.VK_UNDEFINED)
					continue;

				// Check for location of the key
				if (keysLoc[r][c] != KeyEvent.KEY_LOCATION_UNKNOWN && keysLoc[r][c] != loc)
					continue;

				// Check the code of the key
				if (keysMap[r][c] != key)
					continue;
				
				log.fine("Key="+KeyEvent.getKeyText(e.getKeyCode())+" Pressed="+pressed+" R="+r+" C="+c);
				
				keyEvent(r,c,pressed);
				return;
			}
	}

	/**
	 * Can be redefined in subclass to detect keyboard event for
	 * example to fire a interrupt.
	 */
	protected void keyEvent(int r,int c,boolean pressed)
	{
		if (pressed)
		{
			keyState[r] &= ~(1 << c);
		}
		else
		{
			keyState[r] |= (1 << c);
		}
	}


	public String toString()
	{
		return getName()+" R="+numRows+" C="+numCols;
	}
}


