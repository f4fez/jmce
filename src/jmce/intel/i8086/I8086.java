/**
   $Id: I8086.java 461 2010-12-14 13:44:05Z mviara $

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
package jmce.intel.i8086;

import jmce.sim.*;
import jmce.sim.cpu.*;
import jmce.util.Hex;

import jmce.sim.memory.*;
import jmce.util.Logger;

/**
 * Intel 8086 <p>
 *
 *<p>
 * <h3>Memory required :<h3>
 * <ul>
 *  <li>MAIN up to 1024 KB of memory.
 *  <li>IO up to 64 KB of I/O address space.
 * </ul>
 *
 * Implemented register : AX,BX,CX,DX,BP,SI,DI,ES,CS,DS,SS,SP,IP,F
 * <p>
 * @author Mario Viara
 * @version 1.00
 */
public class I8086 extends AbstractCPU implements I8086Constants
{
	private static Logger log = Logger.getLogger(I8086.class);

	/**
	 * Special register
	 */
	public int F,IP,SP,BP;

	/** Index register */
	public int SI,DI;

	/** General register */
	public int AX,BX,CX,DX;

	/** Segment register */
	public int CS,DS,SS,ES;

	static final String regs16[] = {"AX","CX","DX","BX","SP","BP","SI","DI"};
	static final String regs8[]  = {"AL","CL","DL","BL","AH","CH","DH","BH"};
	static final String sregs[] = {"ES","CS","SS","DS"};
	
	public I8086()
	{
		super("i8086");
		setEndian(LITTLE_ENDIAN);
		setClock(8000000);
		setClockPerCycle(1);
		setResetAddress((RESET_CS << 4) + RESET_IP);

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

	protected void initPeripherals() throws SIMException
	{
	}

	protected void initMemories()
	{
		Memory m ;
		m = getMemoryForName(MAIN_MEMORY);
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(MAIN_MEMORY,0x0100000));
		setMemory(m);

		/** Set memory name using segment:address format */
		for (int i = 0 ; i < m.getSize() ; i++)
		{
			m.setMemoryName(i,Hex.formatWord((i & 0xffff0000) >>> 4)+":"+Hex.formatWord(i & 0xffff));
		}			
			
		m = getMemoryForName(IO_MEMORY);
		
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(IO_MEMORY,0x10000));

