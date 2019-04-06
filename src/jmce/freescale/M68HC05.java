/**
   $Id: M68HC05.java 596 2011-05-24 07:12:27Z mviara $

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

import jmce.sim.*;
import jmce.sim.cpu.*;
import jmce.util.Hex;
import jmce.sim.memory.PlainMemory;

/**
 * Motorola / Freescale M68HC05 family.<p>
 * <p>
 * Coded using M68HC05TB.PDF
 *
 * Implemented registers :
 * 
 * <ul>
 *  <li>A 8 bit Accumulator.</li>
 *  <li>X 8 bit index register.</li>
 *  <li>SP 16 bit stack pointer.</li>
 *  <li>PC 16 bit program counter.</li>
 *  <li>CCR 8 bit flag register.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @since 1.02
 */
public class M68HC05 extends AbstractCPU implements M68HC05Constants
{
	/** CPU register */
	int	A,X,SP,CCR,PC;

	/** Address of reset vector (default to invalid address) */
	protected int resetVector = 0x10000;
	
	/**
	 * Constructor with name
	 */
	protected M68HC05(String name)
	{
		super(name);
		setResetVector(M68HC05_RESET_VECTOR);
		setEndian(LITTLE_ENDIAN);
		setClockPerCycle(1);
		setClock(2100000);
	}
	
	/**
	 * Default constructor.
	 */
	public M68HC05()
	{
		this("M68HC05");
	}

