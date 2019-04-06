/**
   $Id: M6502.java 946 2012-12-02 11:01:18Z mviara $

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
package jmce.mos;

import java.io.*;
import jmce.sim.*;
import jmce.sim.cpu.*;
import jmce.util.Hex;
import jmce.sim.memory.PlainMemory;

/**
 * MOS 6502
 *
 * <p>
 * Emulated register : A,X,Y,P,S,PC
 * <p>
 * The 6502 have one one memory used as program data and i/o space.
 * <p>
 * <h2>Lmitations</h2>
 * <p>
 * <ul>
 *  <li>The 6502 can operate on binary or bcd numbers depending on the
 * bit 3 of the flag, this version support only the binary mode.</li>
 *  <li>Only offical and documented opcodes are implemented</li>
 * </ul>
 * 
 * <p>
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public class M6502 extends AbstractCPU implements M65XXConstants
{
	
	/** CPU register */
	int 	A,X,Y,P,S,PC;

	/**
	 * Operand with all type of addressing
	 */
	OperandGeneric ac   = new OperandA();
	OperandGeneric zp   = new OperandZeroPage();
	OperandGeneric zpi  = new OperandZeroPageIndirect();
	OperandGeneric zpix = new OperandZeroPageIndexIndirectX();
	OperandGeneric zpiy = new OperandZeroPageIndexIndirectY();
	OperandGeneric zpx  = new OperandZeroPageIndexedX();
	OperandGeneric zpy  = new OperandZeroPageIndexedY();
	OperandGeneric im   = new OperandImmediate();
	OperandGeneric absx = new OperandAbsoluteIndexedX();
	OperandGeneric absy = new OperandAbsoluteIndexedY();
	OperandGeneric abs  = new OperandAbsolute();

	/**
	 * Inner class to manage one opcode executer.
	 */
	abstract class  OpcodeExecuter
	{
		private String name;
		private int baseTime;
		private int opcode;
		
		public OpcodeExecuter(String name,int baseTime)
		{
			this(0,name,baseTime);
		}
		
		public OpcodeExecuter(int opcode,String name,int baseTime)
		{
			this.opcode = opcode;
			this.name = name;
			this.baseTime = baseTime;
		}

		public int getOpcode()
		{
			return opcode;
		}
		
		public int getBaseTime()
		{
			return baseTime;
		}


		public String getOpcodeName()
		{
			return name;
		}
		
		abstract public void exec(OperandGeneric ge,int add) throws SIMException;
		
	}

	/**
	 * Abstract inner class to rappresent one operand.
	 */
	abstract class OperandGeneric 
	{
		private int length,addTimes;
		private String operand;
		private long counter = 0;
		
		public OperandGeneric(String operand,int length,int addTimes)
		{
			this.length = length;
			this.addTimes = addTimes;
			this.operand = operand;
		}
		

		public int getAddTime()
		{
			return addTimes;
		}

		public int getLength()
		{
			return length;
		}
		
		abstract int getAddress(int pc) throws SIMException;

		public int getValue(int pc) throws SIMException
		{
			return getByte(getAddress(pc));
		}

		public void setValue(int pc,int value) throws SIMException
		{
			setByte(getAddress(pc),value);
		}

		public String getOperandName()
		{
			return operand;
		}
		
		public final void incCounter()
		{
			counter++;
		}
		
		public long getCounter()
		{
			return counter;
		}

		public void resetCounter()
		{
			counter = 0;
		}
	}

	/**
	 * The operand is one immediate byte after the opcode.
	 */
	class OperandImmediate extends OperandGeneric
	{
		OperandImmediate()
		{
			super("#%byte",2,0);
		}

		public int getAddress(int pc) throws SIMException
		{
			throw new SIMException("Invalid address mode immediate");
		}


		public int getValue(int pc) throws SIMException
		{
			return getByte(pc+1);
		}
		
	}

	/**
	 * The operand is the accumulator
	 */
	class OperandA extends OperandGeneric
	{
		OperandA()
		{
			super("A",1,0);
			
		}
		
		public int getAddress(int pc) throws SIMException
		{
			throw new SIMException("Invalid address mode A");
		}

		public void setValue(int pc,int value) throws SIMException
		{
			setAcc(value);
		}

		public int getValue(int pc) throws SIMException
		{
			return A;
		}
				
	}

	/**
	 * Operand is in the zero page indirect
	 */
	class OperandZeroPageIndirect extends OperandGeneric
	{
		OperandZeroPageIndirect()
		{
			super("(%byte)",2,3);
		}

		public final int getAddress(int pc) throws SIMException
		{
			return getWord(getByte(pc+1));
		}


	}
	
	
	/**
	 * The second byte of the instruction is the address in the
	 * zero page of the operand. [zp]
	 */
	class OperandZeroPage extends OperandGeneric
	{
		OperandZeroPage()
		{
			super("%byte",2,1);
		}

		public final int getAddress(int pc) throws SIMException
		{
			return getByte(pc+1);
		}

	}

	
	/**
	 * Zero page indexed indirect X. The addres of the operand is
	 * is read from the zero page at zp+X.
	 * 
	 *  [(zp,x)]
	 **/
	class OperandZeroPageIndexIndirectX extends OperandGeneric
	{
		OperandZeroPageIndexIndirectX()
		{
			super("(%byte,X)",2,4);
		}

		public final int getAddress(int pc) throws SIMException
		{
			int a = (getByte(pc+1) + X) & 0xff;
			return  getWord(a);
		}

	}


	/**
	 * Absolute addressing the absolute address is the next 16 bit
	 * after the opcode.
	 */
	class OperandAbsolute extends OperandGeneric
	{
		OperandAbsolute()
		{
			super("%word",3,2);
		}

		public final int getAddress(int pc) throws SIMException
		{
			return getWord(pc+1);
		}

	}

	/**
	 * The address is the next 16 bit after the opcode plus the X
	 * register [a,x]
	 */
	class OperandAbsoluteIndexedX extends OperandGeneric
	{
		OperandAbsoluteIndexedX()
		{
			super("%word,X",3,3);
		}

		public final int getAddress(int pc) throws SIMException
		{
			int a = getWord(pc+1);
			return (a + X) & 0xffff;
		}

	}

	/**
	 * The address is the next 16 bit after the opcode plus the Y
	 * register [a,y]
	 */
	class OperandAbsoluteIndexedY extends OperandGeneric
	{
		OperandAbsoluteIndexedY()
		{
			super("%word,Y",3,3);
		}

		public final int getAddress(int pc) throws SIMException
		{
			int a = getWord(pc+1);
			return  (a + Y) & 0xffff;
		}

	}


	/** [zp,x] */
	class OperandZeroPageIndexedX extends OperandGeneric
	{
		OperandZeroPageIndexedX()
		{
			super("%byte,X",2,3);
		}

		public final int getAddress(int pc) throws SIMException
		{
			return  (getByte(pc+1) + X) & 0xff; 
		}

	}

	/** [zp,y] */
	class OperandZeroPageIndexedY extends OperandGeneric
	{
		OperandZeroPageIndexedY()
		{
			super("%byte,Y",2,3);
		}

		public final int getAddress(int pc) throws SIMException
		{
			return  (getByte(pc+1) + Y) & 0xff; 
		}

	}

	/* [(zp),y]
	 * 
	 * The address of the operand is read from zero page then the Y
	 * register is added.
	 */
	class OperandZeroPageIndexIndirectY extends OperandGeneric
	{
		OperandZeroPageIndexIndirectY()
		{
			super("(%byte),Y",2,5);
		}

		public final int getAddress(int pc) throws SIMException
		{ 
			int a = getByte(pc+1);
			a = getWord(a);
			return  (a + Y) & 0xffff;
		}

	}


	/**
	 * Abstract class to manage jump relative opcodes.
	 */
	abstract class BR extends AbstractOpcode
	{
		private int tjmp;

		BR(int opcode,int l,int tjmp,int tnojmp,String s)
		{
			super(opcode,l,tnojmp,s);
			this.tjmp = tjmp;

		}

		protected final int jr(int pc,int offset)
		{
			PC = addOffset(PC,offset);

			return tjmp;
		}

	}

	
	public M6502()
	{
		super("M6502");
		
		setEndian(LITTLE_ENDIAN);
		setClock(1000000);
		setClockPerCycle(1);

	}



	/**
	 * Convert one irq number to the memory address.
	 *
	 * @param  irq Interrupt request number.
	 * 
	 * @return The address of the IRQ.
	 */
	public int vectorToAddress(int irq) throws SIMException
	{
		return getWord(0xfffe - irq * 2);
	}

	public void reset() throws SIMException
	{
		super.reset();
		pc(vectorToAddress(RESET_VECTOR));
	}
	

	public final void setWord(int a,int v) throws SIMException
	{
		memory.setMemory(a+0,v & 0xff);
		memory.setMemory(a+1,v >>> 8);
	}

	public final int getWord(int a) throws SIMException
	{
		return memory.getMemory(a+0) | (memory.getMemory(a+1) << 8);
	}

	protected void initRegisters()
	{
		/**  Reset address must be set at runtime */
		addRegister(new StandardRegister("PC",Register.FAMILY_PC,16,0)
		{
			public int getRegister()
			{
				return PC;
			}

			public void setRegister(int value)
			{
				PC = value;
			}
		});
		
		addRegister(new StandardRegister("S",Register.FAMILY_SP,8)
		{
			public int getRegister()
			{
				return S;
			}

			public void setRegister(int value)
			{
				S = value;
			}
			
		});
		
		addRegister(new StandardRegister("P",Register.FAMILY_PSW,8)
		{
			public String descValue()
			{
				String s = "";

				s += "["+Hex.formatByte(P)+"]";

				if (P(P_S))
					s += "S";
				else
					s += "-";

				if (P(P_V))
					s += "V";
				else
					s += "-";

				if (P(P_E))
					s += "E";
				else
					s += "-";

				if (P(P_B))
					s += "B";
				else
					s += "-";

				if (P(P_D))
					s += "D";
				else
					s += "-";
				
				if (P(P_I))
					s += "I";
				else
					s += "-";

				if (P(P_Z))
					s += "Z";
				else
					s += "-";

				if (P(P_C))
					s += "C";
				else
					s += "-";

				return s;
			}

			public int getRegister()
			{
				return P;
			}

			public void setRegister(int value)
			{
				P = value;
			}
			
		});
		
		addRegister(new StandardRegister("A",Register.FAMILY_ACC,8)
		{
			public int getRegister()
			{
				return A;
			}

			public void setRegister(int value)
			{
				A = value;
			}
			
		});
		
		addRegister(new StandardRegister("X",Register.FAMILY_INDEX,8)
		{
			public int getRegister()
			{
				return X;
			}

			public void setRegister(int value)
			{
				X = value;
			}

		});
		
		addRegister(new StandardRegister("Y",Register.FAMILY_INDEX,8)
		{
			public int getRegister()
			{
				return Y;
			}

			public void setRegister(int value)
			{
				Y = value;
			}

		});

	}

	
	private void initOpcodes()
	{
		OpcodeExecuter exec;
		
		/** INC */
		exec = new OpcodeExecuter("INC",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				ge.setValue(pc,inc8(ge.getValue(pc)));
			}
		};

		setOpcode(0xe6,exec,zp);
		setOpcode(0xF6,exec,zpx);
		setOpcode(0xEE,exec,abs);
		setOpcode(0xFE,exec,absx);

		/** STY */
		exec = new OpcodeExecuter("STY",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				ge.setValue(pc,Y);
			}
		};


		setOpcode(0x84,exec,zp);
		setOpcode(0x94,exec,zpx);
		setOpcode(0x8c,exec,abs);
		
		/** STX */
		exec = new OpcodeExecuter("STX",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				ge.setValue(pc,X);
			}

		};

		setOpcode(0x86,exec,zp);
		setOpcode(0x96,exec,zpy);
		setOpcode(0x8e,exec,abs);
		
		/** LDX */
		exec = new OpcodeExecuter("LDX",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				X = ge.getValue(pc);
				setFlagZS(X);
			}

		};

		setOpcode(0xa2,exec,im);
		setOpcode(0xa6,exec,zp);
		setOpcode(0xb6,exec,zpy);
		setOpcode(0xae,exec,abs);
		setOpcode(0xbe,exec,absy);
		
		/** STA */
		exec = new OpcodeExecuter("STA",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				ge.setValue(pc,A);

			}
			
		};

		setOpcode(0x85,exec,zp);
		setOpcode(0x95,exec,zpx);
		setOpcode(0x8d,exec,abs);
		setOpcode(0x9d,exec,absx);
		setOpcode(0x99,exec,absy);
		setOpcode(0x81,exec,zpix);
		setOpcode(0x91,exec,zpiy);

		/** LDY */
		exec = new OpcodeExecuter("LDY",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				Y = ge.getValue(pc);
				setFlagZS(Y);
			}
		};

		setOpcode(0xa0,exec,im);
		setOpcode(0xA4,exec,zp);
		setOpcode(0xb4,exec,zpx);
		setOpcode(0xac,exec,abs);
		setOpcode(0xbc,exec,absx);

		/** BIT */
		exec = new OpcodeExecuter("BIT",3)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int v = ge.getValue(pc);
				P(P_S,(v & 0x80) != 0);
				P(P_V,(v & 0x40) != 0);
				v &= A;
				P(P_Z,v == 0);

			}
		};

		setOpcode(0x24,exec,zp);
		setOpcode(0x2c,exec,abs);
		
		/** ROR */
		exec = new OpcodeExecuter("ROR",1)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int v = ge.getValue(pc);
				boolean c = (v & 0x01) != 0;

				v >>>= 1;

				if (P(P_C))
					v |= 0x80;
				P(P_C,c);
				ge.setValue(pc,v);
				setFlagZS(v);


			}
		};

		setOpcode(0x6a,exec,ac);
		setOpcode(0x66,exec,zp);
		setOpcode(0x76,exec,zpx);
		setOpcode(0x6e,exec,abs);
		setOpcode(0x7e,exec,absx);

		/** ROL */
		exec = new OpcodeExecuter("ROL",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int v = ge.getValue(pc);
				boolean c = (v & 0x80) != 0;

				v <<= 1;

				if (P(P_C))
					v |= 0x01;
				v &= 0xff;
				P(P_C,c);
				ge.setValue(pc,v);
				setFlagZS(v);

			}
		};

		setOpcode(0x2a,exec,ac);
		setOpcode(0x26,exec,zp);
		setOpcode(0x36,exec,zpx);
		setOpcode(0x2e,exec,abs);
		setOpcode(0x3e,exec,absx);

		/** CPY */
		exec = new OpcodeExecuter("CPY",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				cmp8(Y,ge.getValue(pc));

			}
		};

		setOpcode(0xc0,exec,im);
		setOpcode(0xc4,exec,zp);
		setOpcode(0xcc,exec,abs);

		/** CPX */
		exec = new OpcodeExecuter("CPX",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				cmp8(X,ge.getValue(pc));

			}
		};

		setOpcode(0xe0,exec,im);
		setOpcode(0xe4,exec,zp);
		setOpcode(0xec,exec,abs);

		/** ASL */
		exec = new OpcodeExecuter("ASL",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int value = ge.getValue(pc) << 1;
				P(P_C,(value & 0x100) != 0);
				value &= 0xff;
				ge.setValue(pc,value);
				setFlagZS(value);
			}

		};

		
		setOpcode(0x0a,exec,ac);
		setOpcode(0x06,exec,zp);
		setOpcode(0x16,exec,zpx);
		setOpcode(0x0e,exec,abs);
		setOpcode(0x1e,exec,absx);
		

		/** LSR */
		exec = new OpcodeExecuter("LSR",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int value = ge.getValue(pc);
				P(P_C,(value & 0x01) != 0);
				value >>>= 1;
				ge.setValue(pc,value);
				setFlagZS(value);
			}

		};


		setOpcode(0x4a,exec,ac);
		setOpcode(0x46,exec,zp);
		setOpcode(0x56,exec,zpx);
		setOpcode(0x4e,exec,abs);
		setOpcode(0x5e,exec,absx);

		/** DEC */
		exec = new OpcodeExecuter("DEC",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int value = ge.getValue(pc);
				value = dec8(value);
				ge.setValue(pc,value);
			}

		};

		setOpcode(0xc6,exec,zp);
		setOpcode(0xd6,exec,zpx);
		setOpcode(0xce,exec,abs);
		setOpcode(0xde,exec,absx);

		setOpcodeAll(new OpcodeExecuter(0x61,"ADC",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int v = ge.getValue(pc);
				int a = A + v;
				if (P(P_C))
					a += 1;
				P(P_C,(a > 0xff));
				
				/** AS in Z80 */
				boolean o = ((A ^ v ^ 0x80) & (v ^ a) & 0x80) != 0;
				P(P_V,o);
				setAcc(a);
			}
		});

		setOpcodeAll(new OpcodeExecuter(0xE1,"SBC",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				int c = P(P_C) ? 0 : 1;
				int v = ge.getValue(pc);
				int a = A - v - c;
				P(P_C,(a & 0x100) == 0);
				
				/** AS in Z80 */
				boolean o = ((v ^ A) & (A ^ a) & 0x80) != 0;
				P(P_V,o);
				setAcc(a);
			}
		});


		setOpcodeAll(new OpcodeExecuter(0x41,"EOR",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				setAcc(A ^ ge.getValue(pc));

			}
		});

		setOpcodeAll(new OpcodeExecuter(0x01,"ORA",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				setAcc(A | ge.getValue(pc));
			}

		});

		setOpcodeAll(new OpcodeExecuter(0x21,"AND",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				
				setAcc(A & ge.getValue(pc));

			}
		});

		setOpcodeAll(new  OpcodeExecuter(0xa1,"LDA",2)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				setAcc(ge.getValue(pc));

			}
		});


		/** CMP */
		setOpcodeAll(new OpcodeExecuter(0xc1,"CMP",3)
		{
			public void exec(OperandGeneric ge,int pc) throws SIMException
			{
				cmp8(A,ge.getValue(pc));

			}
		});


		setOpcode(new AbstractOpcode(0x60,1,6,"RTS")
		{
			public int exec(int pc) throws SIMException
			{
				PC = (pop16()+1) & 0xffff;

				return 6;
			}

		});

		
		setOpcode(new AbstractOpcode(0x40,1,6,"RTI")
		{
			public int exec(int pc) throws SIMException
			{
				/** Pop flag and ignore B bit */
				P = pop() & ~P_B;
				PC = pop16();
				
				
				return 6;
			}

		});

		setOpcode(new AbstractOpcode(0xE8,1,2,"INX")
		{
			public int exec(int pc) throws SIMException
			{
				X = inc8(X);
				return 2;
			}

		});

		setOpcode(new AbstractOpcode(0xC8,1,2,"INY")
		{
			public int exec(int pc) throws SIMException
			{
				Y = inc8(Y);
				return 2;
			}

		});

		setOpcode(new AbstractOpcode(0xCA,1,2,"DEX")
		{
			public int exec(int pc) throws SIMException
			{
				X = dec8(X);
				return 2;
			}

		});

		setOpcode(new AbstractOpcode(0x88,1,2,"DEY")
		{
			public int exec(int pc) throws SIMException
			{
				Y = dec8(Y);
				return 2;
			}

		});

		setOpcode(new BR(0x30,2,3,2,"BMI\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (P(P_S))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});

		setOpcode(new BR(0x50,2,3,2,"BVC\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (!P(P_V))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});
		
		setOpcode(new BR(0x70,2,3,2,"BVS\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (P(P_V))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});

		setOpcode(new BR(0x10,2,3,2,"BPL\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (!P(P_S))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});



		setOpcode(new BR(0xd0,2,3,2,"BNE\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (!P(P_Z))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});

		setOpcode(new BR(0xf0,2,3,2,"BEQ\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (P(P_Z))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});

		setOpcode(new BR(0xB0,2,3,2,"BCS\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (P(P_C))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});


		setOpcode(new BR(0x90,2,3,2,"BCC\t%offset")
		{
			public int exec(int pc) throws SIMException
			{
				if (!P(P_C))
					return jr(pc,getByte(pc+1));
				else
					return 2;
			}
		});

		setOpcode(new AbstractOpcode(0x00,2,7,"BRK")
		{
			public int exec(int pc) throws SIMException
			{
				push16(PC);
				push(P|P_B);

				P |= P_I;
				
				pc(vectorToAddress(BRK_VECTOR));

				return getTimes();
			}
			
		});

		
		setOpcode(new AbstractOpcode(0xea,1,2,"NOP")
		{
			public int exec(int pc)
			{
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x4c,3,6,"JMP\t%word")
		{
			public int exec(int pc) throws SIMException
			{
				PC = getWord(pc+1);
				return 6;
			}
		});

		setOpcode(new AbstractOpcode(0x6c,3,9,"JMP\t(%word)")
		{
			public int exec(int pc) throws SIMException
			{
				int add = getWord(pc+1);
				
				PC = getWord(add);
				
				return 9;
			}
		});

		setOpcode(new AbstractOpcode(0x20,3,6,"JSR\t%word")
		{
			public int exec(int pc) throws SIMException
			{
				push16(PC-1);
				PC = getWord(pc+1);
				return 6;
			}
		});

		setOpcode(new AbstractOpcode(0x8a,1,2,"TXA")
		{
			public int exec(int pc) throws SIMException
			{
				setAcc(X);
			
				return 2;
			}

		});

		setOpcode(new AbstractOpcode(0x98,1,2,"TYA")
		{
			public int exec(int pc) throws SIMException
			{
				setAcc(Y);

				return 2;
			}

		});

		
		setOpcode(new AbstractOpcode(0xaa,1,2,"TAX")
		{
			public int exec(int pc) throws SIMException
			{
				X = A;
				setFlagZS(X);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0xa8,1,2,"TAY")
		{
			public int exec(int pc) throws SIMException
			{
				Y = A;
				setFlagZS(Y);
				return 2;
			}

		});

		setOpcode(new AbstractOpcode(0x9a,1,2,"TXS")
		{
			public int exec(int pc) throws SIMException
			{
				S = X;
				return getTimes();
			}

		});

		
		setOpcode(new AbstractOpcode(0xba,1,2,"TSX")
		{
			public int exec(int pc) throws SIMException
			{
				X = S;
				setFlagZS(X);
				return getTimes();
			}

		});
		

		setOpcode(new AbstractOpcode(0x48,1,3,"PHA")
		{
			public int exec(int pc) throws SIMException
			{
				push(A);
				return getTimes();
			}
 
		});

		setOpcode(new AbstractOpcode(0x68,1,4,"PLA")
		{
			public int exec(int pc) throws SIMException
			{
				setAcc(pop());
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x08,1,3,"PHP")
		{
			public int exec(int pc) throws SIMException
			{ 
				push(P);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x28,1,4,"PLP")
		{
			public int exec(int pc) throws SIMException
			{
				P = pop();
				if (P(P_D))
					throw new SIMException("Decimal mode not supported");
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x18,1,2,"CLC")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_C,false);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x38,1,2,"SEC")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_C,true);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x58,1,2,"CLI")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_I,false);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0x78,1,2,"SEI")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_I,true);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0xb8,1,2,"CLV")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_V,false);
				return getTimes();
			}

		});
		
		setOpcode(new AbstractOpcode(0xd8,1,2,"CLD")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_D,false);
				return getTimes();
			}

		});

		setOpcode(new AbstractOpcode(0xf8,1,2,"SED")
		{
			public int exec(int pc) throws SIMException
			{
				P(P_D,true);
				
				throw new SIMException("Decimal mode not supported");

				//return getTimes();
			}

		});

	}


	private void setOpcode(int opcode,OpcodeExecuter exec,OperandGeneric ge)
	{
		class Opcode6502 extends AbstractOpcode
		{
			OpcodeExecuter exec;
			OperandGeneric ge;
			
			Opcode6502(int opcode,OpcodeExecuter _exec,OperandGeneric _ge)
			{
				super(opcode,_ge.getLength(),_exec.getBaseTime()+_ge.getAddTime(),_exec.getOpcodeName()+"\t"+_ge.getOperandName());
				exec=_exec;
				ge = _ge;
				if (ge.getLength() == 0)
				{
					System.out.println(getDescription());
					System.exit(0);
				}
			}

			public int exec(int pc) throws SIMException
			{
				ge.incCounter();
				exec.exec(ge,pc);

				return getTimes();
			}

		}
		setOpcode(new Opcode6502(opcode,exec,ge));
	}

	/**
	 * Set the operand for opcode with all type of addressing.
	 */
	private void setOpcodeAll(OpcodeExecuter exec)
	{
		setOpcode(exec.getOpcode()+0x00,exec,zpix);
		setOpcode(exec.getOpcode()+0x04,exec,zp);
		setOpcode(exec.getOpcode()+0x08,exec,im);
		setOpcode(exec.getOpcode()+0x0c,exec,abs);
		setOpcode(exec.getOpcode()+0x11,exec,zpi);
		setOpcode(exec.getOpcode()+0x10,exec,zpiy);
		setOpcode(exec.getOpcode()+0x14,exec,zpx);
		setOpcode(exec.getOpcode()+0x1c,exec,absx);
		setOpcode(exec.getOpcode()+0x18,exec,absy);
	}
	
	public void init(Hardware parent) throws SIMException
	{
		initMemories();
		initPeripherals();
		initRegisters();
		initOpcodes();

		super.init(parent);
	}

	/**
	 * No peripherals are build in on 6502 this function is
	 * present only for compatibility
	 **/
	protected void initPeripherals() throws SIMException
	{
	}
	
	protected void initMemories() throws SIMException
	{
		Memory m = getMemoryForName(MAIN_MEMORY);

		if (m == null)
			addHardware(m = new PlainMemory(MAIN_MEMORY,0x10000));

		setMemory(m);
		setIO(m);
		
		
	}

	/**
	 * Set a bit of the flag.
	 */
	private final void P(int mask,boolean mode) throws SIMException
	{
		if (mode)
			P |=  mask;
		else
			P &= ~mask;
	}

	/**
	 * Return a bit of the flag.
	 */
	private final boolean P(int mask)
	{
		return (P & mask) != 0;
	}



	/**
	 * Push a word (16 bit) on the stack
	 */
	protected void push16(int value) throws SIMException
	{
		push(value >>> 8);
		push(value);
	}

	/**
	 * Pop a word (16 bit) from the stack
	 */
	protected int pop16() throws SIMException
	{
		int value = pop();
		value |= pop() << 8;

		return value;
	}
	
	/**
	 * Push a 8 bit value on the stack.
	 */
	protected void push(int value) throws SIMException
	{
		setByte(S | S_PAGE,value);
		S = (S - 1) & 0xff;
		
	}

	/**
	 * Pop a 8 bit value from the stack.
	 */
	protected int pop() throws SIMException
	{
		S = (S +1) & 0xff;
		int result = getByte(S | S_PAGE);

		return result;
	}
	
	public void pc(int pc) throws SIMException
	{
		PC = pc;
	}

	/**
	 * Set the value of accumulator and update the S and Z flag.
	 */
	private void setAcc(int a) throws SIMException
	{
		a &= 0xff;
		A = a;
		setFlagZS(A);
	}
	

	/**
	 * Set the Z and S flag according to the argument.
	 *
	 * @param value - Value to check.
	 */
	private void setFlagZS(int value) throws SIMException
	{
		value &= 0xff;
		P(P_Z,value == 0);
		P(P_S,(value & 0x80) != 0);
	}

	/**
	 * 8 bit increment.
	 */
	private final int inc8(int v) throws SIMException
	{
		v = (v + 1) & 0xff;
		P(P_Z,v == 0);
		P(P_S,(v & 0x80) != 0);

		return v;
	}

	/**
	 * 8 bit compare.
	 */
	private final void cmp8(int r,int v) throws SIMException
	{
		P(P_C,r >= v);
		r -= v;
		setFlagZS(r & 0xff);
	}

	/**
	 * 8 bit decrement.
	 */
	private final int dec8(int v) throws SIMException
	{
		v = (v - 1) & 0xff;
		P(P_Z,v == 0);
		P(P_S,(v & 0x80) != 0);
		
		return v;
	}

	
	public int pc() throws SIMException
	{
		return PC;
	}

	public int fireISR(Interrupt irq) throws SIMException
	{
		push16(PC);
		push(P);

		P |= P_I;

		PC = vectorToAddress(irq.getVector());

		return 7;
	}

	public boolean isInterruptEnabled()
	{
		return (P & P_I) == 0;
	}


	
	private void dumpOperand(PrintStream ps,OperandGeneric o)
	{
		dumpValue(ps,o.getCounter(),o.getOperandName());
	}
	
	public void dumpStatistics(PrintStream ps) 
	{
		super.dumpStatistics(ps);
		
		dumpTitle(ps,"Execution#","Addressing mode");
		dumpOperand(ps, ac);
		dumpOperand(ps, zp);
		dumpOperand(ps, zpi);
		dumpOperand(ps, zpix);
		dumpOperand(ps, zpiy);
		dumpOperand(ps, zpx);
		dumpOperand(ps, zpy);
		dumpOperand(ps, im);
		dumpOperand(ps, absx);
		dumpOperand(ps, absy);
		dumpOperand(ps, abs);

	}
}