		setIO(m);

	}
	
	public void init(Hardware parent) throws SIMException
	{
		initMemories();
		initPeripherals();
		initRegisters();
		initOpcodes();
		initOpcodeDecoder();

		super.init(parent);
	}

	/**
	 * Interface for ALU operation.
	 */
	interface ALUOP
	{
		public int alu8(int op1,int op2);
		public int alu16(int op1,int op2);
	}

	abstract class AbstractALURuntime extends  OpcodeRuntime8086
	{
		ALUOP op;
		
		AbstractALURuntime(int o,int l,int t,String d,ALUOP op)
		{
			super(o,l,t,d);
			this.op = op;
		}
	}

	abstract class AbstractALU extends  AbstractOpcode
	{
		ALUOP op;

		AbstractALU(int o,int l,int t,String d,ALUOP op)
		{
			super(o,l,t,d);
			this.op = op;
		}
	}

	protected void initOpcodeALU(int base,int time,String desc,ALUOP op)
	{
		/** base+0 /r ALU eb,rb */
		setOpcode(new AbstractALURuntime(base+0,0,time,desc+"\t%eb,%rb",op)
		{
			public int exec(Runtime8086 r) throws SIMException
			{
				decodeModRm(r);
				setValue_eb(r,op.alu8(getValue_eb(r),getValue_rb(r.reg)));
				return getTimes() + r.cycle;
			}
		});

		/** base+1 /r ALU ew,rw */
		setOpcode(new AbstractALURuntime(base+1,0,time,desc+"\t%ew,%rw",op)
		{
			public int exec(Runtime8086 r) throws SIMException
			{
				decodeModRm(r);
				setValue_ew(r,op.alu16(getValue_ew(r),getValue_rw(r.reg)));
				return getTimes() + r.cycle;
			}
		});

		/** base+2 /r ALU rb,eb */
		setOpcode(new AbstractALURuntime(base+2,0,time,desc+"\t%rb,%eb",op)
		{
			public int exec(Runtime8086 r) throws SIMException
			{
				decodeModRm(r);
				setValue_rb(r.reg,op.alu8(getValue_eb(r),getValue_rb(r.reg)));
				return getTimes() + r.cycle;
			}
		});


		/** base+3 /r ALU rw,ew */
		setOpcode(new AbstractALURuntime(base+3,0,time,desc+"\t%rw,%ew",op)
		{
			public int exec(Runtime8086 r) throws SIMException
			{
				decodeModRm(r);
				setValue_rw(r.reg,op.alu16(getValue_ew(r),getValue_rw(r.reg)));
				return getTimes() + r.cycle;
			}
		});

		/** base+4  ALU AL,db */
		setOpcode(new AbstractALU(base+4,2,time,desc+"\tAL,%byte",op)
		{
			public int exec(int pc) throws SIMException
			{
				AX = (AX & 0xFF00) | op.alu8(AX & 0xFF,getByte(pc+1));
				    
				return getTimes()+1;
			}
		});

		/** base+5  ALU AX,dw */
		setOpcode(new AbstractALU(base+5,3,time,desc+"\tAX,%word",op)
		{
			public int exec(int pc) throws SIMException
			{
				AX = op.alu16(AX,getWord(pc+1));

				return getTimes()+1;
			}
		});



	}

	/** Update SF, ZF and PF after 16-bit arithmetic operation */
	private final void fixFlags16(int v)
	{
		F &=  ~(F_SF| F_SF | F_PF);
		if ((v & 0x8000) != 0) F |= F_SF;
		if ((v & 0xffff) == 0) F |= F_ZF;
		v ^= v >> 4; v ^= v >> 2; v ^= v >> 1;
		if ((v & 1) == 0) F |= F_PF;
	}

	/** update SF, ZF, PF, CF, OF, AF  after 8 bit addition */
	private final void fixFlagsAdd8(int x, int v, int y)
	{
		F &= ~(F_CF|F_ZF|F_SF|F_OF|F_PF|F_AF);
		if ((y & 0x100) != 0) F |= F_CF;
		if (((x^y^v) & 0x10) != 0) F |= F_AF;
		if ((((x^(~v))&(x^y)) & 0x80) != 0) F |= F_OF;
		if ((y & 0x80) != 0) F |= F_SF;
		if ((y & 0xff) == 0) F |= F_ZF;
		y ^= y >> 4; y ^= y >> 2; y ^= y >> 1;
		if ((y & 1) == 0) F |= F_PF;
	}

	/** update SF, ZF, PF, CF, OF, AF  after 16 bit addition */
	private final void fixFlagsAdd16(int x, int v, int y)
	{
		F &= ~(F_CF|F_ZF|F_SF|F_OF|F_PF|F_AF);
		if ((y & 0x10000) != 0) F |= F_CF;
		if (((x^y^v) & 0x10) != 0) F |= F_AF;
		if ((((x^(~v))&(x^y)) & 0x8000) != 0) F |= F_OF;
		if ((y & 0x8000) != 0) F |= F_SF;
		if ((y & 0xffff) == 0) F |= F_ZF;
		y ^= y >> 4; y ^= y >> 2; y ^= y >> 1;
		if ((y & 1) == 0) F |= F_PF;
	}

	/** Update SF, ZF and PF after 8-bit arithmetic operation */
	private final void fixFlags8(int v)
	{
		F &=  ~(F_SF| F_SF | F_PF);
		if ((v & 0x80) != 0) F |= F_SF;
		if ((v & 0xff) == 0) F |= F_ZF;
		v ^= v >> 4; v ^= v >> 2; v ^= v >> 1;
		if ((v & 1) == 0) F |= F_PF;
	}

	protected final int add8(int a,int b)
	{
		int result = a + b;
		fixFlagsAdd8(a,b,result);

		return result & 0xff;
	}

	protected final int add16(int a,int b)
	{
		int result = a + b;
		fixFlagsAdd16(a,b,result);

		return result & 0xffff;
	}

	protected final int sub8(int a,int b)
	{
		int result = a - b;
		fixFlagsAdd8(a,b^0x80,result);

		return result & 0xff;
	}

	protected final int sub16(int a,int b)
	{
		int result = a - b;
		fixFlagsAdd16(a,b ^ 0x8000,result);

		return result & 0xffff;
	}

	protected final int xor8(int a,int b)
	{
		int result =  (a ^ b) & 0xff;
		F &= ~(F_CF|F_OF);
		fixFlags8(result);
		return result;
	}

	protected final int xor16(int a,int b)
	{
		int result = (a ^ b) & 0xffff;
		F &= ~(F_CF|F_OF);
		fixFlags16(result);

		return result;

	}

	protected void initOpcodes()
	{
		int i;

		initOpcodeALU(0x28,2,"SUB",new ALUOP()
		{

			public int alu8(int op1,int op2)
			{
				return sub8(op1 ,op2);
			}

			public int alu16(int op1,int op2)
			{
				return sub16(op1,op2);
			}

		});

		initOpcodeALU(0x00,2,"ADD",new ALUOP()
		{

			public int alu8(int op1,int op2)
			{
				return add8(op1 ,op2);
			}
			
			public int alu16(int op1,int op2)
			{
				return add16(op1,op2);
			}

		});

		initOpcodeALU(0x30,2,"XOR",new ALUOP()
		{

			public int alu8(int op1,int op2)
			{
				return xor8(op1 ,op2);
			}

			public int alu16(int op1,int op2)
			{
				return xor16(op1,op2);
			}

		});
		
		setOpcode(new JR(0x76,"JBE\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_CF|F_ZF)) == (F_CF|F_ZF))
					return jr(pc);
				else
					return nojmp();
			}			

		});

		setOpcode(new JR(0x77,"JA\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_CF|F_ZF)) == 0)
					return jr(pc);
				else
					return nojmp();
			}			
			
		});

		setOpcode(new JR(0x74,"JZ\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_ZF)) != 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});

		setOpcode(new JR(0x7A,"JPE\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_PF)) != 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});

		setOpcode(new JR(0x7B,"JPO\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_PF)) == 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});


		setOpcode(new JR(0x75,"JNZ\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_ZF)) == 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});

		setOpcode(new JR(0x78,"JS\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_SF)) != 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});

		setOpcode(new JR(0x72,"JB\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_CF)) != 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});


		setOpcode(new JR(0x70,"JO\t%ipoffset")
		{
			public int exec(int pc) throws SIMException
			{
				if ((F & (F_OF)) != 0)
					return jr(pc);
				else
					return nojmp();
			}			

		});
		
		setOpcode(new AbstractOpcode(0xF4,1,2,"HLT")
		{
			public int exec(int pc) throws SIMException
			{
				halt();

				return 2;
			}			
		});
		setOpcode(new AbstractOpcode(0xFA,1,2,"CLI")
		{
			public int exec(int pc) throws SIMException
			{
				F &= ~F_IF;
				return 2;
			}			
		});

		setOpcode(new AbstractOpcode(0xFB,1,2,"STI")
		{
			public int exec(int pc) throws SIMException
			{
				F |= F_IF;
				return 2;
			}			
		});

		setOpcode(new AbstractOpcode(0xEA,5,11,"JMP\t%cd")
		{
			public int exec(int pc) throws SIMException
			{
				pc(get_cd(pc+1));
				return 11;
			}
		});

		/** Operation on word register operand rw */
		for (i = 0; i < 8 ; i++)
		{
			setOpcode(new AbstractOpcode(0xb8+i,3,2,"MOV\t"+regs16[i]+",%word")
			{
				public int exec(int pc) throws SIMException
				{
					setValue_rw(opcode,getWord(pc+1));

					return 2;
				}
				
			});
		}

		/** MOV Ew,Iw */
		setOpcode(new OpcodeRuntime8086(0xc7,2,3,"MOV\t%ew,%-1%word")
		{
			public int exec(Runtime8086 r) throws SIMException
			{
				decodeModRm(r);
				
				setValue_ew(r,getWord(r.pc+r.length));
				return 3+r.cycle;
			}
			
		});
		
		/** MOV Sw,Ew */
		setOpcode(new OpcodeRuntime8086(0x8e,0,2,"MOV\t%sw,%ew")
		{
			public int exec(Runtime8086 r) throws SIMException
			{
				decodeModRm(r);
				setValue_sw(r,getValue_ew(r));
				return 2 + r.cycle;
			}
			
		});
		
	}

	protected void setValue_sw(Runtime8086 r,int value)
	{
		switch (r.reg & 3)
		{
			case	0:
				ES = value;
				break;
			case	1:
				CS = value;
				break;
			case	2:
				SS = value;
				break;
			case	3:
				DS = value;
				break;
						
		}
	}

	public int getSegBase(int s) throws SIMException
	{

		switch (s)
		{
			default:
				throw new CPUException(this,"Invalid segment "+s);

			case	DS_R:
				return  DS * 16;

			case	SS_R:
				return  SS * 16;

			case	ES_R:
				return  ES * 16;

			case	CS_R:
				return CS * 16;
		}
		
	}

	public void setByte(int s,int a,int v) throws SIMException
	{
		int base = getSegBase(s);

		setByte(base+a,v);
		log.info("SETB "+sregs[s]+":"+Hex.formatWord(a)+"="+Hex.formatByte(v));


	}

	public void setWord(int s,int a,int v) throws SIMException
	{
		int base = getSegBase(s);
		
		setWord(base+a,v);
		log.info("SETW "+sregs[s]+":"+Hex.formatWord(a)+"="+Hex.formatWord(v));

		
	}
	
	public int getWord(int s,int a) throws SIMException
	{
		int base = getSegBase(s);

		int v = getWord(base+a);
		
		log.info("GETW "+sregs[s]+":"+Hex.formatWord(a)+"="+Hex.formatWord(v));

		return v;
	}

	public int getByte(int s,int a) throws SIMException
	{
		int base = getSegBase(s);

		int v = getByte(base+a);

		log.info("GETB "+sregs[s]+":"+Hex.formatWord(a)+"="+Hex.formatByte(v));

		return v;
	}
	
	public String get_ew(Runtime8086 r) throws SIMException
	{
		int length = 2;
		String left = "";
		String right ="";
		String center = "";
		int mod = r.modrm & 0xc0;
		int rm = r.modrm & 0x07;


		switch (mod)
		{
			case 0x00:
				break;
			case 0x40:
				right ="+"+Hex.formatByte(getByte(r.pc + length++));
				break;
			case 0x80:
				left=Hex.formatWord(getWord(r.pc + length));
				length += 2;
				break;
			case 0xc0:
				return regs16[rm & 0x07];
		}


		switch (rm)
		{
			case 0:
				center ="BX+SI";
				break;
			case 1:
				center = "BX+DI";
				break;
			case 2:
				center = "BP+SI";
				break;
			case 3:
				center = "BP+DI";
				break;
			case 4:
				center = "SI";
				break;
			case 5:
				center = "DI";
				break;
			case 6:
				if (mod == 0)
				{
					left =  Hex.formatWord(getWord(r.pc+length));
					length += 2;
				}
				else
				{
					center = "BP";
				}
				break;
			case 7:
				center = "BX";
				break;
		}

		return "["+left+center+right+"]";
		
	}

	protected void setValue_eb(Runtime8086 r,int value) throws SIMException
	{
		if (r.memory)
			setByte(r.defSeg,r.addr,value);
		else
			setValue_rb(r.addr,value);
	}
	
	protected void setValue_ew(Runtime8086 r,int value) throws SIMException
	{
		if (r.memory)
			setWord(r.defSeg,r.addr,value);
		else
			setValue_rw(r.addr,value);
		
	}
	
	protected int getValue_ew(Runtime8086 r) throws SIMException
	{
		
		if (r.memory)
			return getWord(r.defSeg,r.addr);
		else
			return getValue_rw(r.addr);
	}

	protected int getValue_eb(Runtime8086 r) throws SIMException
	{

		if (r.memory)
			return getByte(r.defSeg,r.addr);
		else
			return getValue_rb(r.addr);
	}

	
	protected void initOpcodeDecoder()
	{

		addDecoder(new AbstractDecoder("%ipoffset",1)
		{
			protected String implDecode(CPU cpu,CpuRuntime  r,int startPc,int len,int currentPc) throws SIMException
			{
				int offset = getByte(currentPc+1);
				String s = Hex.formatByte(offset);
				int pc = startPc + len; //fixme
				pc = addOffset(pc,offset);
				s += " ["+Hex.formatWord(pc)+"]";
				return s;
			}
		});

		addDecoder(new AbstractDecoder("%ew",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime r,int startPc,int len,int currentPc) throws SIMException
			{
				size = r.length;
				return get_ew((Runtime8086)r);
			}

		});
		addDecoder(new AbstractDecoder("%sw",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				size = _r.length;
				Runtime8086 r = (Runtime8086)_r;
				return sregs[r.reg & 3];
			}
			
		});
		
		addDecoder(new AbstractDecoder("%rw",0) 
		{
			
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				Runtime8086 r = (Runtime8086)_r;
				
				return regs16[r.reg & 7];
			}
		});
		
		addDecoder(new AbstractDecoder("%cd",4) 
		{
			protected String implDecode(CPU cpu,CpuRuntime r,int startPc,int len,int currentPc) throws SIMException
			{
				return Hex.formatWord(getWord(currentPc+3))+":"+Hex.formatWord(getWord(currentPc+1));
			}
		});
	}
	
	protected void initRegisters()
	{

		addRegister(new StandardRegister("SP",Register.FAMILY_SP,16,0)
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

		addRegister(new StandardRegister("BP",Register.FAMILY_SP,16,0)
		{
			public int getRegister()
			{
				return BP;
			}

			public void setRegister(int value)
			{
				BP = value;
			}


		});

		addRegister(new StandardRegister("IP",Register.FAMILY_PC,16,RESET_IP)
		{
			public int getRegister()
			{
				return IP;
			}

			public void setRegister(int value)
			{
				IP = value;
			}


		});



		addRegister(new StandardRegister("F",Register.FAMILY_PSW,16,0)
		{
			public void add(StringBuffer s,int mask,String d)
			{
				if ((F & mask) != 0)
					s.append(d);
				else
					s.append('-');
			}
			
			public String descValue()
			{
				StringBuffer s = new StringBuffer("["+Hex.formatWord(F)+"]");
				
				s.append("----");
				add(s,F_OF,"O");
				add(s,F_DF,"D");
				add(s,F_IF,"I");
				add(s,F_TF,"T");
				add(s,F_SF,"S");
				add(s,F_ZF,"Z");
				s.append("-");
				add(s,F_AF,"A");
				s.append("-");
				add(s,F_PF,"P");
				s.append("-");
				add(s,F_CF,"C");


				return s.toString();
			}

			public int getRegister()
			{
				return F;
			}

			public void setRegister(int value)
			{
				F = value;
			}


		});

		addRegister(new StandardRegister("AX",Register.FAMILY_ACC,16,0)
		{

			public int getRegister()
			{
				return AX;
			}

			public void setRegister(int value)
			{
				AX = value;
			}


		});

		addRegister(new StandardRegister("BX",Register.FAMILY_GENERAL,16,0)
		{

			public int getRegister()
			{
				return BX;
			}

			public void setRegister(int value)
			{
				BX = value;
			}


		});

		addRegister(new StandardRegister("CX",Register.FAMILY_GENERAL,16,0)
		{

			public int getRegister()
			{
				return CX;
			}

			public void setRegister(int value)
			{
				CX = value;
			}


		});
		
		addRegister(new StandardRegister("DX",Register.FAMILY_GENERAL,16,0)
		{

			public int getRegister()
			{
				return DX;
			}

			public void setRegister(int value)
			{
				DX = value;
			}


		});


		addRegister(new StandardRegister("SI",Register.FAMILY_INDEX,16,0)
		{

			public int getRegister()
			{
				return SI;
			}

			public void setRegister(int value)
			{
				SI = value;
			}


		});

		addRegister(new StandardRegister("DI",Register.FAMILY_INDEX,16,0)
		{

			public int getRegister()
			{
				return DI;
			}

			public void setRegister(int value)
			{
				DI = value;
			}


		});

		addRegister(new StandardRegister("CS",Register.FAMILY_CONTROL,16,RESET_CS)
		{

			public int getRegister()
			{
				return CS;
			}

			public void setRegister(int value)
			{
				CS = value;
			}


		});
		addRegister(new StandardRegister("DS",Register.FAMILY_CONTROL,16,0)
		{

			public int getRegister()
			{
				return DS;
			}

			public void setRegister(int value)
			{
				DS = value;
			}


		});
		
		addRegister(new StandardRegister("ES",Register.FAMILY_CONTROL,16,0)
		{

			public int getRegister()
			{
				return ES;
			}

			public void setRegister(int value)
			{
				ES = value;
			}


		});
		addRegister(new StandardRegister("SS",Register.FAMILY_CONTROL,16,0)
		{

			public int getRegister()
			{
				return SS;
			}

			public void setRegister(int value)
			{
				SS = value;
			}


		});
		
		
	}

	
	public boolean isInterruptEnabled()
	{
		return (F & F_IF) != 0 ? true : false;
	}


	public final void pc(int pc)
	{
		this.IP = pc & 0xffff;
		this.CS = (pc & 0xffff0000) >> 4;
	}

	public int pc()
	{
		return CS << 4 | IP;
	}

	/**
	 * Decode the ModRm byte
	 */
	void decodeModRm(Runtime8086 r) throws SIMException
	{
		r.length = 2;
		r.modrm = getByte(r.pc + 1);

		r.memory = true;
		int mod = r.modrm & 0xc0;
		int rm = r.modrm & 0x07;
		
		r.reg = (r.modrm & 0x38) >> 3;
		
		switch (mod)
		{
			case 0x00:
				r.addr = 0;
				break;
			case 0x40:
				r.addr = addOffset(0,getByte(r.pc + r.length++));
				r.cycle += 4;
				break;
			case 0x80:
				r.addr = getWord(r.pc + r.length);
				r.length += 2;
				r.cycle += 4;
				break;
			case 0xc0:
				r.memory = false;
				r.addr = rm ;
				return;
		}

		r.defSeg = DS_R;
		
		switch (rm)
		{
			case 0:
				r.addr += BX + SI;
				r.cycle += 7;
				break;
			case 1:
				r.addr += BX + DI;
				r.cycle += 8;
				break;
			case 2:
				r.addr += BP + SI;
				r.defSeg = SS_R;
				r.cycle += 8;
				break;
			case 3:
				r.addr += BP + DI;
				r.defSeg = SS_R;
				r.cycle += 7;
				break;
			case 4:
				r.addr += SI;
				r.cycle += 5;
				break;
			case 5:
				r.addr += DI;
				r.cycle += 5;
				break;
			case 6:
				if (mod == 0)
				{
					r.addr = getWord(r.pc+r.length);
					r.length += 2;
					r.cycle += 6;
				}
				else
				{
					r.addr += BP;
					r.defSeg = SS_R;
					r.cycle += 5;
				}
				break;
			case 7:
				r.addr += BX;
				r.cycle += 5;
				break;
		}
		
		r.addr &= 0xffff;
	}


	public int getValue_rb(int r)
	{
		switch (r & 7)
		{
			case	0:
				return AX & 0xFF;	// AL

			case	1:
				return CX & 0xFF;	// CL

			case	2:
				return DX & 0xFF;	// DL

			case	3:
				return BX & 0xFF;	// BL

			case	4:
				return AX >>> 8;	// AH

			case	5:
				return CX >>> 8;	// CH

			case	6:
				return DX >>> 8;	// DH

			case	7:
				return BX >>> 8;	// BH

		}

		return -1;
		
	}
	
	public int getValue_rw(int opcode)
	{
		switch (opcode & 7)
		{
			case	0:
				return AX;

			case	1:
				return CX;
				
			case	2:
				return DX;
				
			case	3:
				return BX;
				
			case	4:
				return SP;
				
			case	5:
				return BP;
				
			case	6:
				return SI;
				
			case	7:
				return DI;
				
		}

		return -1;
	}

	public void setValue_rb(int r,int v)
	{
		v &= 0xff;
		
		switch (r & 0x07)
		{
			case	0:
				AX = (AX & 0xFF00) | v;
				break;
				
			case	1:
				CX = (CX & 0xFF00) | v;
				break;

			case	2:
				DX = (DX & 0xFF00) | v;
				break;

			case	3:
				BX = (BX & 0xFF00) | v;
				break;

			case	4:
				AX = (AX & 0x00FF) | (v << 8);
				break;

			case	5:
				CX = (CX & 0x00FF) | (v << 8);
				break;

			case	6:
				DX = (DX & 0x00FF) | (v << 8);
				break;

			case	7:
				BX = (BX & 0x00FF) | (v << 8);
				break;
				
		}
	}
	/**
	 * Set value for operand rw (word register)
	 */
	public void setValue_rw(int r,int value)
	{
		switch (r & 7)
		{
			case	0:
				log.info("SET AX="+Hex.formatWord(value));
				AX = value;
				break;
			case	1:
				CX = value;
				break;
			case	2:
				DX = value;
				break;
			case	3:
				BX = value;
				break;
			case	4:
				SP = value;
				break;
			case	5:
				BP = value;
				break;
			case	6:
				SI = value;
				break;
			case	7:
				DI = value;
				break;
		}				
	}
	
	/**
	 * Get operand ssss:oooo
	 */
	public int get_cd(int pc)  throws SIMException
	{
		int ea = getWord(pc+0)+getWord(pc+2)*16;

		return ea;
	}
	
	/**
	 * Repeat not equal string prefix
	 */
	class REPNE extends OpcodePrefix8086
	{
		REPNE()
		{
			super(0xf2,1,0,"REPNE");
		}

		public int exec(Runtime8086 r) throws SIMException
		{
			
			r.rep = REPNE_P;
			r.cycle++;
			return 1;
		}
		
		
	}

	/**
	 * Repeat not equal string prefix
	 */
	class REPE extends OpcodePrefix8086
	{
		REPE()
		{
			super(0xf2,1,0,"REPE");
		}

		public int exec(Runtime8086 r) throws SIMException
		{

			r.rep=REPE_P;
			r.cycle++;
			return 1;
		}

	}

	/**
	 * Abstract implementation of JR
	 */
	abstract class JR extends AbstractOpcode
	{

		JR(int opcode,String s)
		{
			super(opcode,2,3,s);
		}

		protected final int jr(int pc) throws SIMException
		{
			int offset = getByte(pc+1);
			IP = addOffset(IP,offset);

			return 7;
				
		}

		protected final int nojmp()
		{
			return 2;
		}

	}

	
	protected Runtime8086 createRuntime() throws SIMException
	{
		return new Runtime8086();
	}
}