	protected void initRegisters()
	{
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

		addRegister(new StandardRegister("SP",Register.FAMILY_SP,16)
		{
			public int getRegister()
			{
				return SP;
			}

			public void setRegister(int value)
			{
				SP = value;
			}

		});

		addRegister(new StandardRegister("CCR",Register.FAMILY_PSW,8)
		{
			public String descValue()
			{
				String s = "";

				s += "["+Hex.formatByte(CCR)+"]";

				s+= "111";
				
				if (ccr(CCR_H))
					s += "H";
				else
					s += "-";

				if (ccr(CCR_I))
					s += "I";
				else
					s += "-";

				if (ccr(CCR_N))
					s += "N";
				else
					s += "-";

				if (ccr(CCR_Z))
					s += "Z";
				else
					s += "-";

				if (ccr(CCR_C))
					s += "C";
				else
					s += "-";

				return s;
			}

			public int getRegister()
			{
				return CCR;
			}

			public void setRegister(int value)
			{
				CCR = value;
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

	}

	/**
	 * Push a 8 bit value on the stack
	 */
	protected void push(int value) throws SIMException
	{
		setByte(SP,value);
		SP = (SP - 1) & 0xffff;
	}

	/**
	 * Pop 8 bit from the stack
	 */
	protected int pop() throws SIMException
	{
		SP = (SP + 1) & 0xffff;
		return getByte(SP);
	}

	/**
	 * Push a 16 bit on the stack.
	 */
	protected void push16(int value) throws SIMException
	{
		push(value);
		push(value >>> 8);
	}

	/**
	 * Pop a 16 bit value from the stack.
	 */
	protected int pop16() throws SIMException
	{
		int result = pop() << 8;
		return result | pop();
	}


	public final void setWord(int a,int v) throws SIMException
	{
		setByte(a+1,v & 0xff);
		setByte(a+0,v >>> 8);
	}

	public void reset() throws SIMException
	{
		super.reset();
		PC = getWord(resetVector);
	}

	public int fireISR(Interrupt irq) throws SIMException
	{
		push(CCR);
		push(A);
		push(X);
		push16(PC);
		CCR |= CCR_I;
		PC = getWord(irq.getVector());

		return 10;
	}
	
	public final int pc() throws SIMException
	{
		return PC;
	}

	public final void pc(int pc) throws SIMException
	{
		this.PC = pc;
	}

	public final boolean isInterruptEnabled()
	{
		return (CCR & CCR_I) == 0;
	}

	public final int getWord(int a) throws SIMException
	{
		return getByte(a+1) | (getByte(a+0) << 8);
	}

	/**
	 * Check a bit of the CCR
	 */
	private final boolean ccr(int mask)
	{
		return (CCR & mask) != 0 ? true : false;
	}

	/**
	* Set/clear bit in the CCR register
	*/
	protected final void ccr(int mask,boolean mode)
	{
		if (mode)
			CCR |= mask;
		else
			CCR &= ~mask;
	}

	/**
	 * 8 bit xor
	 */
	private final int xor8(int value) throws SIMException
	{
		int result  = A ^ value;
		ccr(CCR_N,bit7(result));
		ccr(CCR_Z,result == 0);

		return result;
		
	}

	/**
	 * 8 bit load
	 */
	private final int ld8(int value) throws SIMException
	{
		ccr(CCR_N,bit7(value));
		ccr(CCR_Z,value == 0);

		return value;
	}
	
	/**
	 * 8 bit And
	 */
	private final int and8(int value) throws SIMException
	{
		int result  = A & value;
		ccr(CCR_N,bit7(result));
		ccr(CCR_Z,result == 0);

		return result;
	}

	/**
	 * 8 bit or
	 */
	private final int or8(int value) throws SIMException
	{
		int result  = A | value;
		ccr(CCR_N,bit7(result));
		ccr(CCR_Z,result == 0);

		return result;
	}

	/**
	 * Add 8 with carry
	 */
	private final int adc8(int value) throws SIMException
	{
		int v = value + A;
		
		if (ccr(CCR_C))
			v += 1;
		
		ccr(CCR_C,v > 0xff);
		ccr(CCR_H,(v & 0x10) != 0);
		ccr(CCR_N,(v & 0x80) != 0);
		ccr(CCR_Z,(v & 0xff) == 0);
		return value; // fixme
	}

	/*
	 * sub 8 with carry
	 */
	private final int sbc8(int value) throws SIMException
	{
		int v = A - value;

		if (ccr(CCR_C))
			v -= 1;

		ccr(CCR_C,v > 0xff);
		ccr(CCR_N,(v & 0x80) != 0);
		ccr(CCR_Z,(v & 0xff) == 0);
		
		return value; // fixme
	}

	/*
	 * Sub 8
	 */
	private final int sub8(int value) throws SIMException
	{
		int v = A - value;


		ccr(CCR_C,v > 0xff);
		ccr(CCR_N,(v & 0x80) != 0);
		ccr(CCR_Z,(v & 0xff) == 0);

		return value; // fixme
	}

	/**
	 * 8 bit addition
	 */
	private final int add8(int value) throws SIMException
	{
		ccr(CCR_C,false);
		return adc8(value);
	}

	/**
	 * 8 bit compare
	 */
	private final void cmp8(int v1,int v2) throws SIMException
	{
		int r = v1 - v2;

		ccr(CCR_Z,r == 0);
		ccr(CCR_N,(r & 0x80) != 0);
		ccr(CCR_C,v2 > v1);
	}
	
				
	protected void initMemories() throws SIMException
	{
		Memory m = getMemoryForName(MAIN_MEMORY);

		if (m == null)
			addHardware(m = new PlainMemory(MAIN_MEMORY,0x10000));

		/** Main and I/O memory are the same */
		setMemory(m);
		setIO(m);


	}

	/**
	 * No peripherals are build in on 6805 this function is
	 * present only for compatibility
	 **/
	protected void initPeripherals() throws SIMException
	{
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
	 * Class to rappresent one addressing mode
	 */
	abstract class AddressingMode
	{
		private int length,times,offset;
		private String desc;
		
		AddressingMode(int length,int times,int offset,String desc)
		{
			this.length = length;
			this.times = times;
			this.desc = desc;
			this.offset = offset;
		}

		public final int getOffset()
		{
			return offset;
		}
		
		public final String getDesc()
		{
			return desc;
		}
		
		public final int getLength()
		{
			return length;
		}

		public final int getTimes()
		{
			return times;
		}
		
		public void setMemory(int pc,int value) throws SIMException
		{
			setByte(getAddress(pc),value);
		}
		
		public int getMemory(int pc) throws SIMException
		{
			return getByte(getAddress(pc));
		}
		
		public abstract int getAddress(int pc) throws SIMException;
	}

	/**
	 * A addressiong mode
	 */
	AddressingMode addressA = new AddressingMode(1,-1,0x20,"\tA")
	{
		public int getAddress(int pc) throws SIMException
		{
			throw new SIMSWException("A have ho address");
		}

		public void setMemory(int pc,int value) throws SIMException
		{
			A = value;
		}

		public int getMemory(int pc) throws SIMException
		{
			return A;
		}

	};
	

	/**
	 * X addressiong mode
	 */
	AddressingMode addressX = new AddressingMode(1,-1,0x30,"X")
	{
		public int getAddress(int pc) throws SIMException
		{
			throw new SIMSWException("X have ho address");
		}

		public void setMemory(int pc,int value) throws SIMException
		{
			X = value;
		}

		public int getMemory(int pc) throws SIMException
		{
			return X;
		}

	};


	/**
	 * Immediate address mode
	 */
	AddressingMode immediate = new AddressingMode(2,0,0,"\t#%byte")
	{
		
		public void setMemory(int pc,int value) throws SIMException
		{
			throw new SIMSWException("Immediate operand cannoyt be set");
		}
		

		public int getAddress(int pc)
		{
			return pc+1;
		}
		
	};

	/**
	 * Direct address mode
	 */
	AddressingMode direct = new AddressingMode(2,1,0x10,"\t%byte")
	{
		public int getAddress(int pc) throws SIMException
		{
			return getWord(getByte(pc+1)); 
		}
		
	};

	/**
	 * Extended address mode
	 */
	AddressingMode extended = new AddressingMode(3,2,0x20,"\t%word")
	{
		public int getAddress(int pc) throws SIMException
		{
			return getWord(pc+1);
		}
		

	};

	/**
	 * Indexed 16 bit  offset
	 */
	AddressingMode indexed16 = new AddressingMode(3,3,0x30,"\t%word,X")
	{
		public int getAddress(int pc) throws SIMException
		{
			return X + getWord(pc+1);
		}


	};
	/**
	 * Indexed 8 bit  offset
	 */
	AddressingMode indexed8 = new AddressingMode(2,2,0x40,"\t%byte,X")
	{
		public int getAddress(int pc) throws SIMException
		{
			return X + getByte(pc+1);
		}


	};


	/**
	 * Indexed no offset
	 */
	AddressingMode indexed = new AddressingMode(1,1,0x50,"\t,X")
	{
		public int getAddress(int pc) throws SIMException
		{
			return X;
		}
		

	};

	class OpcodeAddressingMode extends AbstractOpcode
	{
		private CPUOperation op;
		private AddressingMode add;

		public OpcodeAddressingMode(int base,CPUOperation op,AddressingMode add)
		{
			super(base+add.getOffset(),add.getLength(),op.getTimes()+add.getTimes(),op.getDesc()+add.getDesc());
			this.op = op;
			this.add = add;
		}

		public int exec(int pc) throws SIMException
		{
			op.exec(pc,add);
			return times;
		}
	}




	/**
	 * Operation on acculumator
	 */
	abstract class CPUOperation
	{
		private int times;
		private String desc;
		
		public CPUOperation(String desc,int times)
		{
			this.desc = desc;
			this.times = times;
		}
		public int getTimes()
		{
			return times;
		}

		public String getDesc()
		{
			return desc;
		}
				      
		public abstract void exec(int pc,AddressingMode add) throws SIMException;
	}


	/**
	 * Initialize opcode for jmp/call address mode. The jmp address
	 * mode are : direct,extended,indexed16,indexed8 and indexed
	 */
	protected void initOpcodesJumpAddressingMode(int base,CPUOperation op)
	{
		base -= 0x10;
		
		setOpcode(new OpcodeAddressingMode(base,op,direct));
		setOpcode(new OpcodeAddressingMode(base,op,extended));
		setOpcode(new OpcodeAddressingMode(base,op,indexed16));
		setOpcode(new OpcodeAddressingMode(base,op,indexed8));
		setOpcode(new OpcodeAddressingMode(base,op,indexed));


	}

	/**
	 * Initialize opcode for logical address mode. Te logical
	 * address mode are : direct,A,X,indexed8,indexed.
	 *
	 * @param - base for the opcode.
	 */
	private void initOpcodesLogicalAddressingMode(int base,CPUOperation op)
	{
		/** Opcode relative to not existing direct mode */
		base -= 0x10;

		setOpcode(new OpcodeAddressingMode(base,op,direct));
		setOpcode(new OpcodeAddressingMode(base,op,addressA));
		setOpcode(new OpcodeAddressingMode(base,op,addressX));
		setOpcode(new OpcodeAddressingMode(base,op,indexed8));
		setOpcode(new OpcodeAddressingMode(base,op,indexed));

	}
	
	/**
	 * Initialize operation in all addressing mode.
	 * 
	 * Specifically -  immediate, direct, extended, indexed16,
	 * indexed8 and indexed.
	 *
	 */
	private void initOpcodesAllAddressingMode(int base,CPUOperation op)
	{
		setOpcode(new OpcodeAddressingMode(base,op,immediate));
		setOpcode(new OpcodeAddressingMode(base,op,direct));
		setOpcode(new OpcodeAddressingMode(base,op,extended));
		setOpcode(new OpcodeAddressingMode(base,op,indexed16));
		setOpcode(new OpcodeAddressingMode(base,op,indexed8));
		setOpcode(new OpcodeAddressingMode(base,op,indexed));

		
	}
	
	/**
	 * Initialize the CPU opcodes
	 */
	protected void initOpcodes()
	{
		CPUOperation op;

		/** RTS */
		setOpcode(new AbstractOpcode(0x81,1,6,"RTS")
		{
			public int exec(int pc) throws SIMException
			{
				PC = pop16();

				return 6;
			}
		});

		/** STX */
		op = new CPUOperation("STX",3)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				add.setMemory(pc,X);
			}
		};
		initOpcodesJumpAddressingMode(0xBF,op);
		
		/** JSR */
		op = new CPUOperation("JSR",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				push16(PC);
			
				PC = add.getAddress(pc);
			}
		};
		initOpcodesJumpAddressingMode(0xBD,op);

		
		/** Aritmetic operation  only 5 addressing modes */
		op = new CPUOperation("ROR",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);

				if (bit0(v))
				{
					v >>= 1;
					ccr(CCR_C,true);
					v |= 0x80;
				}
				else
				{
					v >>= 1;
					ccr(CCR_C,false);
				}


				ccr(CCR_N,false);
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x36,op);

		op = new CPUOperation("ROL",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);

				if (bit7(v))
				{
					v <<= 1;
					ccr(CCR_C,true);
					v |= 1;
				}
				else
				{
					v <<= 1;
					ccr(CCR_C,false);
				}
				
				
				ccr(CCR_N,false);
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x39,op);

