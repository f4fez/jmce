/**
   $Id: Test.java 510 2011-01-18 09:25:07Z mviara $

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

import jmce.util.Hex;

class TestTerminal
{
	
	static public void testTitle(SampleTerminal t,String s) throws Exception
	{
		t.cls();
		t.home();
		t.println("\n\t\t"+s);
		t.printStatusLine(0,s);
	}

	static void testContinue(SampleTerminal t) throws Exception
	{
		t.print("\nPress any key to continue");
		t.readOutput();
	}

	static void testCursor(SampleTerminal t) throws Exception
	{
		testTitle(t,"Test cursor");
		for (int i = 0 ; i < 10 ; i++)
		{
			t.setCursor(i+5,i);
			t.print("Row="+(i+5)+", Column="+i);
		}
		t.println();
		testContinue(t);
	}

	static void testValue(SampleTerminal t,String s1,String s2) throws Exception
	{
		while (s1.length() < 32)
			s1 = " "+s1;
		t.print(s1+" : ");
		t.setAtt(Terminal.HI);
		t.println(s2);
		t.setAtt(Terminal.NORMAL);

	}

	static void testValue(SampleTerminal t,String s1,int n) throws Exception
	{
		testValue(t,s1,""+n);
	}

	static void testInfo(SampleTerminal t) throws Exception
	{
		testTitle(t,"Terminal info");
		testValue(t,"Terminal name",t.getName());
		testValue(t,"Number of rows",t.getNumRow());
		testValue(t,"Number of status row",t.getNumStatus());
		testValue(t,"Number of columns",t.getNumCol());
		testValue(t,"Font ",t.getFont().toString());
		testContinue(t);
	}

	static void testAttribute(SampleTerminal t) throws Exception
	{

		testTitle(t,"Test attribute");
		for (int i = 0 ; i < 16 ; i++)
		{

			t.setAtt(Terminal.NORMAL);
			t.print("Atribute "+Hex.formatByte(i)+ " ");
			t.setAtt(i);
			if (i == 0)
				t.print("NORMAL");
			else
			{
				if ((i & Terminal.REVERSE) != 0)
					t.print("REVERSE ");
				if ((i & Terminal.HI) != 0)
					t.print("HIGHLIGHT ");
				if ((i & Terminal.UNDERLINE) != 0)
					t.print("UNDERLINE ");
				if ((i & Terminal.BLINK) != 0)
					t.print("BLINK ");
			}
			t.println();
		}			

		t.setAtt(Terminal.NORMAL);
		testContinue(t);

	}

	static public void test(SampleTerminal t) throws Exception
	{
		
		for (;;)
		{
			t.cls();
			t.home();
			t.setAtt(Terminal.NORMAL);
			t.printStatusLine(0,t.toString());
			t.println("\n\tTerminal test\n\n");
			t.println("1. Terminal information");
			t.println("2. Test attribute");
			t.println("3. Test cursor");
			t.println("0. Perform all test\n");
			t.print("\t\tChoice (ESC Stop) :");
			
			int c = t.readOutput();

			if (c == 27)
				break;
			switch (c)
			{
				case	'1':
					testInfo(t);
					break;
				case	'2':
					testAttribute(t);
					break;
				case	'3':
					testCursor(t);
					break;
				case	'0':
					testInfo(t);
					testAttribute(t);
					testCursor(t);
			}
		}



	}
	
}

/**
 * Test for sample terminal.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Test
{
	static public void main(String argv[])
	{
		SampleTerminal t = new Hazeltine1500();
		new ttyCRT();
		CRT c2 = new SwingCRT();
		
		t.setNumStatus(1);
		
		//t.addHardware(c1);
		t.addHardware(c2);


		try
		{
			t.init(null);
			t.initSwing(null);
			t.reset();

			TestTerminal.test(t);
			t.destroy();

		}
		catch (Exception e)
		{
			System.out.println(e);
		}

		System.exit(0);
		
	}

	
	
}
