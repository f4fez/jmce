/**
   $Id: MCS51.java 691 2011-09-02 07:57:21Z mviara $

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
package jmce.intel.mcs51;

import jmce.sim.*;
import jmce.sim.cpu.*;
import jmce.sim.memory.*;
import jmce.util.Logger;

/**
 * Implementation of Intel MCS51.
 * 
 * <p>
 * <h3>Supported memory :</h3>
 * <ul>
 *  <li> CODE up to 64 KB of memory used to store code accessed by
 *  fetch and MOVX instruction. Also operate as MAIN_MEMORY.</li>
 *  <li> XDATA up to 64 KB of external memory accessible via MOVX
 *  ...</li>
 *  <li> DATA upto 256 bytes of internal memory.</li>
 *  <li> SFR memory used to store special functons register. This memory
 * for reason performance must have a size at least of <tt>256</tt>
 * bytes. Also operate as IO_MEMORY.</li>
 * </ul>
 * <p>
 * <h3>Memory addressing</h3>
 * <p>
 * The  Intel 8051 have a unique 8 bit data memory addressing :
 * When accessed in indirect mode always the DATA memory is referenced,
 * when accessed in direct mode the first 128 byte reference the DATA
 * memory the other 128 byte (Location from 128 to 255) reference the
 * SFR memory.
 * 
 * <h3>Implemented peripheral :</h3>
 * <ul>
 *  <li>1 x Timer (channel 0 and 1).</li>
 *  <li>1 x Serial port.</li>
 *  <li>4 x 8 bit I/O ports.</li>
 *  <li>1 x 128 byte DATA memory.</li>
 *  <li>1 x 128 byte SFR memory.</li>
 * </ul>
 * 
 * <p>This class is derived form a previus 8051 emulator and the
 * simulation very well tested.
 *
 * <p>This CPU honor the {@link #setCallListener} method.
 * 
 * @author Mario Viara
 * @version 1.05
 * 
 */
public class MCS51 extends AbstractCPU implements MCS51Constants
{
	protected static final Logger log = Logger.getLogger(MCS51.class);

	/** Code memory name */
	static public final String CODE_MEMORY = "CODE";

	/** External data memory name */
	static public final String XDATA_MEMORY = "XDATA";

	/** Data memory name */
	static public final String DATA_MEMORY = "DATA";

	/** Special function register memory name */
	static public final String SFR_MEMORY = "SFR";

	/** Memories */
	private Memory data = null ,sfr = null ,xdata = null;
	
	@SuppressWarnings("unused")
	private Register RPC,RSP,RA,RB,RPSW;
	private Register R[] =  new Register[8];
	private Register RDPL,RDPH,RDPTR;
	private int sfrBitmap[] = new int[256];
	private String bitNames[] = new String[256];

	/** Hi address during 8 bit MOVEX */
	private int sfrXdataHi = P2;

	/** Program counter  */
	private int pc;

	/** True when interrupt are enabled */
	private boolean interruptEnabled = false;

	/** True when interrupt in use to avoid nested interrupt */
	private boolean interruptInUse = false;
	
	/** Current dptr register in use */
	private int dptrIndex = 0;

	/** Copy of the current DPTR, when multiple DPTRS are used */
	private int dptrs[] = new int[256];

	/* True if the standard AUXR1 dptr is used */
	private boolean auxrDptr = false;

	/**
	 * Class for 8051 register R0..R7
	 */
	class Register8051 extends MemoryRegister
	{
		private int r;
		
		Register8051(int n)
		{
			super(data,n,"R"+n,FAMILY_GENERAL,8);
			this.r = n;
		}

		public final void setRegister(int value) throws SIMException
		{
			setIndex((sfr.getMemory(PSW) & 0x18) + r);
			super.setRegister(value);
		}

		public final int getRegister() throws SIMException
		{
			setIndex((sfr.getMemory(PSW) & 0x18) + r);
			return super.getRegister();
		}
		
		
	}

	/**
	 * Default constructor
	 */
	public MCS51()
	{
		super("i8051");
		
		setEndian(BIG_ENDIAN);
		setClock(12000000);
		setClockPerCycle(12);
	}


	public final void setWord(int a,int v) throws SIMException
	{
		memory.setMemory(a+1,v & 0xff);
		memory.setMemory(a+0,v >>> 8);
	}


	public final int getWord(int a) throws SIMException
	{
		return memory.getMemory(a+1) | (memory.getMemory(a+0) << 8);
	}

	/**
	 * Return the 8 high bit during 8 bit access to XDATA
	 */
	protected int getXdataHi() throws SIMException
	{
		int value = 0;
		
		if (sfrXdataHi != -1)
			value = sfr(sfrXdataHi);


		return value;
	}

	/*
	 * Set the 8 high bit sfr register used during 8 bit access to
	 * XDATA or set to -1 to return 0.
	 */
	protected void setSfrXdataHi(int sfr)
	{
		sfrXdataHi = sfr;
	}

	/**
	 * Return the memory used as XDATA
	 */
	protected Memory getXdata()
	{
		return xdata;
	}

	/**
	 * Return the memory used as SFR.
	 */
	protected Memory getSfr()
	{
		return sfr;
	}

	/**
	 * Setr the memory used as SFR.
	 */ 
	protected void setSfr(Memory m)
	{
		sfr = m;
	}


	/**
	 * Set the memory used aas XDATA
	 */
	protected void setXdata(Memory m)
	{
		xdata = m;
	}

	/**
	 * Return the memory used as DATA
	 */
	protected Memory getData()
	{
		return data;
	}

	/**
	 * Set the memory used as DATA
	 */
	protected void setData(Memory m)
	{
		data = m;
	}

	/**
	 * Return a SFR register.
	 */
	public final int sfr(int add) throws SIMException
	{
		return sfr.getMemory(add);
				
	}

	/**
	 * Set a SFR.
	 */
	public final void sfr(int add,int value) throws SIMException
	{
		sfr.setMemory(add,value);
	}



	/**
	 * Get a directy byte of memory.
	 * <p>
	 *
	 * The first 128 location (0..127) reference DATA memory the
	 * last (128 .. 255) the SFR memory.
	 *
	 * @param add - Address 0 .. 255
	 *
	 * @return the result.
	 */
	public final int getDirect(int add) throws SIMException
	{
		if (add >= 128)
			return sfr(add);
		else
			return data(add);
	}

	/**
	 * Set a directy byte of memory.
	 * <p>
	 *
	 * The first 128 location (0..127) reference DATA memory the
	 * last (128 .. 255) the SFR memory.
	 *
	 * @param add - Address 0 .. 255
	 */
	public final void setDirect(int add,int value) throws SIMException
	{
		if (add >= 128)
			sfr(add,value);
		else
			data(add,value);
	}

	/**
	 * Return the name of a direct byte of memory.
	 * <p>
	 * 
	 * The first 128 location (0..127) reference DATA memory the
	 * last (128 .. 255) the SFR memory.
	 *
	 * @param add - Address 0 .. 255
	 */
	public String getDirectName(int add)
	{
		if (add >= 128)
			return getSfrName(add);
		else
			return getDataName(add);

	}


	public final boolean getBitCode(int add) throws SIMException
	{
		return getBit(code(add));
	}

	protected final int getDirectCode(int pc) throws SIMException
	{
		return getDirect(code(pc));
	}


