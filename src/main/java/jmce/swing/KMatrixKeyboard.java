/**
   $Id: KMatrixKeyboard.java 692 2011-09-02 08:38:10Z mviara $

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

import java.awt.*;
import javax.swing.*;

/**
 * Matrix keyboard.
 * <p>
 * This class rappresent a sample Swing matrix keyboard.
 *
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public class KMatrixKeyboard extends JPanel
{
	private static final long serialVersionUID = 1L;
	private int numRows = 0;
	private int numCols = 0;
	private AbstractButton keysButton[][];
	private JComponent keysComponent[][];
	private String keysString[][];
	private int keys[][];

	/**
	 * Default constructor.
	 * <p>
	 * Set matrix for simple 4x4 keypad..
	 * 
	 */
	public KMatrixKeyboard()
	{
		super(new GridBagLayout());
		setNumRows(4);
		setNumCols(4);

		setKey(0,0,'1');
		setKey(0,1,'2');
		setKey(0,2,'3');
		setKey(0,3,'A');

		setKey(1,0,'4');
		setKey(1,1,'5');
		setKey(1,2,'6');
		setKey(1,3,'B');

		setKey(2,0,'7');
		setKey(2,1,'8');
		setKey(2,2,'9');
		setKey(2,3,'C');

		setKey(3,0,'*');
		setKey(3,1,'0');
		setKey(3,2,'#');
		setKey(3,3,'D');


	}

	public void setComponent(int r,int c,JComponent j)
	{
		GridBagConstraints g = new GridBagConstraints();
		g.gridx = c;
		g.gridy = r;
		add(j,g);
		keysComponent[r][c] = j;
	}
	
	private void setKey(int r,int c,AbstractButton b)
	{
		keysButton[r][c] = b;
		b.setMnemonic(keys[r][c]);
		setComponent(r,c,b);
	}

	private void setKey(int r,int c,String s)
	{
		keysString[r][c] = s;
		setKey(r,c,new JButton(s));
	}

	public void setKey(int r,int c,char s)
	{
		keys[r][c] = s;
		setKey(r,c,""+s);
	}

	public void setNumCols(int n)
	{
		if (n != numCols)
		{
			numCols = n;
			createArray();
		}
	}

	public void setNumRows(int n)
	{
		if (n != numRows)
		{
			numRows = n;
			createArray();
		}
	}

	private void createArray()
	{
		keysButton = new AbstractButton[numRows][numCols];
		keysComponent = new JComponent[numRows][numCols];
		keysString = new String[numRows][numCols];
		keys       = new int[numRows][numCols];

		removeAll();
	}


}