		op = new CPUOperation("NEG",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				ccr(CCR_C,v != 0);
				v = (0 - v) & 0xff;

				ccr(CCR_N,bit7(v));
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x30,op);

		op = new CPUOperation("LSR",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				ccr(CCR_C,bit0(v));
				v >>= 1;

				ccr(CCR_N,false);
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x34,op);

		op = new CPUOperation("INC",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				v = (v + 1) & 0xff;

				ccr(CCR_N,bit7(v));
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x3C,op);

		op = new CPUOperation("DEC",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				v = (v -1) & 0xff;

				ccr(CCR_N,bit7(v));
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x3A,op);

		op = new CPUOperation("COM",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				v = 255 - v;

				ccr(CCR_C,true);
				ccr(CCR_N,bit7(v));
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x33,op);

		op = new CPUOperation("CLR",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				ccr(CCR_N,false);
				ccr(CCR_Z,true);

				add.setMemory(pc,0);
			}
		};
		initOpcodesLogicalAddressingMode(0x3F,op);
		
		op = new CPUOperation("ASL",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				ccr(CCR_C,bit7(v));
				v =  (v << 1) & 0xff;
				ccr(CCR_N,bit7(v));
				ccr(CCR_Z,v == 0);

				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x38,op);

		op = new CPUOperation("ASR",4)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int v = add.getMemory(pc);
				if (bit7(v))
					v = 0x80 | (v >> 1);
				else
					v = v >> 1;
				ccr(CCR_N,bit7(v));
				ccr(CCR_Z,v == 0);
				ccr(CCR_C,bit0(v));
						
				add.setMemory(pc,v);
			}
		};
		initOpcodesLogicalAddressingMode(0x37,op);

		
		/** Standard operation on all addressing modes */
		op = new CPUOperation("ADC",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				A = adc8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA9,op);

		op = new CPUOperation("ADD",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				A = add8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xAB,op);

		op = new CPUOperation("AND",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				A = and8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA4,op);

		op = new CPUOperation("BIT",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				int result = A & add.getMemory(pc);
				ccr(CCR_Z,result == 0);
				ccr(CCR_N,(result & 0x80) != 0);
			}
		};
		initOpcodesAllAddressingMode(0xA5,op);


		op = new CPUOperation("CMP",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				cmp8(A,add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA1,op);

		op = new CPUOperation("CPX",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				cmp8(X,add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA3,op);


		op = new CPUOperation("EOR",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{

				A = xor8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA8,op);

		op = new CPUOperation("LDA",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{

				A = ld8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA6,op);

		op = new CPUOperation("LDX",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{

				X = ld8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xAE,op);

		op = new CPUOperation("ORA",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				A = or8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xAA,op);

		op = new CPUOperation("SBC",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				A = sbc8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA2,op);

		op = new CPUOperation("SUB",2)
		{
			public void exec(int pc,AddressingMode add) throws SIMException
			{
				A = sub8(add.getMemory(pc));
			}
		};
		initOpcodesAllAddressingMode(0xA0,op);



	}

	/**
	 * Setup the reset vector used only by sub class
	 */
	protected void setResetVector(int r)
	{
		resetVector = r;
	}
	
}


   