	/**
	 * Initialize arithmetic opcode
	 */
	private  void initArithmeticOpcode(ArithmeticOperation op,int basecode,String name)
	{
		// XXX A,Ri
		for (int i = 0 ; i < 8 ; i++)
			setOpcode(new Arithmetic(basecode|0x08|i,1,op,name+"\tA,%reg")
			{
				public int getValue(int pc) throws SIMException
				{
					return r((opcode & 7));
				}

			});

		// XXX A,direct
		setOpcode(new Arithmetic(basecode|5,2,op,name+"\tA,%direct")
		{
			public int getValue(int pc) throws SIMException
			{
				return getDirectCode(pc+1);
			}

		});

		// XXX A,@Rx0
		for (int i = 0 ; i < 2 ; i++)
		{
			setOpcode(new Arithmetic(basecode|6|i,1,op,name+"\tA,%ri")
			{
				public int getValue(int pc) throws SIMException
				{
					return data(r((opcode & 1)));
				}

			});

		}

		// XXX A,#data
		setOpcode(new Arithmetic(basecode|4,2,op,name+"\tA,#%byte")
		{
			public int getValue(int pc) throws SIMException
			{
				return code(pc+1);
			}

		});

	}


	/**
	 * Initialize the opcodes.
	 */
	protected void initOpcodes()
	{

		
		for (int i = 0 ; i < 8 ; i++)
		{
			setOpcode(new ACALL((i << 5)|0x11));
			setOpcode(new AJMP((i << 5)|0x01));
		}

		
		initArithmeticOpcode(new ArithmeticADD(),0x20,"ADD");
		
		initArithmeticOpcode(new ArithmeticADDC(),0x30,"ADDC");
		
		initArithmeticOpcode(new ArithmeticANL(),0x50,"ANL");
		initArithmeticOpcode(new ArithmeticORL(),0x40,"ORL");
		initArithmeticOpcode(new ArithmeticSUBB(),0x90,"SUBB");
		initArithmeticOpcode(new ArithmeticXRL(),0x60,"XRL");


		setOpcode(new ANL_DIRECT_A());
		setOpcode(new ANL_DIRECT_DATA());
		setOpcode(new ANL_C_DIRECT());
		setOpcode(new ANL_C_NOT_DIRECT());

		setOpcode(new CJNE_A_DIRECT());
		setOpcode(new CJNE_A_DATA());

		for (int r = 0 ; r < 8 ; r++)
		{
			setOpcode(new CJNE_R_DATA(r));
			setOpcode(new DEC_R(r));
			setOpcode(new DJNZ_R(r));
			setOpcode(new INC_R(r));
			setOpcode(new MOV_A_R(r));
			setOpcode(new MOV_R_A(r));
			setOpcode(new MOV_R_DIRECT(r));
			setOpcode(new MOV_R_DATA(r));
			setOpcode(new MOV_DIRECT_R(r));
			setOpcode(new XCH_A_R(r));
		}

		for (int r = 0 ; r < 2 ; r++)
		{
			setOpcode(new CJNE_RI_DATA(r));
			setOpcode(new DEC_RI(r));
			setOpcode(new INC_RI(r));
			setOpcode(new MOV_A_RI(r));
			setOpcode(new MOV_RI_A(r));
			setOpcode(new MOV_DIRECT_RI(r));
			setOpcode(new MOV_RI_DIRECT(r));
			setOpcode(new MOV_RI_DATA(r));
			setOpcode(new MOVX_A_RI(r));
			setOpcode(new MOVX_RI_A(r));
			setOpcode(new XCH_A_RI(r));
			setOpcode(new XCHD_A_RI(r));
		}

		setOpcode(new CLR_A());
		setOpcode(new CLR_C());
		setOpcode(new CLR_BIT());

		setOpcode(new CPL_A());
		setOpcode(new CPL_C());
		setOpcode(new CPL_BIT());

		setOpcode(new DA_A());

		setOpcode(new DEC_A());
		setOpcode(new DEC_DIRECT());

		setOpcode(new DIV_AB());

		setOpcode(new DJNZ_DIRECT());

		setOpcode(new INC_A());
		setOpcode(new INC_DIRECT());
		setOpcode(new INC_DPTR());

		setOpcode(new JB());
		setOpcode(new JBC());
		setOpcode(new JC());

		setOpcode(new JMP_A_DPTR());

		setOpcode(new JNB());
		setOpcode(new JNC());
		setOpcode(new JNZ());
		setOpcode(new JZ());

		setOpcode(new LCALL());
		setOpcode(new LJMP());

		setOpcode(new MOV_A_DIRECT());
		setOpcode(new MOV_A_DATA());
		setOpcode(new MOV_DIRECT_A());
		setOpcode(new MOV_DIRECT_DIRECT());
		setOpcode(new MOV_DIRECT_DATA());
		setOpcode(new MOV_C_BIT());
		setOpcode(new MOV_BIT_C());
		setOpcode(new MOV_DPTR_DATA16());
		setOpcode(new MOVC_A_DPTR_A());
		setOpcode(new MOVC_A_PC_A());
		setOpcode(new MOVX_A_DPTR());
		setOpcode(new MOVX_DPTR_A());
		setOpcode(new MUL_AB());
		setOpcode(new NOP());
		setOpcode(new ORL_C_BIT());
		setOpcode(new ORL_C_NBIT());

		setOpcode(new POP_DIRECT());
		setOpcode(new PUSH_DIRECT());
		setOpcode(new RET());
		setOpcode(new RETI());

		setOpcode(new RL_A());
		setOpcode(new RLC_A());
		setOpcode(new RR_A());
		setOpcode(new RRC_A());

		setOpcode(new SETB_C());
		setOpcode(new SETB_BIT());

		setOpcode(new SJMP());
		setOpcode(new SWAP_A());

		setOpcode(new XCH_A_DIRECT());

		setOpcode(new XRL_DIRECT_A());
		setOpcode(new XRL_DIRECT_DATA());
		setOpcode(new ORL_DIRECT_A());
		setOpcode(new ORL_DIRECT_DATA());
	}

	/**
	 * Set a bit name
	 */
	public void setBitName(int add,String name)
	{
		bitNames[add] = name;
	}

	/**
	 * Return a bit name
	 */
	public String getBitName(int add)
	{
		int bit;

		if (bitNames[add] != null)
		{
			return bitNames[add];
		}

		bit = (add & 7);

		if (add < 128)
		{
			add = 0x20 + add / 8 ;
		}
		else
		{
			add = add & 0xf8;
		}

		return getDirectName(add)+"^"+bit;
	}

	/**
	 * Set the internal address of one bit
	 */
	public void setSfrBitmap(int i,int add)
	{
		sfrBitmap[i] = add;
	}

	/**
	 * Return a bit
	 */
	public final boolean getBit(int add) throws SIMException
	{
		
		int value = getDirect(sfrBitmap[add]);

		return (value & ((1 << (add & 0x07)))) != 0 ? true : false;

	}

	
	/**
	 * Complement bit
	 */
	public final void cplBit(int bit) throws SIMException
	{
		setBit(bit,!getBit(bit));
	}

	/**
	 * Set a bit
	 */
	public final void setBit(int add,boolean value) throws SIMException
	{
		int mask = 1 << (add & 7);
		add = sfrBitmap[add];

		if (add >= 128)
		{
			if (value)
				sfr.setBit(add,mask);
			else
				sfr.clrBit(add,mask);
		}
		else
		{
			if (value)
				data.setBit(add,mask);
			else
				data.clrBit(add,mask);
		}

	}



