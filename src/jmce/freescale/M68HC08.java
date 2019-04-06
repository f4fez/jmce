/**
   $Id: M68HC08.java 814 2012-03-29 11:07:49Z mviara $

   Copyright (c) 2011, Mario Viara

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
package jmce.freescale;

import jmce.sim.Register;
import jmce.sim.SIMException;
import jmce.sim.cpu.AbstractOpcode;
import jmce.sim.cpu.StandardRegister;

/**
 * Motorola / Freescale M68HC08 family.<p>
 * <p>
 * Implemented registers other than M68HC05 :
 * 
 * <ul>
 *  <li>H 8 bit index register.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @since 1.02
 */
public class M68HC08 extends M68HC05 implements M68HC08Constants
{
	/** Cpu register */
	private int H;
	
	/**
	 * Default constructor
	 */
	public M68HC08()
	{
		super("M68HC08");
		setResetVector(M68HC08_RESET_VECTOR);
	}

	protected void initRegisters()
	{
		super.initRegisters();

		addRegister(new StandardRegister("H",Register.FAMILY_GENERAL,8)
		{
			public int getRegister()
			{
				return H;
			}

			public void setRegister(int value)
			{
				H = value;
			}

		});

	}

	protected void initOpcodes()
	{
		CPUOperation op;
		super.initOpcodes();


		/** TXS */
		setOpcode(new AbstractOpcode(0x94,1,2,"TXS")
		{
			public int exec(int pc) throws SIMException
			{
				SP = (H << 8 | X) - 1;

				return 2;
			}
		});
		
		/** LDHX */
		setOpcode(new AbstractOpcode(0x45,3,3,"LDHX\t#%word")
		{
			public int exec(int pc) throws SIMException
			{
				H = getByte(pc+1);
				X = getByte(pc+2);

				ccr(CCR_N,bit7(H));
				ccr(CCR_Z,(H + X ) == 0);
				
				return 3;
			}
		});

		setOpcode(new AbstractOpcode(0x55,2,4,"LDHX\t%byte")
		{
			public int exec(int pc) throws SIMException
			{
				int a = getWord(getByte(pc+1));
				
				H = getByte(a+0);
				X = getByte(a+1);

				ccr(CCR_N,bit7(H));
				ccr(CCR_Z,(H + X ) == 0);

				return 3;
			}
		});

	}

}

