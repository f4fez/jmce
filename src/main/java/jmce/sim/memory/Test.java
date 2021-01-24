/**
   $Id: Test.java 613 2011-05-26 23:35:14Z mviara $

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
package jmce.sim.memory;

import jmce.util.Timeout;

/**
 * Simple test for memory performance.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class Test
{

	static void test(jmce.sim.Memory m) throws jmce.sim.SIMException
	{
		Timeout t = new Timeout();
		int size = m.getSize();
		
		System.out.print("Check memory '"+m+"'");

		System.out.print(" Read ");
		for (int cycle = 0 ; cycle < 500; cycle++)
			for (int i = 0 ; i < size ; i++)
				m.getMemory(i);
		System.out.print(t.getElapsed()+" ms, ");

		System.out.print("Write ");
		for (int cycle = 0 ; cycle < 500; cycle++)
			for (int i = 0 ; i < size ; i++)
				m.setMemory(i,i);
		System.out.println(t.getElapsed()+" ms");

		
	}
	
	static public void main(String argv[])
	{
		System.out.println("Test memory speed $Id: Test.java 613 2011-05-26 23:35:14Z mviara $");
		
		try
		{
			
			test(new PlainMemory("TEST",0x100000));
			BankedMemory bm = new BankedMemory("BANKED",0x100000,16,0x100,0x1000);
			test(bm);
			bm.initMmu();
			test(bm);
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
}