	/**
	 * Initialize the register.
	 */
	protected void initRegisters()
	{
		RPC = addRegister(new StandardRegister("PC",Register.FAMILY_PC,16,0)
		{
			public int getRegister()
			{
				return pc;
			}

			public void setRegister(int value)
			{
				pc = value;
			}


		});
			
		RSP = addRegister(new MemoryRegister(sfr,SP,"SP",Register.FAMILY_SP,8));
		RA  = addRegister(new MemoryRegister(sfr,ACC,"A",Register.FAMILY_ACC,8));
		RB  = addRegister(new MemoryRegister(sfr,B,"B",Register.FAMILY_ACC,8));
		RPSW= addRegister(new MemoryRegister(sfr,PSW,"PSW",Register.FAMILY_PSW,8));
		RDPL= addRegister(new MemoryRegister(sfr,DPL,"DPL",Register.FAMILY_INDEX,8));
		RDPH= addRegister(new MemoryRegister(sfr,DPH,"DPH",Register.FAMILY_INDEX,8));
		RDPTR = addRegister(new PairRegister("DPTR",RDPL,RDPH));
			  
		for (int i = 0 ; i < 8 ; i ++)
		{
			R[i] = addRegister(new Register8051(i));
		}

		// Internal memory
		for (int i = 0 ; i < 128 ; i++)
			setSfrBitmap(i,0x20 + i / 8);

		// Sfr register
		for (int i = 128 ; i < 256 ; i++)
		{
			setSfrBitmap(i,i & 0xf8);
		}


	}

	/**
	 * Set the name for DATA address space.
	 */
	protected  void setDataName(int r,String name)
	{
		data.setMemoryName(r,name);
	}

	/**
	 * Return a name for DATA address space.
	 */
	protected String getDataName(int r)
	{
		return data.getMemoryName(r);
	}

	/**
	 * Set a name for SFR address space.
	 */
	protected void setSfrName(int address,String name)
	{
		sfr.setMemoryName(address,name);
	}

	/**
	 * Return a name for SFR address space
	 */
	protected String getSfrName(int address)
	{
		return sfr.getMemoryName(address);
	}
	
	/**
	 * Initialize the memories.
	 */
	protected void initMemories()
	{
		Memory m ;

		m = getMemoryForName(CODE_MEMORY);

		/**
		 * Default code size 64 KB
		 */
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(CODE_MEMORY,0x10000));
		setMemory(m);
		
		m = getMemoryForName(XDATA_MEMORY);
		
