/**
   $Id: Z80.java 814 2012-03-29 11:07:49Z mviara $

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
package jmce.zilog.z80;

import jmce.sim.*;
import jmce.sim.cpu.*;
import jmce.util.Hex;

/**
 * Standard Zilog Z80 cpu implementation.
 *<p>
 *
 * Implements all documented and not documented instruction set.
 * 
 * Implemented registers other then i8080:
 * 
 * <ul>
 *  <li> IY Index register 0.</li>
 *  <li> IX Index register 1.</li>
 *  <li> I Interrupt register.</li>
 *  <li> R Refresh register. The register is not incremented at every
 * fetch but the value will be the low part of the current cycle
 * counter.</li>
 * <li> A1,F1,H1,L1,B1,C1,D1,E1 an alternative set of standard {@link
 * jmce.intel.i8080.I8080} register.<li>
 *</ul>
 *
 * Tested using Z80 'Z80 instruction set exerciser' from Frank D.
 * Cringle.
 * <p>
 * <ul>
 *  <li>PRELIM.COM Test succesfully.</li>
 *  <li>ZEXDOC.COM Test succesfully.</li>
 *  <li>ZEXALL.COM not tested.</li>
 * </ul>
 * <p>
 * The mnemonic for JP xx where xx is one index register are
 * changed from the original Z80 for example the  JP (HL) is JP HL because is more
 * correct the jump is made to the address set in the register and not
 * in the memory location where the register is set.
 * 
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class Z80 extends jmce.intel.i8080.I8080
{
	/** Registers */
	private int A1,F1,BC1,DE1,HL1;
	public int IX,IY,I,R;
	private int interruptMode;

	/**
	 * Default constructor.
	 */
	public Z80()
	{
		this("Z80");
		
	}

	/**
	 * Constructor with the cpu name specified.
	 */
	public Z80(String name)
	{
		super(name);

		setClock(4500000);

	}

	protected void initRegisters() 
	{
		super.initRegisters();
		
		addRegister(new StandardRegister("IX",Register.FAMILY_INDEX,16,0)
		{
			public int getRegister()
			{
				return IX;
			}

			public void setRegister(int value)
			{
				IX = value;
			}


		});

		addRegister(new StandardRegister("IY",Register.FAMILY_INDEX,16,0)
		{
			public  int getRegister()
			{
				return IY;
			}

			public void setRegister(int value)
			{
				IY = value;
			}


		});

		addRegister(new StandardRegister("I",Register.FAMILY_CONTROL,8,0)
		{
			public int getRegister()
			{
				return I;
			}

			public void setRegister(int value)
			{
				I = value;
			}


		});
		
		addRegister(new StandardRegister("R",Register.FAMILY_CONTROL,8,0)
		{
			public int getRegister()
			{
				return (int)(getCycle() & 0xff);
			}

			public void setRegister(int value)
			{
				R = value;
			}


		});

	}

	/**
	 * Return the value of the I register.
	 *
	 * @since 1.01
	 */
	public final int I()
	{
		return I;
	}
	
	public final int srl(int value)
	{
		boolean  c = (value & 0x01) != 0;
		value = (value >>> 1);
		F = booleanTable[value];
		FLAG_C(c);

		return value;
	}

	public final int sll(int value)
	{
		boolean  c = (value & 0x80) != 0;
		value = ((value << 1) | 1) & 0xff;
		F = booleanTable[value];
		FLAG_C(c);

		return value;
	}

	public final int sra(int value)
	{
		boolean  c = (value & 0x01) != 0;
		value = ((value >>> 1)| ( value & 0x80)) & 0xff;
		F = booleanTable[value];
		FLAG_C(c);

		return value;

	}
	
	public final int sla(int value)
	{
		boolean  c = (value & 0x80) != 0;
		value = (value << 1) & 0xff;
		F = booleanTable[value];
		FLAG_C(c);
		
		return value;
		
	}

	void rld() throws SIMException
	{
		int t = A;
		int m = getByte(HL);

		A = (A & 0xF0) | ((m & 0xF0) >>> 4);
		m = ( (m & 0x0f) << 4) | (t & 0x0F);

		setByte(HL,m);

		FLAG_S((A & 0x80) != 0);
		FLAG_5((A & 0x20) != 0);
		FLAG_3((A & 0x08) != 0);
		FLAG_Z(A == 0);
		FLAG_H(false);
		FLAG_N(false);
		FLAG_V(parityTable[A]);

		
	}

	void rrd() throws SIMException
	{
		int t = A;
		int m = getByte(HL);
		
		A = (A & 0xF0) | (m & 0x0f);
		m = (m >>> 4) | ((t & 0x0f) << 4);

		setByte(HL,m);
		
		FLAG_S((A & 0x80) != 0);
		FLAG_5((A & 0x20) != 0);
		FLAG_3((A & 0x08) != 0);
		FLAG_Z(A == 0);
		FLAG_H(false);
		FLAG_N(false);
		FLAG_V(parityTable[A]);


	}

	/**
	 * Negate the accumulator.
	 */
	private final void neg()
	{
		int t = A;
		
		A = -A & 0xff;
		
		F = 0;
		FLAG_S((A & 0x80) != 0);
		FLAG_5((A & 0x20) != 0);
		FLAG_3((A & 0x08) != 0);
		FLAG_Z(A == 0);
		FLAG_H((t & 0x0f) != 0);
		FLAG_V(t == 0x80);
		FLAG_N(true);
		FLAG_C(t != 0);
	}
	
	/**
	 * Reset one bit.
	 *
	 * @param value - value
	 * @param bit - Number of the bit.
	 *
	 * @return the value with then bit <tt>bit</tt> set to 0.
	 */
	public final int res(int value,int bit)
	{
		return value & ~(1 << bit);
	}

	/**
	 * Set one bit.
	 */
	public final int set(int value,int bit)
	{
		return value | (1 << bit);
	}

	/**
	 * Check the status of one bit.
	 */
	public final int bit(int value,int bit)
	{
		value &= (1 << bit);
		FLAG_Z(value == 0);
		FLAG_S((value & 0x80) != 0);
		FLAG_H(true);
		FLAG_N(false);

		return value;
	}
	
		

	/**
	 * Change the AF with AF1 register
	 */
	public final void ex_af_af1()
	{
		int v;
		
		v = F;F = F1;F1 = v;
		v = A;A = A1;A1 = v;
		
	}

	/**
	 * Return the interrupt mode.
	 *
	 * @since 1.01
	 */
	public final int im()
	{
		return interruptMode;
	}
	
	/**
	 * Set the interrupt mode.
	 */
	public final void im(int n)
	{
		interruptMode = n;
	}
	
	/**
	 * Exchange the  bank of Z80 register BC,DE,HL
	 */
	public final  void exx()
	{
		int v;
		
		v = BC; BC = BC1; BC1 = v;
		v = DE; DE = DE1; DE1 = v;
		v = HL; HL = HL1; HL1 = v;
	}

	protected void resetRegisters() throws SIMException
	{
		super.resetRegisters();
		A1 = F1 = BC1 = DE1 = HL1 = 0;

		/** At reset the interrupt mode is 1 */
		im(1);
	}

	/**
	 * Abstract implementation of JR
	 */
	abstract class JR extends AbstractOpcode
	{
		private int tjmp;

		JR(int opcode,int l,int tjmp,int tnojmp,String s)
		{
			super(opcode,l,tnojmp,s);
			this.tjmp = tjmp;
		}

		protected final int jr(int offset)
		{
			PC = addOffset(PC,offset);
			
			return tjmp;
		}

	}

	/**
	 * Special version of compare used by CPD and CPI is like the
	 * normal compare but the C and V flag are untouched.
	 */
	public final void cpSpecial(int v)
	{
		int savedF = F;
		cp(v);

		/** Restore CY and PV */
		FLAG_C((savedF & FLAG_C) != 0);
		FLAG_V((savedF & FLAG_PV) != 0);
	}
	 
	/**
	 * CPI compare a with (HL) and increment HL and decrement BC
	 */
	public void cpi() throws SIMException
	{
		cpSpecial(getByte(HL));

		HL = (HL + 1) & 0xffff;
		BC = (BC - 1) & 0xffff;
		FLAG_V(BC != 0);

	}

	/**
	 * CPD Compare A with (HL) and decrement HL and BC
	 */
	public void cpd() throws SIMException
	{
		cpSpecial(getByte(HL));

		HL = (HL - 1) & 0xffff;
		BC = (BC - 1) & 0xffff;

		FLAG_V(BC != 0);
	}

	/**
	 * IND Input from the port C store the value at (HL) and
	 * decrement the HL register.
	 */
	public void ind() throws SIMException
	{
		int v = in(BC & 0xff,BC >> 8);
		setByte(HL,v);
		
		HL = (HL - 1) & 0xffff;
		v = ((BC >>> 8) - 1) & 0xff;
		b(v);
		
		FLAG_Z(v == 0);
		FLAG_N(true);
		

		
	}

	/**
	 * INI Input from the port C store the value at (HL) and
	 * increment the HL pair register.
	 */
	public void ini() throws SIMException
	{
		int v = in(BC & 0xff,BC >> 8);
		setByte(HL,v);

		HL = (HL + 1) & 0xffff;
		v = ((BC >>> 8) - 1) & 0xff;
		b(v);

		FLAG_Z(v == 0);
		FLAG_N(true);
		


	}
	

	public void outd() throws SIMException
	{
		int v = ((BC >>> 8) - 1) & 0xff;
		b(v);
		out(BC & 0xff,BC >> 8,getByte(HL));

		HL = (HL - 1) & 0xffff;
		FLAG_Z(v == 0);
		FLAG_N(true);



	}

	public void outi() throws SIMException
	{
		int v = ((BC >>> 8) - 1) & 0xff;
		b(v);
		out(BC & 0xff,BC >> 8,getByte(HL));

		HL = (HL + 1) & 0xffff;


		FLAG_Z(v == 0);
		FLAG_N(true);



	}
	
	public void ldi() throws SIMException
	{
		setByte(DE,getByte(HL));

		HL = (HL + 1) & 0xffff;
		DE = (DE + 1) & 0xffff;
		BC = (BC - 1) & 0xffff;

		FLAG_H(false);
		FLAG_N(false);
		FLAG_5(false);
		FLAG_3(false);
				
		FLAG_V(BC != 0);
	}


	public void ldd() throws SIMException
	{
		setByte(DE,getByte(HL));

		HL = (HL - 1) & 0xffff;
		DE = (DE - 1) & 0xffff;
		BC = (BC - 1) & 0xffff;

		FLAG_H(false);
		FLAG_N(false);
		FLAG_5(false);
		FLAG_3(false);
		
		FLAG_V(BC != 0);
	}

	/**
	 * Initialize opcode pages EDxx
	 */
	private void initOpcodes_ED()
	{

		MultiOpcode ed = new MultiOpcode(0xED);


		// Operation on B,C,D,E,H,L,(HL),A
		for (int i = 0 ; i < 8 ; i ++)
		{
			if (i != 6)
			{
				// OUT (C),rrr
				ed.setOpcode(new AbstractOpcode(0x41|(i << 3),2,9,"OUT\t(C),%1%rr3")
				{
					public int exec(int pc) throws SIMException
					{
						out(BC & 0xff,BC >> 8,getValueRRR(opcode >> 3));
						return 9;
					}
				});
				
				// IN rrr,(C)
				ed.setOpcode(new AbstractOpcode(0x40|(i<<3),2,9,"IN\t%1%rr3,(C)")
				{
					public int exec(int pc) throws SIMException
					{
						int n = in(BC & 0xff,BC >> 8);
						
						FLAG_S((n & 0x80) != 0);
						FLAG_Z(n == 0);
						FLAG_H(false);
						FLAG_V(parityTable[n]);
						FLAG_N(false);

						setValueRRR(opcode >> 3,n);
						
						return 9;
					}
				});
			}
		}
		
		
		// LD I,A
		ed.setOpcode(new AbstractOpcode(0x47,2,9,"LD\tI,A")
		{
			public int exec(int pc) throws SIMException
			{
				I = A;
				return 9;
			}
		});


		// LD A,I
		ed.setOpcode(new AbstractOpcode(0x57,2,9,"LD\tA,I")
		{
			public int exec(int pc) throws SIMException
			{
				A = I;
				F = booleanTable[A];
				FLAG_V(iff2);
				return 9;
			}
		});

		// LD R,A
		ed.setOpcode(new AbstractOpcode(0x4F,2,9,"LD\tA,R")
		{
			public int exec(int pc) throws SIMException
			{
				R = (int)((getCycle() & 0xff));
				return 9;
			}
		});

		// LD A,R
		ed.setOpcode(new AbstractOpcode(0x5F,2,9,"LD\tA,R")
		{
			public int exec(int pc) throws SIMException
			{
				A = R;
				F = booleanTable[A];
				FLAG_V(iff2);
				return 9;
			}
		});

		// RLD
		ed.setOpcode(new AbstractOpcode(0x6F,2,18,"RLD\t(HL)")
		{
			public int exec(int pc) throws SIMException
			{
				rld();

				return 18;
			}
		});

		
		// RRD
		ed.setOpcode(new AbstractOpcode(0x67,2,18,"RRD\t(HL)")
		{
			public int exec(int pc) throws SIMException
			{
				rrd();

				return 18;
			}
		});
		
		// NEG
		ed.setOpcode(new AbstractOpcode(0x44,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}
				
		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x4c,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x54,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x5C,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x64,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x6C,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x74,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// Undocumented NEG
		ed.setOpcode(new AbstractOpcode(0x7C,2,8,"NEG")
		{
			public int exec(int pc) throws SIMException
			{
				neg();

				return 8;
			}

		});

		// RETN
		ed.setOpcode(new AbstractOpcode(0x45,2,14,"RETN")
		{
			public int exec(int pc) throws SIMException
			{
				pc(pop());
				iff1 = iff2;
				return 14;
			}

		});


		// RETI
		ed.setOpcode(new AbstractOpcode(0x4D,2,14,"RETI")
		{
			public int exec(int pc) throws SIMException
			{
				pc(pop());

				return 20;
			}
			
		});
		
		// Operation on BC,DE,HL,SP
		for (int i = 0 ; i < 4 ; i++)
		{
			ed.setOpcode(new AbstractOpcode(0x43|(i << 4),4,20,"LD\t(%2%word),%-2%pp")
			{
				public final int exec(int pc) throws SIMException
				{
					setWord(getWord(pc+2),getValuePP(opcode));

					return 20;
				}
			});

			
			ed.setOpcode(new AbstractOpcode(0x42|(i << 4),2,15,"SBC\tHL,%1%pp")
			{
				public int exec(int pc) throws SIMException
				{
					HL = sbc16(HL,getValuePP(opcode),FLAG_C() ? 1 : 0);

					return 11;
				}

			});

			
			ed.setOpcode(new AbstractOpcode(0x4a|(i<<4),2,15,"ADC\tHL,%1%pp")
			{
				public int exec(int pc) throws SIMException
				{
					HL = adc16(HL,getValuePP(opcode),FLAG_C() ? 1 : 0);

					return 15;
				}
			});

			ed.setOpcode(new AbstractOpcode(0x4b|(i<<4),4,20,"LD\t%1%pp,(%word)")
			{
				public int exec(int pc) throws SIMException
				{
					setValuePP(opcode,getWord(getWord(pc+2)));

					return 20;
				}
			});
		}
		

		ed.setOpcode(new AbstractOpcode(0xb0,2,21,"LDIR")
		{
			public int exec(int pc) throws SIMException
			{
				ldi();

				if (BC != 0)
				{
					pc(pc);
					return 21;
				}

				return 16;
			}

		});


		ed.setOpcode(new AbstractOpcode(0xa9,2,21,"CPD")
		{
			public int exec(int pc) throws SIMException
			{
				cpd();

				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xb9,2,21,"CPDR")
		{
			public int exec(int pc) throws SIMException
			{
				cpd();

				if ((BC != 0) && !FLAG_Z())
				{

					pc(pc);
					return 21;
				}

				return 16;
			}

		});


		ed.setOpcode(new AbstractOpcode(0xa1,2,21,"CPI")
		{
			public int exec(int pc) throws SIMException
			{
				cpi();

				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xb1,2,21,"CPIR")
		{
			public int exec(int pc) throws SIMException
			{
				cpi();
				
				if ((BC != 0) && !FLAG_Z())
				{

					pc(pc);
					return 21;
				}

				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xa3,2,21,"OUTI")
		{
			public int exec(int pc) throws SIMException
			{
				outi();


				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xb3,2,21,"OTIR")
		{
			public int exec(int pc) throws SIMException
			{
				outi();

				if (((BC >>> 8) & 0xff) != 0)
					pc(pc);

				return 16;
			}

		});
		
		ed.setOpcode(new AbstractOpcode(0xab,2,21,"OUTD")
		{
			public int exec(int pc) throws SIMException
			{
				outd();


				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xbb,2,21,"OTDR")
		{
			public int exec(int pc) throws SIMException
			{
				outd();

				if (((BC >>> 8) & 0xff) != 0)
					pc(pc);

				return 16;
			}

		});


		ed.setOpcode(new AbstractOpcode(0xa2,2,21,"INI")
		{
			public int exec(int pc) throws SIMException
			{
				ini();


				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xb2,2,21,"INIR")
		{
			public int exec(int pc) throws SIMException
			{
				ini();

				if (((BC >>> 8) & 0xff) != 0)
					pc(pc);

				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xaa,2,21,"IND")
		{
			public int exec(int pc) throws SIMException
			{
				ind();


				return 16;
			}

		});

		ed.setOpcode(new AbstractOpcode(0xba,2,21,"INDR")
		{
			public int exec(int pc) throws SIMException
			{
				ind();

				if (((BC >>> 8) & 0xff) != 0)
					pc(pc);

				return 16;
			}

		});


		
		ed.setOpcode(new AbstractOpcode(0xa0,2,21,"LDI")
		{
			public int exec(int pc) throws SIMException
			{
				ldi();

				
				return 16;
			}

		});


		ed.setOpcode(new AbstractOpcode(0xa8,2,21,"LDD")
		{
			public int exec(int pc) throws SIMException
			{
				ldd();


				return 16;
			}

		});


		ed.setOpcode(new AbstractOpcode(0xb8,2,21,"LDDR")
		{
			public int exec(int pc) throws SIMException
			{
				ldd();
				
				if (BC != 0)
				{
					pc(pc);
					return 21;
				}

				return 16;
			}

		});
			
		ed.setOpcode(new AbstractOpcode(0x46,2,8,"IM\t0")
		{
			public int exec(int pc) throws SIMException
			{
				im(0);

				return 8;
			}

		});
		
		ed.setOpcode(new AbstractOpcode(0x56,2,8,"IM\t1")
		{
			public int exec(int pc) throws SIMException
			{
				im(1);

				return 8;
			}

		});
		
		ed.setOpcode(new AbstractOpcode(0x5E,2,8,"IM\t2")
		{
			public int exec(int pc) throws SIMException
			{
				im(2);

				return 8;
			}

		});
		

		
		setOpcode(ed);
	}

	/**
	 * SKIP instruction
	 */
	class SKIP extends AbstractOpcode
	{
		SKIP(int opcode)
		{
			super(opcode,1,0,"Skip\t"+Hex.formatByte(opcode));
			
		}

		public int exec(int pc) throws SIMException
		{
			return 0;
		}

	}

	
	class SET_B_R extends AbstractOpcode
	{
		SET_B_R(int b,int r)
		{
			super(0xC0 + b * 8 + r,2,8,"SET\t"+b+",%1%rrr");
		}

		public int exec(int pc) throws SIMException
		{
			int bit = (opcode >> 3) & 7;
			int value = getValueRRR(opcode);
			value |= (1 << bit);
			setValueRRR(opcode,value);

			return (opcode & 0x07) == 6 ? 15 : 8;
		}

	}

	class RES_B_R extends AbstractOpcode
	{
		RES_B_R(int b,int r)
		{
			super(0x80 + b * 8 + r,2,8,"RES\t"+b+","+getRRR(r));
		}

		public int exec(int pc) throws SIMException
		{
			int bit = (opcode >> 3) & 7;
			int value = getValueRRR(opcode);
			value &= ~(1 << bit);
			setValueRRR(opcode,value);

			return (opcode & 0x07) == 6 ? 15 : 8;
		}
		
	}

	class RR_R extends AbstractOpcode
	{
		RR_R(int r)
		{
			super(0x18 | r,2,4,"RR\t%rrr");
		}

		public final int exec(int pc) throws SIMException
		{
			setValueRRR(opcode,rr(getValueRRR(opcode)));

			return (opcode & 0x07) == 6 ? 4 : 2;
		}
	}

	class SRL_R extends AbstractOpcode
	{
		SRL_R(int r)
		{
			super(0x38 | r,2,4,"SRL\t%rrr");
		}

		public final int exec(int pc) throws SIMException
		{
			setValueRRR(opcode,srl(getValueRRR(opcode)));

			return (opcode & 0x07) == 6 ? 4 : 2;
		}
	}

	class RLC_R extends AbstractOpcode
	{
		RLC_R(int r)
		{
			super(0x00| r,2,4,"RLC\t%1%rrr");
		}

		public final int exec(int pc) throws SIMException
		{
			setValueRRR(opcode,rlc(getValueRRR(opcode)));

			return (opcode & 0x07) == 6 ? 4 : 2;
		}

	}
	
	class BIT_B_R extends AbstractOpcode
	{
		BIT_B_R(int b,int r)
		{
			super(0x40 + b * 8 + r,2,8,"BIT\t"+b+",%1%rrr");
		}

		public final int exec(int pc) throws SIMException
		{
			
			bit(getValueRRR(opcode),(opcode >> 3) & 0x07);

			return (opcode & 0x07) == 6 ? 12 : 8;
		}
	}

	interface IndexRegister
	{
		public void set(int value);
		public int get();
		public String name();
	}

	abstract class AbstractIndex extends AbstractOpcode
	{
		IndexRegister id;
		
		AbstractIndex(IndexRegister id,int opcode,int l,int t,String s)
		{
			super(opcode,l,t,s);
			this.id = id;
		}

		public final void setXYH(int v)
		{
			setXY((getXY() & 0x00ff) | ( v  << 8));
		}

		public final void setXYL(int v)
		{
			setXY((getXY() & 0xff00) | (v & 0xff));
		}

		public final int getXYL()
		{
			return getXY() & 0xff;
		}

		public final int getXYH()
		{
			return getXY() >>> 8;
		}
		
		public final int getXY()
		{
			return id.get();
		}
		
		public void setXY(int value)
		{
			id.set(value);
		}

		public int getXYOffset(int n) throws SIMException
		{
			int offset = getByte(n);
			return addOffset(getXY(),offset);
		}
	}

	abstract class AbstractIndexOp1 extends AbstractIndex
	{
		OP1 op1;
		
		AbstractIndexOp1(IndexRegister id,int opcode,int l,int t,String s,OP1 op1)
		{
			super(id,opcode,l,t,s);
			this.op1 = op1;
		}
	};

	interface OP1
	{
		int op1(int v);
	};

	abstract class OP1BIT implements OP1
	{
		protected int bit;
		
		OP1BIT(int bit)
		{
			this.bit = bit;
		}
	};
	
				
	interface ALU
	{
		void alu(int v);

	};
	
	class AbstractIndexALUidH extends AbstractIndex
	{
		private ALU alu;
		
		AbstractIndexALUidH(IndexRegister id,int opcode,int l,int t,String s,ALU alu)
		{
			super(id,opcode,l,t,s);
			this.alu = alu;
		}

		public final int  exec(int pc) throws SIMException
		{
			alu.alu(getXY() >> 8);
			return 10;
		}


	};
	
	class AbstractIndexALUidL extends AbstractIndex 
	{
		private ALU  alu;
		
		AbstractIndexALUidL(IndexRegister id,int opcode,int l,int t,String s,ALU alu)
		{
			super(id,opcode,l,t,s);
			this.alu = alu;
		}

		public final int exec(int pc) throws SIMException
		{
			alu.alu(getXY() & 0xff);
			return 10;
		}


	};

	class AbstractIndexALUidOffset extends AbstractIndex
	{
		private ALU alu;
		
		AbstractIndexALUidOffset(IndexRegister id,int opcode,int l,int t,String s,ALU alu)
		{
			super(id,opcode,l,t,s);
			this.alu = alu;
		}

		public int exec(int pc) throws SIMException
		{
			
			alu.alu(getByte(addOffset(getXY(),getByte(pc+2))));
			return 10;
		}


	};

	private void addIndexAlu(MultiOpcode m,IndexRegister id,int opcode,String s,ALU alu)
	{
		m.setOpcode(new AbstractIndexALUidH(id,opcode+0,2,10,s+"\tA,"+id.name()+"H",alu));
		m.setOpcode(new AbstractIndexALUidL(id,opcode+1,2,10,s+"\tA,"+id.name()+"L",alu));
		m.setOpcode(new AbstractIndexALUidOffset(id,opcode+2,2+1,15,s+"\tA,("+id.name()+"+%1%offset)",alu));

	}

	void addIndex8Alu(MultiOpcode m,IndexRegister xy,int opcode,String s,OP1 op1)
	{
		for (int i = 0 ; i < 256 ; i++)
		{
			MultiOpcode m1 = (MultiOpcode)m.getOpcode(i);

			if (m1 == null)
			{
				m1 = new MultiOpcode(i);
				m.setOpcode(m1);
			}

			for (int r = 0 ; r < 8 ; r++)
			{
				m1.setOpcode(new AbstractIndexOp1(xy,opcode|r,4,10,s+"("+xy.name()+"+%1%byte)",op1)
				{

					public int exec(int pc) throws SIMException
					{
						int i = addOffset(getXY(),getByte(pc+2));
						op1.op1(getByte(i));
						return 10;

					}
				});
			}
		}
	}

	void addIndexRrrAlu(MultiOpcode m,IndexRegister xy,int opcode,String s,OP1 op1)
	{
		for (int i = 0 ; i < 256 ; i++)
		{
			MultiOpcode m1 = (MultiOpcode)m.getOpcode(i);
			
			if (m1 == null)
			{
				m1 = new MultiOpcode(i);
				m.setOpcode(m1);
			}
			
			for (int r = 0 ; r < 8 ; r++)
			{
				if (r != 6)
				{
					m1.setOpcode(new AbstractIndexOp1(xy,opcode|r,4,10,"LD\t%1%rrr,"+s+"("+xy.name()+"+%1%byte)",op1)
					{

						public int exec(int pc) throws SIMException
						{
							int i = addOffset(getXY(),getByte(pc+2));
							int v = getByte(i);
							v = op1.op1(v);
							setValueRRR(opcode,v);
							setByte(i,v);
							return 10;

						}
					});
				}
				else
				{
					m1.setOpcode(new AbstractIndexOp1(xy,opcode|r,4,10,s+"("+xy.name()+"+%1%byte)",op1)
					{

						public int exec(int pc) throws SIMException
						{
							int i = addOffset(getXY(),getByte(pc+2));
							int v = getByte(i);
							v = op1.op1(v);
							setByte(i,v);
							return 10;

						}
					});

				}

			}
		}
	}
	
	private void initOpcodeIdCB(MultiOpcode m,IndexRegister id)
	{
		MultiOpcode cb = new MultiOpcode(0xcb);
		m.setOpcode(cb);


		// XXX (XY+d)
		for (int i = 0 ; i < 256 ; i++)
		{
			MultiOpcode m1 = (MultiOpcode)cb.getOpcode(i);
			if (m1 == null)
			{
				m1 = new MultiOpcode(i);
				cb.setOpcode(m1);
			}
			/*
			m1.setOpcode(new AbstractIndex(id,0x0E,4,23,"RRC\t("+id.name()+"+%byte)")
			{
				public int exec(int pc) throws SIMException
				{
					int o = getXYOffset(pc+2);
					setByte(o,rrc(getByte(o)));
					return 23;
				}

			});

			m1.setOpcode(new AbstractIndex(id,0x06,4,23,"RLC\t("+id.name()+"+%byte)")
			{
				public int exec(int pc) throws SIMException
				{
					int o = getXYOffset(pc+2);
					setByte(o,rlc(getByte(o)));
					return 23;
				}
				
			});
			
			m1.setOpcode(new AbstractIndex(id,0x26,4,23,"SLA\t("+id.name()+"+%byte)")
			{
				public int exec(int pc) throws SIMException
				{
					int o = getXYOffset(pc+2);
					setByte(o,sla(getByte(o)));
					return 23;
				}
			});

			m1.setOpcode(new AbstractIndex(id,0x2E,4,23,"SLA\t("+id.name()+"+%byte)")
			{
				public int exec(int pc) throws SIMException
				{
					int o = getXYOffset(pc+2);
					setByte(o,sra(getByte(o)));
					return 23;
				}
			});

			*/
		}
		
		// Add SET x,(XY+d)
		for (int b = 0 ;b < 8 ; b++)
		{
			// BIT x,(ZY+d)
			addIndex8Alu(cb,id,0x40+b*8,"BIT \t"+b+",",new OP1BIT(b)
			{
				public int op1(int value)
				{
					return bit(value,bit);
				}

			});
			// SET x,(XY+d)
			addIndexRrrAlu(cb,id,0xc0+b*8,"SET \t"+b+",",new OP1BIT(b)
			{
				public int op1(int value)
				{
					return set(value,bit);
				}

			});
			// RES x,(XY+D)
			addIndexRrrAlu(cb,id,0x80+b*8,"RES \t"+b+",",new OP1BIT(b)
			{
				public int op1(int value)
				{
					return res(value,bit);
				}

			});

		
		}
		// Logical operation on RRR,ALU (XY+D)
		addIndexRrrAlu(cb,id,0,"RLC",new OP1()
		{
			public int op1(int value)
			{
				return rlc(value);
			}
		});

		addIndexRrrAlu(cb,id,8,"RRC",new OP1()
		{
			public int op1(int value)
			{
				return rrc(value);
			}
		});

		addIndexRrrAlu(cb,id,0x10,"RL",new OP1()
		{
			public int op1(int value)
			{
				return rl(value);
			}
		});

		addIndexRrrAlu(cb,id,0x18,"RR",new OP1()
		{
			public int op1(int value)
			{
				return rr(value);
			}
		});

		addIndexRrrAlu(cb,id,0x20,"SLA",new OP1()
		{
			public int op1(int value)
			{
				return sla(value);
			}
		});

		addIndexRrrAlu(cb,id,0x28,"SRA",new OP1()
		{
			public int op1(int value)
			{
				return sra(value);
			}
		});

		addIndexRrrAlu(cb,id,0x30,"SLL",new OP1()
		{
			public int op1(int value)
			{
				return sll(value);
			}
		});
		

		addIndexRrrAlu(cb,id,0x38,"SRL",new OP1()
		{
			public int op1(int value)
			{
				return srl(value);
			}
		});

		
	}

	/**
	 * Opcode relative to the index register IX,IY
	 */
	private void initOpcodeXY(MultiOpcode m,IndexRegister id)
	{
		initOpcodeIdCB(m,id);


		// Skip xD 40 .. 43
		for (int i = 0x40 ; i < 0x44 ; i++)
			m.setOpcode(new SKIP(i));

		// Skip xD 47 .. 4B
		for (int i = 0x47 ; i < 0x4C ; i++)
			m.setOpcode(new SKIP(i));

		// Skip xD 4F .. 53
		for (int i = 0x4F; i < 0x54 ; i++)
			m.setOpcode(new SKIP(i));

		// Skip xD 57 .. 5B
		for (int i = 0x57; i < 0x5C ; i++)
			m.setOpcode(new SKIP(i));

		// Skip XD 5F
		m.setOpcode(new SKIP(0x5F));

		// Skip XD 78 .. 7B
		for (int i = 0x78; i < 0x7C ; i++)
			m.setOpcode(new SKIP(i));

		// Skip XD 7F .. 83
		for (int i = 0x7F; i < 0x84 ; i++)
			m.setOpcode(new SKIP(i));

		// EX (SP),XY
		m.setOpcode(new AbstractIndex(id,0xE3,2,20,"EX\t(SP),"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				int tmp = getByte(SP);
				setByte(SP,getXY() & 0xFF);
				setXYL(tmp);

				tmp = getByte(SP+1);
				setByte(SP+1,getXY() >>> 8);
				setXYH(tmp);

				return 20;
			}

		});

		// LD SP,XY
		m.setOpcode(new AbstractIndex(id,0xF9,2,8,"LD\tSP,"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				SP = getXY();
				return 8;
			}
			
		});

		// LD (nnnn),XY
		m.setOpcode(new AbstractIndex(id,0x22,4,22,"LD\t%1(%word%),"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				setWord(getWord(pc+2),getXY());
				return 22;

			}

		});
		// LD XY,(nnnn)
		m.setOpcode(new AbstractIndex(id,0x2A,4,20,"LD\t"+id.name()+",%1(%word%)")
		{
			public int exec(int pc) throws SIMException
			{
				setXY(getWord(getWord(pc+2)));
				return 20;

			}

		});
		

		// DEC XY
		m.setOpcode(new AbstractIndex(id,0x2B,2,10,"DEC\t"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				setXY((getXY() - 1) & 0xffff);
				return 10;

			}
			
		});

		// INC XY
		m.setOpcode(new AbstractIndex(id,0x23,2,10,"INC\t"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				setXY((getXY() + 1) & 0xffff);
				return 10;

			}

		});

		// INC (XY+d)
		m.setOpcode(new AbstractIndex(id,0x34,3,23,"INC\t("+id.name()+"+%1%byte)")
		{
			public int exec(int pc) throws SIMException
			{
				int i = getXYOffset(pc+2);
				setByte(i,inc(getByte(i)));
				return 23;

			}
		});
		

		// DEC (XY+d)
		m.setOpcode(new AbstractIndex(id,0x35,3,23,"DEC\t("+id.name()+"+%1%byte)")
		{
			public int exec(int pc) throws SIMException
			{
				int i = getXYOffset(pc+2);
				setByte(i,dec(getByte(i)));
				
				return 23;

			}
		});


		/**
		 * LD	(XY+d),n
		 */
		m.setOpcode(new AbstractIndex(id,0x36,4,19,"LD\t("+id.name()+"+%1%byte),%byte")
		{
			public int exec(int pc) throws SIMException
			{
				int i = getXYOffset(pc+2);

				setByte(i,getByte(pc+3));

				return 19;
			}	
		});

	


		// Arithmetic on id+d and undocumented idL and IDH
		addIndexAlu(m,id,0x94,"SUB",new ALU()
		{
			public void alu(int v)
			{
				 sub8(v);
			}
		});
		
		addIndexAlu(m,id,0x9c,"SBC",new ALU()
		{
			public void alu(int v)
			{
				sbc8(v,FLAG_C() ? 1 : 0);
			}
		});
		
		addIndexAlu(m,id,0x84,"ADD",new ALU()
		{
			public void alu(int v)
			{
				 add8(v);
			}
		});

		addIndexAlu(m,id,0x8c,"ADC",new ALU()
		{
			public void alu(int v)
			{
				adc8(v,FLAG_C() ? 1 : 0);
			}
		});


		addIndexAlu(m,id,0xA4,"AND",new ALU()
		{
			public void alu(int v)
			{
				 and(v);
			}
		});
		
		addIndexAlu(m,id,0xAC,"XOR",new ALU()
		{
			public void alu(int v)
			{
				xor(v);
			}
		});

		addIndexAlu(m,id,0xb4,"OR",new ALU()
		{
			public void alu(int v)
			{
				or(v);
			}
		});

		addIndexAlu(m,id,0xbc,"CP",new ALU()
		{
			public void alu(int v)
			{
				 cp(v);
			}
		});



		// Undocumented LD idL,byte
		m.setOpcode(new AbstractIndex(id,0x2E,3,15,"LD\t"+id.name()+"L,%1%byte")
		{
			public int exec(int pc) throws SIMException
			{
				int id = getXY();
				id = getByte(pc+2) | (id & 0xff00);
				setXY(id);
				
				return 10;

			}
		});

		// Undocumented LD idH,byte
		m.setOpcode(new AbstractIndex(id,0x26,3,15,"LD\t"+id.name()+"H,%1%byte")
		{
			public int exec(int pc) throws SIMException
			{
				int id = getXY();
				id = (getByte(pc+2) << 8) | (id & 0xff);
				setXY(id);
				return 10;
				
			}
		});

		// Undocumented INC idL
		m.setOpcode(new AbstractIndex(id,0x2c,2,15,"INC\t"+id.name()+"L")
		{
			public int exec(int pc) throws SIMException
			{
				int id = getXY();
				id = inc(id & 0xff) | (id & 0xff00);
				setXY(id);

				return 10;
			}
		});

		// Undocumented DEC idL
		m.setOpcode(new AbstractIndex(id,0x2d,2,15,"DEC\t"+id.name()+"L")
		{
			public int exec(int pc) throws SIMException
			{
				int id = getXY();
				id = dec(id & 0xff) | (id & 0xff00);
				setXY(id);

				return 10;
			}
		});

		// Undocumented DEC idH
		m.setOpcode(new AbstractIndex(id,0x25,2,15,"DEC\t"+id.name()+"H")
		{
			public int exec(int pc) throws SIMException
			{
				int id = getXY();
				id = (dec(id >> 8) << 8) | (id & 0xff);
				setXY(id);

				return 10;
			}
		});

		// Undocumented INC idH
		m.setOpcode(new AbstractIndex(id,0x24,2,15,"INC\t"+id.name()+"H")
		{
			public int exec(int pc) throws SIMException
			{
				int id = getXY();
				id = (inc(id >> 8) << 8) | (id & 0xff);
				setXY(id);

				return 10;
			}
		});
		
		// Operation on BC,DE,Ix,SP
		for (int i = 0 ; i < 4 ; i++)
		{
			if (i != 2)
			{
				m.setOpcode(new AbstractIndex(id,0x09|(i << 4),2,15,"ADD\t"+id.name()+",%1%pp")
				{
					public int exec(int pc) throws SIMException
					{
						setXY(add16(getXY(),getValuePP(opcode)));

						return 20;
					}
				});
			}
			else
			{
				m.setOpcode(new AbstractIndex(id,0x09|(i << 4),2,15,"ADD\t"+id.name()+","+id.name())
				{
					public int exec(int pc) throws SIMException
					{
						setXY(add16(getXY(),getXY()));

						return 20;
					}
				});
				
			}
		}
		
		
		// Operation on B,C,D,E,H,L,(HL),A
		for (int i = 0 ; i < 8 ; i ++)
		{
			if (i != 6)
			{
				// LD rrr,(XY+d)
				m.setOpcode(new AbstractIndex(id,0x46 | (i << 3),3,19,"LD\t%1%rr3,("+id.name()+"+%byte)")
				{
					public int exec(int pc) throws SIMException
					{
						int offset = getByte(pc+2);
						offset =  addOffset(getXY(),offset);

						setValueRRR(opcode >> 3,getByte(offset));

						return 19;
					}	

				});

				/**
				 * LD (XY+d),rrr
				 */
				m.setOpcode(new AbstractIndex(id,0x70 |i,3,19,"LD\t("+id.name()+"+%1%byte),%-2%rrr")
				{
					public int exec(int pc) throws SIMException
					{
						int offset = getByte(pc+2);
						offset =  addOffset(getXY(),offset);

						setByte(offset,getValueRRR(opcode));

						return 19;
					}	

				});
				
			}
			
			switch (i)
			{
				case	6: //	(HL)
					break;
					
				case	5:
					// LD XYL,XYL
					m.setOpcode(new AbstractIndex(id,0x6D,2,10,"LD\t"+id.name()+"L,"+id.name()+"L")
					{
						public int exec(int pc) throws SIMException
						{

							return 10;
						}	

					});
					
					// LD XYH,XYL
					m.setOpcode(new AbstractIndex(id,0x65,2,10,"LD\t"+id.name()+"H,"+id.name()+"H")
					{
						public int exec(int pc) throws SIMException
						{
							setXY((getXY() & 0xff) | ((getXY() & 0xff)) << 8);
							return 10;
						}	

					});
					break;
				case	4:
					// LD XYL,XYH
					m.setOpcode(new AbstractIndex(id,0x6c,2,10,"LD\t"+id.name()+"L,"+id.name()+"H")
					{
						public int exec(int pc) throws SIMException
						{
							setXYL(getXYH());
							return 10;
						}	

					});
					
					
					// LD XYH,XYH
					m.setOpcode(new AbstractIndex(id,0x64,2,10,"LD\t"+id.name()+"H,"+id.name()+"H")
					{
						public int exec(int pc) throws SIMException
						{

							return 10;
						}	

					});
					break;
				default:
					// LD XYL,rrr undocumented
					m.setOpcode(new AbstractIndex(id,0x68 | i,2,10,"LD\t"+id.name()+"L,%1%rrr")
					{
						public int exec(int pc) throws SIMException
						{
							setXYL(getValueRRR(opcode));

							return 10;
						}	

					});

					
					// LD XYH,rrr undocumented
					m.setOpcode(new AbstractIndex(id,0x60 | i,2,10,"LD\t"+id.name()+"H,%1%rrr")
					{
						public int exec(int pc) throws SIMException
						{
							setXYH(getValueRRR(opcode));

							return 10;
						}	

					});
					
			
			
					// LD rrr,XYH undocumented
					m.setOpcode(new AbstractIndex(id,0x44 | (i << 3),2,10,"LD\t%1%rr3,"+id.name()+"H")
					{
						public int exec(int pc) throws SIMException
						{
							setValueRRR(opcode >> 3,getXY() >> 8);

							return 10;
						}	

					});

					// LD rrr,XYL undocumented
					m.setOpcode(new AbstractIndex(id,0x45 | (i << 3),2,10,"LD\t%1%rr3,"+id.name()+"L")
					{
						public int exec(int pc) throws SIMException
						{
							setValueRRR(opcode >> 3,getXY() & 0xff);

							return 10;
						}	

					});

				
			}	
		}
			
			
		
		
		m.setOpcode(new AbstractIndex(id,0xe9,2,8,"JP\t"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				pc(getXY());

				return 8;
			}	
		});
		
		m.setOpcode(new AbstractIndex(id,0xe5,2,10,"PUSH\t"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				push(getXY());

				return 10;
			}	
		});
		m.setOpcode(new AbstractIndex(id,0xe1,2,10,"POP\t"+id.name())
		{
			public int exec(int pc) throws SIMException
			{
				setXY(pop());

				return 10;
			}	
		});
			
		
		m.setOpcode(new AbstractIndex(id,0x21,4,14,"LD\t"+id.name()+",%1%word")
		{
			public int exec(int pc) throws SIMException
			{
				setXY(getWord(pc+2));

				return 14;
			}	
		});
	}

	/**
	 * Initialize opcode FDxx (Operation relative to IY register)
	 */
	private void initOpcodes_FD()
	{
		MultiOpcode fd = new MultiOpcode(0xFD);
		
		initOpcodeXY(fd,new IndexRegister()
		{
			public final void  set(int value)
			{
				IY = value;
			}

			public final int get()
			{
				return IY;
			}

			public String name()
			{
				return "IY";
			}
		});
		
		setOpcode(fd);
	}

	/**
	 * Initialize opcode DDxx (Operation relative to IX register)
	 */
	private void initOpcodes_DD()
	{
		MultiOpcode dd = new MultiOpcode(0xDD);
		
		initOpcodeXY(dd,new IndexRegister()
		{
			public final void set(int value)
			{
				IX = value ;
			}

			public final int get()
			{
				return IX;
			}

			public String name()
			{
				return "IX";
			}
		});

		setOpcode(dd);
	}

	private void initOpcodes_CB()
	{
		MultiOpcode cb = new MultiOpcode(0xcb);

		for (int r = 0 ; r < 8 ; r++)
		{
			cb.setOpcode(new RLC_R(r));
			cb.setOpcode(new SRL_R(r));
			cb.setOpcode(new RR_R(r));
			
			cb.setOpcode(new AbstractOpcode(0x08|r,2,8,"RRC\t%1%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					setValueRRR(opcode,rrc(getValueRRR(opcode)));
					return 8;

				}
			});
			
			cb.setOpcode(new AbstractOpcode(0x10|r,2,8,"RL\t%1%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					setValueRRR(opcode,rl(getValueRRR(opcode)));
					return 8;

				}
			});

			cb.setOpcode(new AbstractOpcode(0x20|r,2,8,"SLA\t%1%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					setValueRRR(opcode,sla(getValueRRR(opcode)));
					return 8;

				}
			});
			
			cb.setOpcode(new AbstractOpcode(0x28|r,2,8,"SRA\t%1%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					setValueRRR(opcode,sra(getValueRRR(opcode)));
					return 8;

				}
			});

			cb.setOpcode(new AbstractOpcode(0x30|r,2,8,"SLL\t%1%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					setValueRRR(opcode,sll(getValueRRR(opcode)));
					return 8;

				}
			});
			
			

			
		}
		
		for (int b = 0 ; b < 8 ; b++)
			for (int r = 0 ; r < 8 ; r++)
			{
				cb.setOpcode(new BIT_B_R(b,r));
				cb.setOpcode(new RES_B_R(b,r));
				cb.setOpcode(new SET_B_R(b,r));

			}
		setOpcode(cb);
	}

	/**
	 * Initialize the Z80 opcodes
	 */
	protected void initOpcodes()
	{
		super.initOpcodes();

		setOpcode(new AbstractOpcode(0xd9,1,4,"EXX")
		{
			public int exec(int pc) throws SIMException
			{
				exx();

				return 4;
			}

		});

		setOpcode(new AbstractOpcode(0x08,1,4,"EX\tAF,AF'")
		{
			public int exec(int pc) throws SIMException
			{
				ex_af_af1();

				return 4;
			}

		});

		setOpcode(new JR(0x10,2,13,8,"DJNZ\t%byte")
		{
			public int exec(int pc) throws SIMException
			{
				int v = ((BC >>> 8) - 1) & 0xff;
				BC = (BC & 0xff) | (v << 8);
				
				if (v != 0)
					return jr(getByte(pc+1));
				else
					return 8;
			}

		});

		setOpcode(new JR(0x18,2,12,12,"JR\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				return jr(getByte(pc+1));
			}
		});
		
		setOpcode(new JR(0x28,2,12,7,"JR\tZ,%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (FLAG_Z())
					return jr(getByte(pc+1));
				else
					return 7;
			}
		});

		setOpcode(new JR(0x20,2,12,7,"JR\tNZ,%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (FLAG_Z())
					return 7;
				else
					return jr(getByte(pc+1));
			}
		});

		setOpcode(new JR(0x30,2,12,7,"JR\tNC,%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (FLAG_C())
					return 7;
				else
					return jr(getByte(pc+1));
			}
		});
		
		setOpcode(new JR(0x38,2,12,7,"JR\tC,%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (FLAG_C())
					return jr(getByte(pc+1));
				else
					return 7;
			}
		});

		initOpcodes_CB();
		initOpcodes_ED();
		initOpcodes_FD();
		initOpcodes_DD();
		
	}
	
}