		/**
		 * Default XData size 64 KB
		 */
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(XDATA_MEMORY,0x10000));
		setXdata(m);
		
		m = getMemoryForName(DATA_MEMORY);
		
		/**
		 * Default data size 128 byte
		 */
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(DATA_MEMORY,128));
		setData(m);
		
		m = getMemoryForName(SFR_MEMORY);
		
		/**
		 * For performance reason the SFR memory is
		 * created 256 bytes long. But only the upper
		 * 128 bytes are used.
		 */
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(SFR_MEMORY,256));
		setSfr(m);
		setIO(m);
		
	}

	protected void initNames()
	{
		/*
		 * SFR register name
		 */
		setSfrName(ACC,	"ACC");
		setSfrName(B,	"B");
		setSfrName(PSW,	"PSW");
		setSfrName(SP,	"SP");
		setSfrName(DPL,	"DPL");
		setSfrName(DPH,	"DPH");
		setSfrName(P0,	"P0");
		setSfrName(P0M1,"P0M1");
		setSfrName(P0M2,"P0M2");
		setSfrName(P1,	"P1");
		setSfrName(P1M1,"P1M1");
		setSfrName(P1M2,"P1M2");
		setSfrName(P2,	"P2");
		setSfrName(P2M1,"P2M1");
		setSfrName(P2M2,"P2M2");
		setSfrName(P3,	"P3");
		setSfrName(P3M1,"P3M1");
		setSfrName(P3M2,"P3M2");
		setSfrName(SCON,"SCON");
		setSfrName(SBUF,"SBUF");
		setSfrName(TCON,"TCON");
		setSfrName(TMOD,"TMOD");
		setSfrName(TH0,"TH0");
		setSfrName(TL0,"TL0");
		setSfrName(TH1,"TH1");
		setSfrName(TL1,"TL1");
		setSfrName(IE, "IE");

		for (int i = 0 ; i < 8 ;i++)
			setDataName(i,"R"+i);

		setBitName(SCON+0,"RI");
		setBitName(SCON+1,"TI");
		setBitName(SCON+2,"RB8");
		setBitName(SCON+3,"TB8");
		setBitName(SCON+4,"REN");
		setBitName(SCON+5,"SM2");
		setBitName(SCON+6,"SM1");
		setBitName(SCON+7,"SM0");

		setBitName(TCON+7,"TF1");
		setBitName(TCON+6,"TR1");
		setBitName(TCON+5,"TF0");
		setBitName(TCON+4,"TR0");

		setBitName(IE+7,"EA");
		setBitName(IE+6,"EC");
		setBitName(IE+5,"ET2");
		setBitName(IE+4,"ES");
		setBitName(IE+3,"ET1");
		setBitName(IE+2,"EX1");
		setBitName(IE+1,"ET0");
		setBitName(IE+0,"EX0");


		setBitName(PSW+7,"CY");
		setBitName(PSW+6,"AC");
		setBitName(PSW+5,"F0");
		setBitName(PSW+4,"RS1");
		setBitName(PSW+3,"RS0");
		setBitName(PSW+2,"OV");
		setBitName(PSW+1,"F1");
		setBitName(PSW+0,"P");


		/* Set AUXR1 if enabled */
		if (auxrDptr)
			setSfrName(AUXR1,"AUXR1");

	}

	
	protected void initOpcodeDecoder()
	{
		addDecoder(new AbstractDecoder("%bit",1)
		{
			protected String implDecode(CPU cpu,CpuRuntime r,int startPc,int len,int currentPc) throws SIMException
			{
				return getBitName(code(currentPc+1));
			}
		});
		
		addDecoder(new AbstractDecoder("%ri",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime r,int startPc,int len,int currentPc) throws SIMException
			{
				return "@R"+(code(startPc) & 1);
			}
		});

		addDecoder(new AbstractDecoder("%reg",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime r,int startPc,int len,int currentPc) throws SIMException
			{
				return "R"+(code(startPc) & 7);
			}
		});

		addDecoder(new AbstractDecoder("%direct",1)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				int r = code(currentPc+1);
				return getDirectName(r);
					
			}
		});


	}

	/**
	 * Initialize peripherals.
	 * <p>
	 */
	protected void initPeripherals() throws SIMException
	{
		if (getHardware(Ports.class) == null)
			addHardware(new Ports(4));


		if (getHardware(Timer.class) == null)
			addHardware(new Timer());
		
		if (getHardware(Serial.class) == null)
		{
			Serial s = new Serial();
			jmce.sim.terminal.Terminal t = jmce.sim.terminal.Terminal.createTerminal();
			s.addHardware(t);
			s.setConnected(t);
			addHardware(s);
		}

		initListeners();
			
	}

	/**
	 * Add all required memory listeners
	 * @since 1.02
	 */
	protected void initListeners() throws SIMException
	{
		if (auxrDptr)
		{
			addIOWriteListener(AUXR1,new MemoryWriteListener()
			{
				public void	writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
				{
					setDptrIndex(value & 0x01);

				}
			});

			addIOReadListener(AUXR1,new MemoryReadListener()
			{
				public int readMemory(Memory memory,int address,int value) throws SIMException
				{
					/* Bit 1 is always 0 */
					return value & 0xfd;
				}

			});

		}

	}
	
	public final boolean isInterruptEnabled()
	{
		return interruptEnabled && !interruptInUse;
	}

	public void init(Hardware parent) throws SIMException
	{
		initMemories();
		initPeripherals();
		initRegisters();
		initOpcodes();
		initOpcodeDecoder();
		initNames();
		super.init(parent);

		/**
		 * Add memory listener to check interrupt enabled flag
		 */
		addIOWriteListener(IE,new MemoryWriteListener()
		{
			public void writeMemory(Memory m,int a,int v,int oldValue) 
			{
				interruptEnabled = (v & IE_EA) != 0;
			}

		});
	}

	/**
	 * Reset  bits in the the SFR memory.
	 *
	 * @param sfr - Address.
	 * @param mask - Bit to reset.
	 */
	public void sfrReset(int sfr,int mask) throws SIMException
	{
		this.sfr.clrBit(sfr,mask);
	}

	/**
	 * Set bit in the SFR MEMORY.
	 * 
	 * @param sfr - Address.
	 * @param mask - Bit(s) to set.
	 */
	public void sfrSet(int sfr,int mask) throws SIMException
	{
		this.sfr.setBit(sfr,mask);
	}

	public OpenCollectorMemoryBit getSfrBitOpenCollector(int address,int bit)
	{
		return new OpenCollectorMemoryBit(sfr,address,bit);
	}
	
	public MemoryBit getSfrBit(int address,int bit)
	{
		return new MemoryBit(sfr,address,bit);
	}

	/**
	 * Add a memory read listener to the SFR memory
	 *
	 * @deprecated Use addIOReadListener
	 */
	@Deprecated
	public void addSfrReadListener(int a,MemoryReadListener l)
	{
		sfr.addMemoryReadListener(a,l);
	}

	/**
	 * Add a memory write listener to the SFR memory.
	 *
	 * @deprecated Use addIOWriteListener
	 */
	@Deprecated
	public void addSfrWriteListener(int address,MemoryWriteListener l)
	{
		sfr.addMemoryWriteListener(address,l);
	}
	
	public final void pc(int pc) throws SIMException
	{
		this.pc = pc;
	}

	public final int pc() throws SIMException
	{
		return pc;
	}


	protected final int r(int r) throws SIMException
	{
		return data.getMemory((sfr.getMemory(PSW) & 0x18) + r);
	}

	protected final void r(int r,int n) throws SIMException
	{
		data.setMemory((sfr.getMemory(PSW) & 0x18) + r,n);
	}
	
	public void code(int addr,int value) throws SIMException
	{
		memory.setMemory(addr,value);
	}

	/**
	 * Return the value of the accumulator.
	 */
	protected final int acc() throws SIMException
	{
		return sfr.getMemory(ACC);
	}

	/**
	 * Set the accumulator.
	 */
	protected final void acc(int value) throws SIMException
	{
		sfr.setMemory(ACC,value);
	}

	/**
	 * Return the B register
	 */
	private final int b() throws SIMException
	{
		return sfr.getMemory(B);
	}

	/**
	 * Set the B register.
	 */
	private final void b(int value) throws SIMException
	{
		sfr.setMemory(B,value);
	}

	/**
	 * Return the current DPTR.
	 */
	private final int dptr() throws SIMException
	{
		return sfr.getMemory(DPH) << 8 | sfr.getMemory(DPL);
	}

	/**
	 * Set the DPTR.
	 */
	private final void dptr(int value) throws SIMException
	{
		sfr.setMemory(DPH,value >>> 8);
		sfr.setMemory(DPL,value);
	}

	/**
	 * Return the PSW.
	 */
	public final int psw() throws SIMException
	{
		return sfr.getMemory(PSW);
	}

	/**
	 * Set the PSW.
	 */
	public final void psw(int value) throws SIMException
	{
		sfr.setMemory(PSW,value);

	}

	public final void cy(boolean value) throws SIMException
	{
		if (value)
			sfr.setBit(PSW,PSW_CY);
		else
			sfr.clrBit(PSW,PSW_CY);

	}

	public final boolean cy() throws SIMException
	{
		return sfr.isBit(PSW,PSW_CY);
	}

	public final void ac(boolean value) throws SIMException
	{
		if (value)
			pswSet(PSW_AC);
		else
			pswReset(PSW_AC);

	}

	public final boolean ac() throws SIMException
	{
		return ((psw() & PSW_AC) != 0 ? true : false);
	}

	public final void ov(boolean value) throws SIMException
	{
		if (value)
			pswSet(PSW_OV);
		else
			pswReset(PSW_OV);

	}

	public final boolean ov() throws SIMException
	{
		return ((psw() & PSW_OV) != 0 ? true : false);
	}

	private final void pswSet(int value) throws SIMException
	{
		psw(psw() | value);
	}

	private final void pswReset(int value) throws SIMException
	{
		psw(psw() &  ~value);
	}


	/**
	 * Return byte from the code memory.
	 */
	public final int code(int addr) throws SIMException
	{
		return memory.getMemory(addr);

	}

	/**
	 * Return a word from the code memory.
	 */
	public final int code16(int addr) throws SIMException
	{
		return (memory.getMemory(addr) << 8) | memory.getMemory(addr+1);
	}

	public final int xdata(int add) throws SIMException
	{
		return xdata.getMemory(add);
	}

	public final void xdata(int add,int value) throws SIMException
	{
		xdata.setMemory(add,value);
	}

	public final int data(int add) throws SIMException
	{
		return data.getMemory(add);
	}

	public final void data(int add,int value) throws SIMException
	{
		data.setMemory(add,value);
	}



	/**
	 * Pop a word from the stack.
	 */
	public final int popw() throws SIMException
	{
		int sp = sfr.getMemory(SP);
		int value = data(sp) << 8;

		if (--sp < 0)
			throw new CPUException(this,"Stack underflow at "+RPC.hexValue());
		value |= data(sp);

		if (--sp < 0)
			throw new CPUException(this,"Stack underflow at "+RPC.hexValue());
		sfr.setMemory(SP,sp);
		
		return value;

	}

	/**
	 * Pop a byte from the stack
	 */
	public final int pop() throws SIMException
	{
		int sp = sfr.getMemory(SP);
		int value = data(sp);
		if (--sp < 0)
			throw new CPUException(this,"Stack underflow at "+RPC.hexValue());
		sfr.setMemory(SP,sp);

		return value;
	}

	/**
	 * Push a word in to the stack.
	 */
	public final void pushw(int value) throws SIMException
	{
		int sp = sfr.getMemory(SP);
		if (++sp > 255)
			throw new CPUException(this,"Stack overflow at "+RPC.hexValue());
		data(sp,value);
		if (++sp > 255)
			throw new CPUException(this,"Stack overflow at "+RPC.hexValue());
		data(sp,value >> 8);
		sfr.setMemory(SP,sp);
	}

	/**
	 * Push a byte in to the stack.
	 */
	public final void push(int value) throws SIMException
	{
		int sp = sfr.getMemory(SP);
		if (++sp > 255)
			throw new CPUException(this,"Stack overflow at "+RPC.hexValue());
		data(sp,value);
		sfr.setMemory(SP,sp);
	}

	

	
	/**x
	 * MCS51 Opcode
	 */
	class ACALL extends AbstractOpcode
	{

		public ACALL(int opcode)
		{
			super(opcode,2,2,"ACALL\t%data12");
		}

		protected ACALL(int opcode,String name)
		{
			super(opcode,2,2,name);
		}

		protected int getAddress(int pc) throws SIMException
		{
			int add = code(pc+1) | ((opcode << 3) & 0x700);
			add |= (pc + 2 ) & 0xF800;
			return add;
		}

		public int exec(int pc) throws SIMException
		{
			int address = getAddress(pc);

			CallListener l = getCallListener(address);
			
			if (l != null)
				return l.call(MCS51.this,address);
			else
			{
				pushw(pc+2);
				pc(getAddress(pc));

				return times;
			}
		}

	}


	class AJMP extends ACALL
	{
		public AJMP(int opcode)
		{
			super(opcode,"AJMP\t%data12");
		}


		public int exec(int pc) throws SIMException
		{

			pc(getAddress(pc));
			return times;
		}


	}

	interface ArithmeticOperation
	{
		public void calc(int value) throws SIMException;
	}


	abstract class Arithmetic extends AbstractOpcode
	{
		ArithmeticOperation operation;

		public Arithmetic(int opcode,int length,ArithmeticOperation op,String name)
		{
			super(opcode,length,1,name);
			this.operation = op;
		}

		public int exec(int pc) throws SIMException
		{
			operation.calc(getValue(pc));
			return times;
		}

		abstract int getValue(int pc) throws SIMException;
	}



	class ArithmeticADD implements ArithmeticOperation
	{
		protected int result;

		protected boolean op(int acc,int value,int c,int mask)
		{
			result = (acc & mask) + (value & mask) + c;
			return (result & (mask + 1)) != 0 ? true : false;
		}

		protected void add(int value,int c) throws SIMException
		{
			int acc = acc();
			ac(op(acc,value,c,0x0f));
			boolean cy7 = op(acc,value,c,0x7F);
			cy(op(acc,value,c,0xff));
			ov(cy() != cy7);
			acc(result);
		}

		public void calc(int value) throws SIMException
		{
			add(value,0);
		}

	}

	class ArithmeticADDC extends ArithmeticADD
	{


		public void calc(int value) throws SIMException
		{
			int c = cy() ? 1 : 0;
			add(value,c);
		}

	}


	class ArithmeticANL implements ArithmeticOperation
	{
		public void calc(int value) throws SIMException
		{
			acc(acc() & value);
		}
	}

	class ArithmeticORL implements ArithmeticOperation
	{
		public void calc(int value) throws SIMException
		{
			acc(acc() | value);
		}
	}

	class ArithmeticXRL implements ArithmeticOperation
	{
		public void calc(int value) throws SIMException
		{
			acc(acc() ^ value);
		}
	}




	class ArithmeticSUBB extends ArithmeticADDC
	{

		protected boolean op(int acc,int value,int c,int mask)
		{
			result = (acc & mask) - (value & mask) - c;
			return (result & (mask + 1)) != 0 ? true : false;
		}


	}


	class ANL_DIRECT_A extends AbstractOpcode
	{
		ANL_DIRECT_A()
		{
			super(0x52,2,1,"ANL\tA,%direct");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,getDirect(add) & acc());
			
			return times;
		}

	}

	class ANL_DIRECT_DATA extends AbstractOpcode
	{
		ANL_DIRECT_DATA()
		{
			super(0x53,3,2,"ANL\t%direct,#%byte");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,(getDirect(add) & code(pc+2)));
			return times;
		}


	}


	class ANL_C_DIRECT extends AbstractOpcode
	{
		ANL_C_DIRECT()
		{
			super(0x82,2,2,"ANL\tC,%bit");
		}

		public int exec(int pc) throws SIMException
		{
			cy(getBit(code(pc+1)) & cy());
			return times;
		}

	}


	class ANL_C_NOT_DIRECT extends AbstractOpcode
	{
		ANL_C_NOT_DIRECT()
		{
			super(0xb0,2,2,"ANL\tC,NOT %bit");
		}

		public int exec(int pc) throws SIMException
		{
			cy(!(getBit(code(pc+1)) & cy()));
			return times;
		}


	}

	abstract class JR extends AbstractOpcode
	{

		JR(int opcode,int len,int cycle,String desc)
		{
			super(opcode,len,cycle,desc);
		}

		protected final void jr(int pc,int offset) throws SIMException
		{
			/*
			if (offset < 128)
				pc(pc + offset + length);
			else
				pc(pc + length - ( 0x100 - offset));
			*/
			pc(pc + length + (offset - ((offset & 128) << 1)));

		}
	}


	class SJMP extends JR
	{
		SJMP()
		{
			super(0x80,2,2,"SJMP\t%offset");
		}

		public int exec(int pc) throws SIMException
		{
			jr(pc,code(pc+1));
			return times;
		}
	}


	
	abstract class CJNE extends JR
	{
		CJNE(int opcode,int len,int cycle,String desc)
		{
			super(opcode,len,cycle,desc);
		}

		protected void cjne(int pc,int op1,int op2,int offset) throws SIMException
		{

			if (op1 < op2)
			{
				cy(true);
			}
			else
			{
				cy(false);
			}

			if (op1 != op2)
				jr(pc,offset);
		}


	}


	class CJNE_A_DIRECT extends CJNE
	{
		CJNE_A_DIRECT()
		{
			super(0xb5,3,2,"CJNE\tA,%direct,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			cjne(pc,acc(),getDirectCode(pc+1),code(pc+2));
			return times;
		}
	}

	class CJNE_A_DATA extends CJNE
	{
		CJNE_A_DATA()
		{
			super(0xb4,3,2,"CJNE\tA,#%byte,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			cjne(pc,acc(),code(pc+1),code(pc+2));
			return times;
		}
	}

	class CJNE_R_DATA extends CJNE
	{
		CJNE_R_DATA(int r)
		{
			super(0xb8+r,3,2,"CJNE\t%reg,#%byte,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			cjne(pc,r((opcode & 7)),code(pc+1),code(pc+2));
			return times;
		}

	}


	class DEC_R extends AbstractOpcode
	{
		public DEC_R(int r)
		{
			super(0x18|r,1,1,"DEC\t%reg");
		}

		public int exec(int pc) throws SIMException
		{
			int r = (opcode & 7);
			r(r,(r(r) - 1));
			return times;
		}

	}

	abstract class DJNZ extends JR
	{
		DJNZ(int opcode,int len,int cycle,String desc)
		{
			super(opcode,len,cycle,desc);
		}

		protected void jnz(int pc,int value) throws SIMException
		{
			value &= 0xff;
			if (value != 0)
				jr(pc,code(pc+getLength() - 1));
		}
	}
	
	class DJNZ_R extends DJNZ
	{
		DJNZ_R(int r)
		{
			super(0xd8|r,2,2,"DJNZ\t%reg");
		}

		public int exec(int pc) throws SIMException
		{
			int r = (opcode & 7);
			int value = (r(r) - 1);
			r(r,value);
			jnz(pc,value);
			return times;
		}


	}


	class INC_R extends AbstractOpcode
	{
		INC_R(int r)
		{
			super(8|r,1,1,"INC\t%reg");
		}

		public int exec(int pc) throws SIMException
		{
			int r = (opcode & 7);
			r(r,(r(r)+1));
			return times;
		}


	}


	class MOV_A_R extends AbstractOpcode
	{
		public MOV_A_R(int r)
		{
			super(0xe8|r,1,1,"MOV\tA,%reg");
		}

		public int exec(int pc) throws SIMException
		{
			acc(r((opcode & 7)));
			return times;
		}


	}

	class MOV_R_A extends AbstractOpcode
	{
		public MOV_R_A(int r)
		{
			super(0xf8|r,1,1,"MOV\t%reg,A");
		}

		public int exec(int pc) throws SIMException
		{
			r((opcode & 7),acc());
			return times;
		}


	}

	class MOV_R_DIRECT extends AbstractOpcode
	{
		public MOV_R_DIRECT(int r)
		{
			super(0xa8|r,2,2,"MOV\t%reg,%direct");
		}

		public int exec(int pc) throws SIMException
		{
			r((opcode & 7),getDirectCode(pc+1));
			return times;
		}


	}

	class MOV_R_DATA extends AbstractOpcode
	{
		public MOV_R_DATA(int r)
		{
			super(0x78|r,2,1,"MOV\t%reg,%byte");
		}

		public int exec(int pc) throws SIMException
		{
			r((opcode & 7),code(pc+1));
			return times;
		}


	}

	class MOV_DIRECT_R extends AbstractOpcode
	{
		public MOV_DIRECT_R(int r)
		{
			super(0x88|r,2,2,"MOV\t%direct,%reg");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,r((opcode & 7)));
			return times;
		}


	}

	class XCH_A_R extends AbstractOpcode
	{
		public XCH_A_R(int r)
		{
			super(0xc8|r,1,1,"XCH\tA,%reg");
		}

		public int exec(int pc) throws SIMException
		{
			int r = (opcode & 7);
			int tmp = acc();
			acc(r(r));
			r(r,tmp);
			return times;
		}

	}

	class CJNE_RI_DATA extends CJNE
	{
		CJNE_RI_DATA(int r)
		{
			super(0xb6+r,3,2,"CJNE\t%ri,%byte,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			cjne(pc,data(r((opcode & 1))),code(pc+1),code(pc+2));
			return times;
		}


	}

	class DEC_RI extends AbstractOpcode
	{
		public DEC_RI(int r)
		{
			super(0x16|r,1,1,"DEC\t%ri");
		}

		public int exec(int pc) throws SIMException
		{
			int address = r((opcode & 1));
			data(address,data(address ) -1 );
			return times;
		}


	}

	class INC_RI extends AbstractOpcode
	{
		INC_RI(int r)
		{
			super(6|r,1,1,"INC\t%ri");
		}

		public int exec(int pc) throws SIMException
		{
			int i = r((opcode & 1));
			setDirect(i,(getDirect(i) + 1));
			return times;
		}

	}

	class MOV_A_RI extends AbstractOpcode
	{
		public MOV_A_RI(int r)
		{
			super(0xe6|r,1,1,"MOV\tA,%ri");
		}

		public int exec(int pc) throws SIMException
		{
			acc(data(r((opcode & 1))));
			return times;
		}


	}

	class MOV_RI_A extends AbstractOpcode
	{
		public MOV_RI_A(int r)
		{
			super(0xf6|r,1,1,"MOV\t%ri,A");
		}

		public int exec(int pc) throws SIMException
		{
			int add = r((opcode & 1));
			data(add,acc());
			return times;
		}


	}

	class MOV_DIRECT_RI extends AbstractOpcode
	{
		public MOV_DIRECT_RI(int r)
		{
			super(0x86|r,2,2,"MOV\t%direct,%ri");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,data(r((opcode & 1))));
			return times;
		}


	}

	class MOV_RI_DATA extends AbstractOpcode
	{
		public MOV_RI_DATA(int r)
		{
			super(0x76|r,2,1,"MOV\t%ri,%byte");
		}

		public int exec(int pc) throws SIMException
		{
			int add = r((opcode & 1));
			data(add,(code(pc+1)));
			return times;
		}


	}


	class MOV_RI_DIRECT extends AbstractOpcode
	{
		public MOV_RI_DIRECT(int r)
		{
			super(0xa6|r,2,2,"MOV\t%ri,%direct");
		}

		public int exec(int pc) throws SIMException
		{
			int add = r((opcode & 1));
			data(add,getDirectCode(pc+1));
			return times;
		}

	}


	class MOVX_A_RI extends AbstractOpcode
	{
		public MOVX_A_RI(int r)
		{
			super(0xe2|r,1,2,"MOVX\tA,%ri");
		}

		public int exec(int pc) throws SIMException 
		{
			int offset = getXdataHi() << 8;
			offset += r(opcode & 1);
			acc(xdata(offset));
			return times;
		}

	}

	class MOVX_RI_A extends AbstractOpcode
	{
		public MOVX_RI_A(int r)
		{
			super(0xf2|r,1,2,"MOVX\t%ri,A");
		}

		public int exec(int pc) throws SIMException 
		{
			int offset = getXdataHi() << 8;

			offset += r(opcode & 1);
			xdata(offset,acc());

			return times;
		}


	}

	class XCH_A_RI extends AbstractOpcode
	{
		public XCH_A_RI(int r)
		{
			super(0xc6|r,1,1,"XCH\tA,%ri");
		}

		public int exec(int pc) throws SIMException
		{
			int r = r((opcode & 1));
			int tmp = acc();
			acc(data(r));
			data(r,tmp);
			return times;
		}


	}

	class XCHD_A_RI extends AbstractOpcode
	{
		public XCHD_A_RI(int r)
		{
			super(0xd6|r,1,1,"XCHD\tA,%ri");
		}

		public int exec(int pc) throws SIMException
		{
			int r = r((opcode & 1));
			int tmp = acc();
			acc(((acc() & 0xf0) | (data(r) & 0x0f)));
			data(r,((data(r) & 0xf0) | (tmp) & 0x0f));
			return times;
		}


	}

	class XRL_DIRECT_A extends AbstractOpcode
	{
		XRL_DIRECT_A()
		{
			super(0x62,2,1,"XRL\t%direct,A");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,(getDirect(add) ^ acc()));
			return times;
		}
	}

	class XRL_DIRECT_DATA extends AbstractOpcode
	{
		XRL_DIRECT_DATA()
		{
			super(0x63,3,2,"XRL\t%direct,#%byte");
		}


		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,(getDirect(add) ^ code(pc+2)));
			return times;
		}
	}

	class ORL_DIRECT_A extends AbstractOpcode
	{
		ORL_DIRECT_A()
		{
			super(0x42,2,1,"ORL\t%direct,A");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,(getDirect(add) | acc()));
			return times;
		}
	}

	class ORL_DIRECT_DATA extends AbstractOpcode
	{
		ORL_DIRECT_DATA()
		{
			super(0x43,3,2,"ORL\t%direct,#%byte");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,(getDirect(add) | code(pc+2)));
			return times;
		}
	}

	class CLR_A extends AbstractOpcode
	{
		public CLR_A()
		{
			super(0xe4,1,1,"CLR\tA");
		}

		public int exec(int pc) throws SIMException
		{
			acc(0);
			return times;
		}
	}

	class CLR_C extends AbstractOpcode
	{
		public CLR_C()
		{
			super(0xc3,1,1,"CLR\tC");
		}

		public int exec(int pc) throws SIMException
		{
			cy(false);
			return times;
		}
	}

	class CLR_BIT extends AbstractOpcode
	{
		public CLR_BIT()
		{
			super(0xc2,2,1,"CLR\t%bit");
		}

		public int exec(int pc) throws SIMException
		{
			setBit(code(pc+1),false);
			return times;
		}
	}

	class CPL_A extends AbstractOpcode
	{
		public CPL_A()
		{
			super(0xf4,1,1,"CPL\tA");
		}

		public int exec(int pc) throws SIMException
		{
			acc(~acc());
			return times;
		}
	}


	class CPL_C extends AbstractOpcode
	{
		public CPL_C()
		{
			super(0xb3,1,1,"CPL\tC");
		}

		public int exec(int pc) throws SIMException
		{
			cy(!cy());
			return times;
		}
	}

	class CPL_BIT extends AbstractOpcode
	{
		public CPL_BIT()
		{
			super(0xb2,2,1,"CPL\t%bit");
		}

		public int exec(int pc) throws SIMException
		{
			int bit = code(pc+1);
			setBit(bit,!getBit(bit));
			return times;
		}
	}

	class DA_A extends AbstractOpcode
	{
		public DA_A()
		{
			super(0xd4,1,1,"DA\tA");
		}

		public int exec(int pc) throws SIMException
		{
			int a = acc();

			if ((a & 0x0f)	> 9 || ac())
			{
				a += 6;
				if ((a & 0xf0) != (acc() & 0xf0))
					cy(true);
			}

			if ((a & 0xf0) > 0x90 || cy())
			{
				a += 0x60;
				if (a  > 255)
					cy(true);
			}

			acc(a);

			return times;
		}

	}

	class XCH_A_DIRECT extends AbstractOpcode
	{
		public XCH_A_DIRECT()
		{
			super(0xc5,2,1,"XCH\tA,%direct");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			int tmp = acc();
			acc(getDirect(add));
			setDirect(add,tmp);
			return times;
		}

	}

	class SWAP_A extends AbstractOpcode
	{
		public SWAP_A()
		{
			super(0xc4,1,1,"SWAP\tA");
		}

		public int exec(int pc) throws SIMException
		{
			int a = acc();
			acc(((a >> 4) & 0x0f | (a << 4)));
			return times;
		}

	}

	class DEC_A extends AbstractOpcode
	{
		public DEC_A()
		{
			super(0x14,1,1,"DEC\tA");
		}

		public int exec(int pc) throws SIMException
		{
			acc((acc() - 1));
			return times;
		}

	}


	class DEC_DIRECT extends AbstractOpcode
	{
		public DEC_DIRECT()
		{
			super(0x15,2,1,"DEC\t%direct");
		}

		public int exec(int pc) throws SIMException
		{
			int direct = code(pc+1);
			setDirect(direct,(getDirect(direct) - 1));
			return times;
		}

	}

	class DIV_AB extends AbstractOpcode
	{
		public DIV_AB()
		{
			super(0x84,1,4,"DIV\tAB");
		}

		public int exec(int pc) throws SIMException
		{
			int a,b;

			a = acc();
			b = b();

			acc((a/b));
			b((a % b));
			cy(false);
			ov(false);
			return times;
		}
	}

	class MUL_AB extends AbstractOpcode
	{
		public MUL_AB()
		{
			super(0xa4,1,4,"MUL\tAB");
		}

		public int exec(int pc) throws SIMException
		{
			int value = acc() * b();

			b((value >> 8));
			acc(value);
			
			/** Bug fixed thank you to Dmitri Danilki */
			cy(false);
			
			ov(value > 255 ? true : false);
			return times;
		}
	}


	class DJNZ_DIRECT extends DJNZ
	{
		DJNZ_DIRECT()
		{
			super(0xd5,3,2,"DJNZ\t%direct,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			int address = code(pc+1);
			int value = getDirect(address) - 1;
			setDirect(address,value);
			jnz(pc,value);
			return times;
		}
	}

	class INC_A extends AbstractOpcode
	{
		INC_A()
		{
			super(4,1,1,"INC\tA");
		}

		public int exec(int pc) throws SIMException
		{
			acc((acc()+1));
			return times;
		}

	}

	class INC_DIRECT extends AbstractOpcode
	{
		INC_DIRECT()
		{
			super(5,2,1,"INC\t%direct");
		}

		public int exec(int pc) throws SIMException
		{
			int a = code(pc+1);
			setDirect(a,(getDirect(a)+1));
			return times;
		}

	}

	class INC_DPTR extends AbstractOpcode
	{
		INC_DPTR()
		{
			super(0xa3,1,2,"INC\tDPTR");
		}

		public int exec(int pc) throws SIMException
		{
			dptr(dptr()+1);
			return times;
		}

	}

	class JB extends JR
	{
		JB()
		{
			super(0x20,3,2,"JB\t%bit,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			if (getBitCode(pc+1))
				jr(pc,code(pc+2));
			return times;
		}
	}

	class JBC extends JR
	{
		JBC()
		{
			super(0x10,3,2,"JBC\t%bit,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			if (getBit(add))
			{
				setBit(add,false);
				jr(pc,code(pc+2));
			}
			return times;
		}
	}

	class JNB extends JR
	{
		JNB()
		{
			super(0x30,3,2,"JNB\t%bit,%offset");
		}

		public int exec(int pc) throws SIMException
		{
			boolean bit = getBitCode(pc+1);

			if (!bit)
				jr(pc,code(pc+2));
			return times;
		}
	}

	class JNC extends JR
	{
		JNC()
		{
			super(0x50,2,2,"JNC\t%offset");
		}

		public int exec(int pc) throws SIMException
		{
			if (!cy())
				jr(pc,code(pc+1));
			return times;
		}
	}


	class JC extends JR
	{
		JC()
		{
			super(0x40,2,2,"JC\t%offset");
		}

		public int exec(int pc) throws SIMException
		{

			if (cy())
				jr(pc,code(pc+1));
			return times;
		}
	}

	class JNZ extends JR
	{
		JNZ()
		{
			super(0x70,2,2,"JNZ\t%offset");
		}

		public int exec(int pc) throws SIMException
		{
			if (acc() != 0)
				jr(pc,code(pc+1));
			return times;
		}
	}


	class JZ extends JR
	{
		JZ()
		{
			super(0x60,2,2,"JZ\t%offset");
		}

		public int exec(int pc) throws SIMException
		{
			if (acc() == 0)
				jr(pc,code(pc+1));
			return times;
		}
	}


	class JMP_A_DPTR extends AbstractOpcode
	{
		JMP_A_DPTR()
		{
			super(0x73,1,2,"JMP\t@A+DPTR");
		}

		public int exec(int pc) throws SIMException
		{
			pc(dptr()+acc());
			return times;
		}
	}



	class LCALL extends AbstractOpcode
	{

		LCALL()
		{
			super(0x12,3,2,"LCALL\t%word");
		}

		public int exec(int pc) throws SIMException 
		{
			int address = code16(pc+1);

			CallListener l = getCallListener(address);

			if (l != null)
				return l.call(MCS51.this,address);
			else
			{
				pushw(pc+3);
				pc(address);
			
				return times;
			}
		}

	}

	class LJMP extends AbstractOpcode
	{
		LJMP()
		{
			super(0x2,3,2,"LJMP\t%word");
		}

		public int exec(int pc) throws SIMException
		{
			pc(code16(pc+1));
			//pc((code(pc+1) << 8) | code(pc+2));
			return times;
		}

	}


	class MOV_DPTR_DATA16 extends AbstractOpcode
	{
		public MOV_DPTR_DATA16()
		{
			super(0x90,3,2,"MOV\tDPTR,%word");
		}

		public int exec(int pc) throws SIMException
		{
			dptr((code(pc+1) << 8) | code(pc+2));
			return times;
		}
	}

	class POP_DIRECT extends AbstractOpcode
	{
		public POP_DIRECT()
		{
			super(0xd0,2,2,"POP\t%direct");
		}

		public int exec(int pc) throws SIMException 
		{
			int add = code(pc+1);
			setDirect(add,pop());
			return times;
		}
	}

	class PUSH_DIRECT extends AbstractOpcode
	{
		public PUSH_DIRECT()
		{
			super(0xc0,2,2,"PUSH\t%direct");
		}

		public int exec(int pc) throws SIMException 
		{
			push(getDirectCode(pc+1));
			return times;
		}
	}

	class RET extends AbstractOpcode
	{
		public RET()
		{
			super(0x22,1,2,"RET");
		}

		public int exec(int pc) throws SIMException 
		{
			pc(popw());
			return times;
		}
	}

	class RETI extends AbstractOpcode
	{
		public RETI()
		{
			super(0x32,1,2,"RETI");
		}

		public int exec(int pc) throws SIMException 
		{
			pc(popw());
			interruptInUse = false;
			return times;
		}
	}

	class RL_A extends AbstractOpcode
	{
		public RL_A()
		{
			super(0x23,1,1,"RL\tA");
		}

		public int exec(int pc) throws SIMException
		{
			int a = acc();

			a = a << 1;
			if ((acc() & 0x80) != 0)
				a |= 1;
			acc(a);
			return times;
		}
	}

	class RR_A extends AbstractOpcode
	{
		public RR_A()
		{
			super(0x3,1,1,"RR\tA");
		}

		public int exec(int pc) throws SIMException
		{
			int a = acc();

			a = a >> 1;
			if ((acc() & 0x01) != 0)
				a |= 0x80;
			acc(a);
			return times;
		}
	}


	class SETB_BIT extends AbstractOpcode
	{
		public SETB_BIT()
		{
			super(0xd2,2,1,"SETB\t%bit");
		}

		public int exec(int pc) throws SIMException
		{
			setBit(code(pc+1),true);
			return times;
		}
	}

	class SETB_C extends AbstractOpcode
	{
		public SETB_C()
		{
			super(0xd3,1,1,"SETB\tC");
		}

		public int exec(int pc) throws SIMException
		{
			cy(true);
			return times;
		}
	}


	class RRC_A extends AbstractOpcode
	{
		public RRC_A()
		{
			super(0x13,1,1,"RRC\tA");
		}

		public int exec(int pc) throws SIMException
		{
			int a = acc();

			a = a >> 1;
			if (cy())
				a |= 0x80;

			cy((acc() & 1) != 0);

			acc(a);
			return times;
		}
	}


	class RLC_A extends AbstractOpcode
	{
		public RLC_A()
		{
			super(0x33,1,1,"RLC\tA");
		}

		public int exec(int pc) throws SIMException
		{
			int a = acc();

			a = a << 1;
			if (cy())
				a |= 1;
			cy((a & 0x100) != 0) ;
			acc(a);
			return times;
		}
	}


	class ORL_C_BIT extends AbstractOpcode
	{
		public ORL_C_BIT()
		{
			super(0x72,2,2,"ORL\tC,%bit");
		}

		public int exec(int pc) throws SIMException
		{
			cy(getBitCode(pc+1)|cy());
			return times;
		}
	}

	class ORL_C_NBIT extends AbstractOpcode
	{
		public ORL_C_NBIT()
		{
			super(0xA0,2,2,"ORL\tC,NOT %bit");
		}

		public int exec(int pc) throws SIMException
		{
			cy(cy() | !getBitCode(pc+1));
			return times;
		}
	}

	class MOV_C_BIT extends AbstractOpcode
	{
		public MOV_C_BIT()
		{
			super(0xa2,2,1,"MOV\tC,%bit");
		}

		public int exec(int pc) throws SIMException
		{
			cy(getBitCode(pc+1));
			return times;
		}
	}

	class MOV_BIT_C extends AbstractOpcode
	{
		public MOV_BIT_C()
		{
			super(0x92,2,2,"MOV\t%bit,C");
		}

		public int exec(int pc) throws SIMException
		{
			setBit(code(pc+1),cy());
			return times;
		}
	}

	class MOV_DIRECT_DATA extends AbstractOpcode
	{
		public MOV_DIRECT_DATA()
		{
			super(0x75,3,2,"MOV\t%direct,#%byte");
		}

		public int exec(int pc) throws SIMException
		{
			int add = code(pc+1);
			setDirect(add,code(pc+2));
			return times;
		}
	}


	class MOV_A_DIRECT extends AbstractOpcode
	{
		public MOV_A_DIRECT()
		{
			super(0xe5,2,1,"MOV\tA,%direct");
		}

		public int exec(int pc) throws SIMException
		{
			acc(getDirectCode(pc+1));
			return times;
		}
	}

	class MOVC_A_DPTR_A extends AbstractOpcode
	{
		public MOVC_A_DPTR_A()
		{
			super(0x93,1,2,"MOVC\tA,@DPTR+A");
		}

		public int exec(int pc) throws SIMException
		{
			acc(code(dptr()+acc()));
			return times;
		}
	}


	class MOVX_A_DPTR extends AbstractOpcode
	{
		public MOVX_A_DPTR()
		{
			super(0xe0,1,2,"MOVX\tA,@DPTR");
		}

		public int exec(int pc) throws SIMException
		{
			acc(xdata(dptr()));
			return times;
		}
	}


	class MOVX_DPTR_A extends AbstractOpcode
	{
		public MOVX_DPTR_A()
		{
			super(0xf0,1,2,"MOVX\t@DPTR,A");
		}

		public int exec(int pc) throws SIMException
		{
			xdata(dptr(),acc());

			return times;
		}
	}


	class MOVC_A_PC_A extends AbstractOpcode
	{
		public MOVC_A_PC_A()
		{
			super(0x83,1,2,"MOVC\tA,@PC+A");
		}

		public int exec(int pc) throws SIMException
		{
			acc(code(pc+1+acc()));
			return times;
		}
	}

	class MOV_DIRECT_DIRECT extends AbstractOpcode
	{
		public MOV_DIRECT_DIRECT()
		{
			super(0x85,3,2,"MOV\t%1%direct,%-2%direct");
		}

		public int exec(int pc) throws SIMException
		{
			int source = code(pc+1);
			int dest = code(pc+2);
			setDirect(dest,getDirect(source));
			return times;
		}
	}

	class MOV_A_DATA extends AbstractOpcode
	{
		public MOV_A_DATA()
		{
			super(0x74,2,1,"MOV\tA,#%byte");
		}

		public int exec(int pc) throws SIMException
		{
			acc(code(pc+1));
			return times;
		}
	}

	class MOV_DIRECT_A extends AbstractOpcode
	{
		public MOV_DIRECT_A()
		{
			super(0xf5,2,1,"MOV\t%direct,A");
		}

		public int exec(int pc) throws SIMException
		{
			setDirect(code(pc+1),acc());
			return times;
		}
	}

	class NOP extends AbstractOpcode
	{
		NOP()
		{
			super(0,1,1,"NOP");
		}

		public int exec(int pc) throws SIMException
		{

			return times;
		}

	}

	public void reset() throws SIMException
	{
		super.reset();
		for (int i = 0 ; i < sfr.getSize() ; i++)
			sfr.setMemory(i,0);
		setDptrIndex(0);
		interruptInUse = false;
	}

	public boolean sfrIsBit(int add,int mask) throws SIMException
	{
		return sfr.isBit(add,mask);
	}

	public void sfrSetBit(int add,int mask) throws SIMException
	{
		sfr.setBit(add,mask);
	}

	/**
	 * Set the current DPTR index
	 */
	public final void setDptrIndex(int i) throws SIMException
	{
		if (i != dptrIndex)
		{
			dptrs[dptrIndex] = dptr();
			dptr(dptrs[i]);
			dptrIndex = i;
			log.info("Dptr index="+dptrIndex);

		}
	}

	/**
	 * Return the DPTR register with the specified index
	 */
	public final int getDptr(int i) throws SIMException
	{
		return i == dptrIndex ? dptr() : dptrs[i];
	}

	@Override
	public int fireISR(Interrupt isr) throws SIMException
	{
		interruptInUse = true;
		pushw(pc);
		pc = isr.getVector();
		
		return 2;
	}

	/**
	 * Enabled the standard AUXR1 dptr. Must be calld before
	 * initializations.
	 *
	 * @since 1.02
	 */
	protected final void setAuxrDptrEnabled()
	{
		auxrDptr = true;
	}

}
