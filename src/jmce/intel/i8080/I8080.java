/**
   $Id: I8080.java 617 2011-05-31 16:31:29Z mviara $

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
package jmce.intel.i8080;

import jmce.sim.*;
import jmce.sim.cpu.*;
import jmce.util.Hex;

import jmce.sim.memory.*;

/**
 * Intel 8080 Standard.<p>
 *
 * </h2>Required memory</h2>
 * 
 * <ul>
 *  <li> MEMORY the main memory used for code and data. See
 *  #MAIN_MEMORY<li>
 *  <li> IO the I/O memory used for IN/OUT istructions. See
 *  #IO_MEMORY</li>
 * </ul>
 *<p>
 *
 * Emulated register : A,F,BC,DE,HL,PC,SP
 * 
 *<p>
 * The 8080 inplementation is tested using the child class
 * <code>Z80</code> with Yaze ZEXDOC Z80 exerciser.
 * <p> 
 * For better rediability the mnemonics used on the disassembler are the
 * Z80 mnemonics and not the original Intel 8080.<p>
 *
 * @author Mario Viara
 * @version 1.01
 */
public class I8080 extends AbstractCPU implements I8080Constants
{
	
	/** 8080 register */
	public int A,F,BC,DE,HL,PC,SP;

	/**
	 * Interrupt enable flag register. Interrupt are enable only
	 * when the 2 flag (iff1,iff2) are both true.
	 */
	public boolean iff1,iff2;


	/**
	 * Table with PV flag on accumulator
	 */
	protected static final boolean parityTable[] = {
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		false, true, true, false, true, false, false, true, true, false, false, true, false, true, true, false,
		true, false, false, true, false, true, true, false, false, true, true, false, true, false, false, true,
	};



	/**
	 * Table calculate at runtime to speed up arithmetic / logical
	 * operation
	 */
	static private int dec8Table[] = new int[256];
	static private int inc8Table[] = new int[256];
	static private int adc8Table[][][]=new int[256][256][2];
	static private int sbc8Table[][][]=new int[256][256][2];
	static protected int booleanTable[] = new int[256];


	/** bit 8-15 during in/out operation */
	private int portHI;

	/**
	 * When the class is loaded calculate all tables used to speed
	 * up arithmetic operation.
	 */
	static
	{
		int old,value,F;

		
		/**
		 * Calculate table for flag
		 */
		for (int i = 0 ; i < 256 ; i++)
		{
			/**
			 * Calculate 8 bit decrement flags.
			 */
			old = i;
			value = i;
			F = FLAG_N;
			if ((value == 0x80))
				F |= FLAG_PV;
			value = (value - 1) & 0xff;
			if ((value & 0x80) != 0)
				F |= FLAG_S;
			if (value == 0)
				F |= FLAG_Z;
			if ((value & 0x08) != 0)
				F |= FLAG_3;
			if ((value & 0x20) != 0)
				F |= FLAG_5;

			if (((old ^ value) & 0x10) != 0)
				F |= FLAG_H;


			dec8Table[i] = F & 0xff;

			/**
			 * Calculate 8 bit increment flags.
			 */
			F = 0;
			old = i;
			value = i;

			if ((value == 0x7f))
				F |= FLAG_PV;
			value = (value + 1) & 0xff;
			if ((value & 0x80) != 0)
				F |= FLAG_S;
			if (value == 0)
				F |= FLAG_Z;
			if ((value & 0x08) != 0)
				F |= FLAG_3;
			if ((value & 0x20) != 0)
				F |= FLAG_5;

			if (((old ^ value) & 0x10) != 0)
				F |= FLAG_H;


			inc8Table[i] = F & 0xff;


			/**
			 * Calculate flag for boolean operation
			 */
			F = 0;
			if ((i & 0x80) != 0)
				F |= FLAG_S;
			if (i == 0)
				F |= FLAG_Z;
			if (parityTable[i])
				F |= FLAG_PV;

			if ((i & 0x08) != 0)
				F |= FLAG_3;
			
			if ((i & 0x20) != 0)
				F |= FLAG_5;

			booleanTable[i] = F & 0xff;

			/**
			 * Calculate flag for adc8
			 */
			for (int j = 0 ; j < 256 ; j++)
			{
				for (int c = 0 ; c < 2 ; c++)
				{
					F = 0;
					int ans = i + j + c;
					if ((ans & 0x80) != 0)
						F |= FLAG_S;
					if ((ans & 0x100) != 0)
						F |= FLAG_C;
					if ((ans & 0xff) == 0)
						F |= FLAG_Z;
					if (((i ^ ans ^ j ) & 0x10) != 0)
						F |= FLAG_H;
					if (((i ^ j ^ 0x80) & (j ^ ans) & 0x80) != 0)
						F |= FLAG_PV;
					if ((ans & 0x08) != 0)
						F |= FLAG_3;
					if ((ans & 0x20) != 0)
						F |= FLAG_5;
					
					adc8Table[ans & 0xff][i][c] = F;
					
				}
			}

			/**
			 * Calculate flag for sdc8
			 */
			for (int j = 0 ; j < 256 ; j++)
			{
				for (int c = 0 ; c < 2 ; c++)
				{
					F = FLAG_N;
					int ans = i - j - c;
					if ((ans & 0x80) != 0)
						F |= FLAG_S;
					if ((ans & 0x100) != 0)
						F |= FLAG_C;
					if ((ans & 0xff) == 0)
						F |= FLAG_Z;
					if (((i ^ ans ^ j ) & 0x10) != 0)
						F |= FLAG_H;
					if (((j ^ i) & (i ^ ans) & 0x80) != 0)
						F |= FLAG_PV;
					if ((ans & 0x08) != 0)
						F |= FLAG_3;
					if ((ans & 0x20) != 0)
						F |= FLAG_5;

					sbc8Table[ans & 0xff][i][c] = F;

				}
			}

		}
			
	};

	/**
	 * Default constructor
	 */
	public I8080()
	{
		this("I8080");
	}

	/**
	 * Costructor with specified name.
	 * <p>
	 * 
	 * Set all requested value for the intel 8080 cpu.
	 *
	 */
	public I8080(String name)
	{
		super(name);
		
		setEndian(LITTLE_ENDIAN);
		setClock(2000000);
		setClockPerCycle(1);


	}

	public final void setWord(int a,int v) throws SIMException
	{
		memory.setMemory(a+0,v);
		memory.setMemory(a+1,v >>> 8);
	}
	
	public final int getWord(int a) throws SIMException
	{
		return memory.getMemory(a+0) | (memory.getMemory(a+1) << 8);
	}

	public final void pc(int pc)
	{
		this.PC = pc;
	}

	public final int pc()
	{
		return this.PC;
	}

	/**
	 * Return the value of CF
	 */
	protected final boolean FLAG_C()
	{
		return (F & FLAG_C) != 0 ? true : false;
	}

	/**
	 * Set the value of CF
	 */
	protected final void FLAG_C(boolean value)
	{
		if (value)
			F |= FLAG_C;
		else
			F &= ~FLAG_C;
	}

	
	/**
	 * Return the statu of the ZF
	 */
	protected final boolean FLAG_Z()
	{
		return ((F & FLAG_Z) != 0) ? true : false;

	}

	/**
	 * Return the status of PV flag.
	 */
	protected final boolean FLAG_V()
	{
		return ((F & FLAG_PV) != 0) ? true : false;
	}

	/**
	 * Return the SF flag.
	 */
	protected final boolean FLAG_S()
	{
		return ((F & FLAG_S) != 0) ? true : false;
	}

	/**
	 * Return the HF flag.
	 */
	protected final boolean FLAG_H()
	{
		return ((F & FLAG_H) != 0) ? true : false;
	}

	/**
	 * Set the ZF flag.
	 */
	protected final void FLAG_Z(boolean value)
	{
		if (value)
			F |= FLAG_Z;
		else
			F &= ~FLAG_Z;
	}

	/**
	 * Set the SF flag.
	 */
	protected final void FLAG_S(boolean value)
	{
		if (value)
			F |= FLAG_S;
		else
			F &= ~FLAG_S;
	}


	/**
	 * Set the HF flag.
	 */
	protected void FLAG_H(boolean value)
	{
		if (value)
			F |= FLAG_H;
		else
			F &= ~FLAG_H;
	}

	/**
	 * Set the PV flag.
	 */
	protected final void FLAG_V(boolean value)
	{
		if (value)
			F |= FLAG_PV;
		else
			F &= ~FLAG_PV;
	}

	/**
	 * Return the NF flag.
	 */
	protected final boolean FLAG_N()
	{
		return ((F & FLAG_N) != 0) ? true : false;
		
	}

	/**
	 * Set the NF flag.
	 */
	protected final void FLAG_N(boolean value)
	{
		if (value)
			F |= FLAG_N;
		else
			F &= ~FLAG_N;
	}

	/**
	 * Return the undocumented bit 3 of flag.
	 */
	protected final boolean FLAG_3()
	{
		return ((F & FLAG_3) != 0) ? true : false;

	}

	/**
	 * Return the undocumented bit 5 of flag.
	 */
	protected final boolean FLAG_5()
	{
		return ((F & FLAG_5) != 0) ? true : false;

	}


	/**
	 * Set the undocumented bit 5 of flag.
	 */
	protected final void FLAG_5(boolean value)
	{
		if (value)
			F |= FLAG_5;
		else
			F &= ~FLAG_5;
	}

	/**
	 * Set the undocumented bit 3 of flag.
	 */
	protected final void FLAG_3(boolean value)
	{
		if (value)
			F |= FLAG_3;
		else
			F &= ~FLAG_3;
	}


	/**
	 * Initialize all required memories.<p>
	 * 
	 * Add the MAIN_MEMORY and the IO_MEMORY if sub class override
	 * this method to change the type of memory must first add the
	 * required memories and then call the parent method.
	 * 
	 */
	protected void initMemories()
	{
		Memory m ;

		m = getMemoryForName(MAIN_MEMORY);
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(MAIN_MEMORY,0x10000));

		// Set main memory
		setMemory(m);

		m = getMemoryForName(IO_MEMORY);
		if (m == null)
			m = (Memory)addHardware(new PlainMemory(IO_MEMORY,0x100));

		// Set I/O memory
		setIO(m);
		
	}

	/**
	 * Initialize all standard intel 8080 register.
	 */
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

		addRegister(new StandardRegister("A",Register.FAMILY_ACC,8,0)
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

		addRegister(new StandardRegister("F",Register.FAMILY_PSW,8,0)
		{
			public String descValue()
			{
				String s = "";
				
				s += "["+Hex.formatByte(F)+"]";

				if (FLAG_S())
					s += "S";
				else
					s += "-";

				if (FLAG_Z())
					s += "Z";
				else
					s += "-";

				if (FLAG_5())
					s += "5";
				else
					s += "-";

				if (FLAG_H())
					s += "H";
				else
					s += "-";

				if (FLAG_3())
					s += "3";
				else
					s += "-";
				if (FLAG_V())
					s += "P";
				else
					s += "-";

				if (FLAG_N())
					s += "N";
				else
					s += "-";

				if (FLAG_C())
					s += "C";
				else
					s += "-";

				return s;
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



		addRegister(new StandardRegister("BC",Register.FAMILY_GENERAL,16,0)
		{
			public int getRegister()
			{
				return BC;
			}

			public void setRegister(int value)
			{
				BC = value;
			}


		});

		addRegister(new StandardRegister("DE",Register.FAMILY_GENERAL,16,0)
		{
			public int getRegister()
			{
				return DE;
			}

			public void setRegister(int value)
			{
				DE = value;
				     
			}


		});

		addRegister(new StandardRegister("HL",Register.FAMILY_GENERAL,16,0)
		{
			public int getRegister()
			{
				return HL;
			}

			public void setRegister(int value)
			{
				HL = value;
			}


		});
		
	}



	/**
	 * Set the value of H register
	 */
	protected final void h(int v)
	{
		HL = (HL & 0xff) | (v << 8);
	}

	/**
	 * Set the value of L register
	 */
	protected final void l(int v)
	{
		HL = (HL & 0xff00) | ( v & 0xff);
	}

	/**
	 * Set the value of B register
	 */
	protected final void b(int v)
	{
		BC = (BC & 0xff) | (v << 8);
	}

	/**
	 * Set the value of C register
	 */
	protected final void c(int v)
	{
		BC = (BC & 0xFF00) | (v & 0xff);
	}

	/**
	 * Set the value of D register.
	 */
	protected final void d(int v)
	{
		DE = (DE & 0xFF) | (v << 8);
	}

	/**
	 * Set the value of E register.
	 */
	protected final void e(int v)
	{
		DE = (DE & 0xFF00) | (v & 0xFF);
	}

	/**
	 * Set the value of F register.
	 */
	protected final void f(int value)
	{
		F = value;
	}

	/**
	 * Return the AF register
	 */
	public final int af()
	{
		return A << 8 | F;
	}

	/**
	 * Set the value of AF register.
	 */
	protected final void af(int v)
	{
		A = v >> 8;
		F = v & 0xff;

	}

	public final boolean isInterruptEnabled()
	{
		return iff1 & iff2;
	}

	/**
	 * Initialize all peripherals.
	 * <p>
	 * This method do nothing because the Intel 8080 do not have
	 * any internal peripheral.
	 */
	protected void initPeripherals() throws SIMException
	{
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
	 * Push a WORD (16 bit) on the stack.
	 */
	protected final void push(int value) throws SIMException
	{
		SP = (SP - 2) & 0xFFFF;
		setWord(SP,value);
	}

	/**
	 * Pop a WORD (16 bit) from the stack and return it.
	 */
	public final int pop() throws SIMException
	{
		int v = getWord(SP);
		SP = (SP+2) & 0xffff;
		return v;
	}


	/**
	 * Return the 8 High bit for I/O operations.
	 */
	public final int getPortHI()
	{
		return portHI;
	}


	/**
	 * Output one byte to the specfied port.
	 *
	 * <p>
	 * @param portLow - 8 Bit low port address.
	 * @param portHI - 8 Bit hi port address.
	 * @param value - Value to write.
	 */
	public final void out(int portLow,int portHI,int value) throws SIMException
	{
		this.portHI = portHI;
		setIOByte(portLow,value);
	}

	/**
	 * Read a I/O port.
	 *
	 * <p>
	 * 
	 * @param portLOW - 8 Bit low port address.
	 * @param portHI - 8 Bit port high address.
	 *
	 * @return The value read from the specified port.
	 */
	protected final int in(int portLOW,int portHI) throws SIMException
	{
		this.portHI = portHI;
		return getIOByte(portLOW);
	}

	/**
	 * Decimal Adjust after addition.
	 */
	public final void daa()
	{
		int tmp1 = A;
		int tmp2 = 0;
		int tmp3 = FLAG_C() ? 1 : 0;
		int tmp = tmp3;
		
		if ( FLAG_H() || ((tmp1 & 0x0f) > 0x09) )
			tmp2 |= 0x06;
		
		if ( (tmp3 == 1) || ( tmp1 > 0x9f) || ((tmp1 > 0x8f) && ((tmp1 & 0x0f) > 0x09)))
		{
			tmp2 |= 0x60;
			tmp = 1;
		}
		
		if ( tmp1 > 0x99 )
		{
			tmp = 1;
		}
		
		if (FLAG_N())
			 sub8(tmp2);
		else
			add8(tmp2);
		
	
		FLAG_C(tmp != 0);
		FLAG_V(parityTable[A]);


	}

	/**
	 * 9 bit left rotation
	 */
	public final int rl(int value)
	{
		boolean bit7 = (value & 0x80) != 0;

		int c = FLAG_C() ? 1 : 0;
		value <<= 1;
		value |= c;
		value &= 0xff;
		F = booleanTable[value];
		FLAG_C(bit7);
		
		return value ;

	}	

	/**
	 * 8 bit left rotation
	 */
	protected final int rlc(int value)
	{
		boolean bit7 = (value & 0x80) != 0;
		value <<= 1;
		value &= 0xff;
		if (bit7)
			value |= 0x01;

		F = booleanTable[value];
		FLAG_C(bit7);

		
		return value;
	}

	/**
	 * 9 bit right rotate.
	 */
	protected final int rr(int value)
	{
		boolean bit0 = (value & 1) != 0;
		value = (value >> 1) & 0x7f;
		if (FLAG_C())
			value |= 0x80;
		F = booleanTable[value];
		FLAG_C(bit0); 

		return value;
	}

	public final void rla()
	{
		boolean bit7 = (A & 0x80) != 0;
		FLAG_H(false);
		FLAG_N(false);

		int c = FLAG_C() ? 1 : 0;
		A = ((A << 1) | c) & 0xff;
		
		FLAG_C(bit7);
		FLAG_H(false);
		FLAG_N(false);

		
	}
	
	public final void rra()
	{
		boolean bit0 = (A & 1) != 0;
		A = (A >> 1) & 0x7f;
		if (FLAG_C())
			A |= 0x80;
		
		FLAG_C(bit0); 
		FLAG_N(false);
		FLAG_H(false);

		
	}
	
	public final void rlca()
	{
		boolean bit7 = (A & 0x80) != 0;


		A = (A << 1) & 0xff;
		if (bit7)
			A |= 0x01;

		FLAG_C(bit7);
		FLAG_N(false);
		FLAG_H(false);
		
		FLAG_3((A & 0x08) != 0);
		FLAG_5((A & 0x20) != 0);

		
	}
	
	public final void rrca()
	{
		boolean bit0 = (A & 0x01) != 0;
		
		A = (A >>> 1) & 0xff;
		if (bit0)
			A |= 0x80;
		
		FLAG_C(bit0);
		FLAG_N(false);
		FLAG_H(false);
		
		FLAG_3((A & 0x08) != 0);
		FLAG_5((A & 0x20) != 0);

	}
	
	/**
	 * 8 bit right rotate
	 */
	public final int rrc(int value)
	{
		boolean bit0 = (value & 0x01) != 0;
		value = (value >>> 1) & 0x7f;
		if (bit0)
			value |= 0x80;
		F = booleanTable[value];
		FLAG_C(bit0);

		return value;
	}

	protected void resetRegisters() throws SIMException
	{
		super.resetRegisters();
		
		/** Reset interrupt */
		iff1 = iff2 = false;
	}
	
	
	/**
	 * Logical or
	 */
	public final void or(int value)
	{
		A = (A | value) & 0xff;

		F = booleanTable[A];
	}

	/**
	 * Set carry flag
	 */
	public final void scf()
	{
		FLAG_C(true);
		FLAG_H(false);
		FLAG_N(false);

		FLAG_3((A & 0x08) != 0);
		FLAG_5((A & 0x20) != 0);

		
	}
	
	/**
	 * Complement Carry Flag
	 */
	public final void ccf()
	{
		FLAG_H(FLAG_C());
		FLAG_N(false);
		FLAG_C(!FLAG_C());

		FLAG_3((A & 0x08) != 0);
		FLAG_5((A & 0x20) != 0);

	}
	
	/**
	 * Complement accumulator.
	 */
	public final void cpl()
	{
		A = A ^ 0xff;
		FLAG_H(true);
		FLAG_N(true);

		FLAG_3((A & 0x08) != 0);
		FLAG_5((A & 0x20) != 0);
		
	}
	
	/**
	 * Logical xor
	 */
	public final void xor(int value)
	{
		A  = (A ^ value) & 0xff;

		F = booleanTable[A];
	}

	/**
	 * Logical and
	 */
	public final void and(int value)
	{
		A = (A & value) & 0xff;

		F = booleanTable[A] | FLAG_H;
	}

	/**
	 * 8 bit addition
	 */
	public final void add8(int b)
	{
		adc8(b,0);
	}
	

	/**
	 * Add 8 bit with carry.
	 */
	public final void adc8(int b,int c)
	{
		int ans = A + b + c;

		F = adc8Table[ans & 0xff][A][c];
		A = ans & 0xff;
	}

	/**
	 * Sub 8 bit
	 */
	public final void sub8(int b)
	{
		sbc8(b,0);
	}
	


	/**
	 * Sub 8 bit with carry
	 */
	public final void sbc8(int b,int c)
	{
		int ans = A - b - c;

		F = sbc8Table[ans & 0xff][A][c];
		A =  ans & 0xff;


	}

	/**
	 * Compare value with accumulator.
	 */
	public final void cp(int v)
	{
		int tmp = A;
		sbc8(v,0);
		A = tmp;
	}

	
	/**
	 * 8 Bit increment
	 */
	public final int inc(int value)
	{
		F = (F & FLAG_C) | inc8Table[value];
		return  (value + 1) & 0xff;

	}


	/**
	 * 8 Bit decrement
	 */
	public final int dec(int value)
	{
		F = (F & FLAG_C) | dec8Table[value];
		return (value - 1) & 0xff;
	}

	/**
	 * 16 bit sub with carry
	 */
	public final int sbc16(int a,int b,int c)
	{
		int ans = a - b - c;
		
		F = (F & (FLAG_3|FLAG_5)) | FLAG_N;

		if ((ans & 0x8000) != 0)
			F |= FLAG_S;
		
		if ((ans & 0xffff) == 0)
			F |= FLAG_Z;
		
		if ((ans & 0x10000) != 0)
			F |= FLAG_C;

		if (((b ^ a ) & (a ^ ans) & 0x8000) != 0)
			F |= FLAG_PV;

		if (((a ^ ans ^ b) & 0x1000) != 0)
			F |= FLAG_H;
		

		return ans & 0xffff;

	}

	

	/**
	 * 16 bit addition
	 */
	public final  int add16(int a,int b)
	{
		int ans = a + b;
		FLAG_H(((a ^ ans ^ b) & 0x1000) != 0);
		FLAG_C((ans & 0x10000) != 0);
		FLAG_N(false);

		return ans & 0xffff;
	}

	
	/**
	 * 16 bit addition with carry
	 */
	protected final int adc16( int a, int b ,int c)
	{
		int ans = a + b + c;
		
		F = F & (FLAG_3|FLAG_5);
		
		if ((ans &  0x8000) != 0)
			F |= FLAG_S;
		if ((ans & 0x10000) != 0)
			F |= FLAG_C;
			
		if ((ans & 0xffff) == 0)
			F |= FLAG_Z;
		
		if (((a ^ ans ^ b) & 0x1000) != 0)
			F |= FLAG_H;
		
		if (((b ^ a ^ 0x8000) & (b ^ ans) & 0x8000) != 0)
			F |= FLAG_PV;
		
		return ans & 0xffff;
	}

	
	public final void setValuePP(int opcode,int value)
	{
		switch ((opcode >> 4) & 3)
		{
			case	0:
				BC = value;
				break;
				
			case	1:
				DE = value;
				break;
				
			case	2:
				HL = value;
				break;
				
			case	3:
				SP = value;
				break;
		}
	}

	/** Vector with all possible flag condition and mask */
	static private final int cccFlag[]  = {FLAG_Z,FLAG_Z,FLAG_C,FLAG_C,FLAG_PV,FLAG_PV,FLAG_S,FLAG_S};
	static private final int cccValue[] = {0     ,FLAG_Z,0     ,FLAG_C,0      ,FLAG_PV,0     ,FLAG_S};
	
	public final boolean getFlagCCC(int opcode)
	{
		opcode = (opcode >> 3) & 0x07;
		return (F & cccFlag[opcode]) == cccValue[opcode];
	}
	
	public String getCCC(int opcode)
	{
		switch ((opcode >> 3) & 7)
		{
			case	0:
				return "NZ";
			case	1:
				return "Z";
			case	2:
				return "NC";
			case	3:
				return "C";
			case	4:
				return "PO";
			case	5:
				return "PE";
			case	6:
				return "P";
			case	7:
				return "M";
		}

		return "";
	}
	
	public final void setValueQQ(int opcode,int value)
	{
		switch ((opcode >> 4) & 3)
		{
			case	0:
				BC = value;
				break;

			case	1:
				DE = value;
				break;

			case	2:
				HL = value;
				break;

			case	3:
				af(value);
				break;
		}
	}

	public String getPP(int opcode)
	{
		switch ((opcode >> 4) & 3)
		{
			case	0:
				return "BC";
			case	1:
				return "DE";
			case	2:
				return "HL";
			case	3:
				return "SP";
		}

		return "";
		
	}
	
	public final int getValuePP(int opcode)
	{
		switch ((opcode >> 4) & 3)
		{
			case	0:
				return BC;
			case	1:
				return DE;
			case	2:
				return HL;
			case	3:
				return SP;
		}

		return 0;
	}

	public String getQQ(int opcode)
	{
		switch ((opcode >> 4) & 3)
		{
			case	0:
				return "BC";
			case	1:
				return "DE";
			case	2:
				return "HL";
			case	3:
				return "AF";
		}

		return "";

	}

	public final int getValueQQ(int opcode)
	{
		switch ((opcode >> 4) & 3)
		{
			case	0:
				return BC;
			case	1:
				return DE;
			case	2:
				return HL;
			case	3:
				return af();
		}

		return 0;
	}


	public String getRRR(int opcode)
	{
		switch (opcode & 0x07)
		{
			case	0:
				return "B";
			case	1:
				return "C";
			case	2:
				return "D";
			case	3:
				return "E";
			case	4:
				return "H";
			case	5:
				return "L";
			case	6:
				return "(HL)";
			case	7:
				return "A";
		}

		return "";
		
	}
	
	public final int getValueRRR(int opcode) throws SIMException
	{
		switch (opcode & 0x07)
		{
			case	0:
				return BC >>> 8;
			case	1:
				return BC & 0xFF;
			case	2:
				return DE >>> 8;
			case	3:
				return DE & 0xFF;
			case	4:
				return HL >>> 8;
			case	5:
				return HL & 0xFF;
			case	6:
				return getByte(HL);
			case	7:
				return A;
		}

		return 0;
	}

	public final void setValueRRR(int opcode,int value) throws SIMException
	{
		switch (opcode & 0x07)
		{
			case	0:
				BC = (BC & 0xff) | (value << 8);
				break;
			case	1:
				BC = (BC & 0xFF00) | (value & 0xff);
				break;
			case	2:
				DE = (DE & 0xFF) | (value << 8);
				break;
			case	3:
				DE = (DE & 0xFF00) | (value & 0xFF);
				break;
			case	4:
				HL = (HL & 0xff) | (value << 8);
				break;
			case	5:
				HL = (HL & 0xff00) | (value & 0xff);
				break;
			case	6:
				setByte(HL,value);
				break;
			case	7:
				A = value;
				break;
		}

	}


	protected void initOpcodeDecoder()
	{
		addDecoder(new AbstractDecoder("%ccc",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				return getCCC(getByte(currentPc));
			}
		});
		
		addDecoder(new AbstractDecoder("%rrr",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				return getRRR(getByte(currentPc));
			}
		});
		
		addDecoder(new AbstractDecoder("%rr3",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				return getRRR(getByte(currentPc) >> 3);
			}
		});

		addDecoder(new AbstractDecoder("%pp",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				return getPP(getByte(currentPc));
			}
		});

		addDecoder(new AbstractDecoder("%qq",0)
		{
			protected String implDecode(CPU cpu,CpuRuntime _r,int startPc,int len,int currentPc) throws SIMException
			{
				return getQQ(getByte(currentPc));
			}
		});

	}
		
	protected void initOpcodes()
	{
		int i,j;

		setOpcode(new AbstractOpcode(0x02,1,7,"LD\t(BC),A")
		{
			public int exec(int pc) throws SIMException
			{
				setByte(BC,A);

				return 10;
			}
		});

		
		
		setOpcode(new AbstractOpcode(0x27,1,4,"DAA")
		{
			public int exec(int pc) throws SIMException
			{
				daa();
				return 4;
			}

		});

		for (i = 0 ; i < 8 ; i++)
		{
			String s = Hex.formatByte(i*8);
			setOpcode(new AbstractOpcode(0xC7 | (i << 3),1,11,"RST\t"+s)
			{
				public int exec(int pc) throws SIMException
				{
					push(PC);
					PC = ((opcode >> 3) & 0x07) * 8;
					return 11;
				}
			});
		}
		setOpcode(new AbstractOpcode(0x07,1,4,"RLCA")
		{
			public int exec(int pc) throws SIMException
			{
				rlca();
				return 4;
			}

		});

		
		setOpcode(new AbstractOpcode(0x0f,1,4,"RRCA")
		{
			public int exec(int pc) throws SIMException
			{
				rrca();
				return 4;
			}

		});

		setOpcode(new AbstractOpcode(0x17,1,4,"RLA")
		{
			public int exec(int pc) throws SIMException
			{
				rla();
				return 4;
			}

		});

		setOpcode(new AbstractOpcode(0x1f,1,4,"RRA")
		{
			public int exec(int pc) throws SIMException
			{
				rra();
				return 4;
			}

		});

		setOpcode(new AbstractOpcode(0x32,3,13,"LD\t(%word),A")
		{
			public int exec(int pc) throws SIMException
			{
				setByte(getWord(pc+1),A);

				return 13;
			}

		});

		setOpcode(new AbstractOpcode(0x3A,3,13,"LD\tA,(%word)")
		{
			public int exec(int pc) throws SIMException
			{
				A = getByte(getWord(pc+1));

				return 13;
			}

		});

		setOpcode(new AbstractOpcode(0xF3,1,4,"DI")
		{
			public int exec(int pc) throws SIMException
			{
				iff1 = iff2 = false;

				return 4;
			}
			
		});

		setOpcode(new AbstractOpcode(0xFB,1,4,"EI")
		{
			public int exec(int pc) throws SIMException
			{
				iff1 = iff2 = true;

				return 4;
			}

		});
		
	
		/**
		 * Return from subroutine.
		 */
		setOpcode(new AbstractOpcode(0xC9,1,10,"RET")
		{
			public int exec(int pc) throws SIMException
			{
				PC = pop();

				return 10;
			}

		});

		setOpcode(new AbstractOpcode(0xEE,2,7,"XOR\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				xor(getByte(pc+1));

				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0xCE,2,7,"ADC\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				adc8(getByte(pc+1),FLAG_C() ? 1 : 0);

				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0xE6,2,7,"AND\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				and(getByte(pc+1));

				return 7;
			}
		});
		
		setOpcode(new AbstractOpcode(0xF6,2,7,"OR\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				or(getByte(pc+1));

				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0xC6,2,7,"ADD\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				adc8(getByte(pc+1),0);

				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0xD6,2,7,"SUB\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				sbc8(getByte(pc+1),0);

				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0xE3,1,19,"EX\t(SP),HL")
		{
			public int exec(int pc) throws SIMException
			{
				int tmp = getByte(SP);
				setByte(SP,HL & 0xFF);
				l(tmp);

				tmp = getByte(SP+1);
				setByte(SP+1,HL >>> 8);
				h(tmp);

				return 19;
			}
		});
		
		setOpcode(new AbstractOpcode(0xEB,1,4,"EX\tDE,HL")
		{
			public int exec(int pc) throws SIMException
			{
				int tmp =HL;
				HL = DE;
				DE = tmp;

				return 4;
			}
		});

		setOpcode(new AbstractOpcode(0x1A,1,7,"LD\tA,(DE)")
		{
			public int exec(int pc) throws SIMException
			{
				A = getByte(DE);
				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0x12,1,7,"LD\t(DE),A")
		{
			public int exec(int pc) throws SIMException
			{
				setByte(DE,A);
				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0x0A,1,7,"LD\tA,(BC)")
		{
			public int exec(int pc) throws SIMException
			{
				A = getByte(BC);
				return 7;
			}
		});
		
		setOpcode(new AbstractOpcode(0x76,1,4,"HALT")
		{
			public int exec(int pc) throws SIMException
			{
				halt();
				
				return 10;
			}
		});
		
		setOpcode(new AbstractOpcode(0x00,1,4,"NOP")
		{
			public int exec(int pc) throws SIMException
			{
				return 4;
			}
		});

		setOpcode(new AbstractOpcode(0xFE,2,7,"CP\t%byte")
		{
			public int exec(int pc) throws SIMException
			{
				cp(getByte(pc+1));
				
				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0xDE,2,7,"SBC\tA,%byte")
		{
			public int exec(int pc) throws SIMException
			{
				sbc8(getByte(pc+1),FLAG_C() ? 1 : 0);
				return 7;
			}
		});

		setOpcode(new AbstractOpcode(0x2F,1,4,"CPL")
		{
			public int exec(int pc) throws SIMException
			{
				cpl();
				return 4;
			}
		});

		setOpcode(new AbstractOpcode(0x3F,1,4,"CCF")
		{
			public int exec(int pc) throws SIMException
			{
				ccf();
				return 4;
			}
		});

		setOpcode(new AbstractOpcode(0x37,1,4,"SCF")
		{
			public int exec(int pc) throws SIMException
			{
				scf();

				return 4;
			}
		});

		setOpcode(new AbstractOpcode(0xD3,2,11,"OUT\t(%byte)")
		{
			public int exec(int pc) throws SIMException
			{
				out(getByte(pc+1),0,A);

				return 11;
			}
		});

		setOpcode(new AbstractOpcode(0xDB,2,11,"IN\t(%byte)")
		{
			public int exec(int pc) throws SIMException
			{
				A = in(getByte(pc+1),A);

				return 11;
			}
		});

		setOpcode(new AbstractOpcode(0xCD,3,17,"CALL\t%word")
		{
			public int exec(int pc) throws SIMException
			{
				push(PC);
				PC = getWord(pc+1);

				return 17;
			}
		});
		
		setOpcode(new AbstractOpcode(0xC3,3,10,"JP\t%word")
		{
			public int exec(int pc) throws SIMException
			{
				PC = getWord(pc+1);

				return 10;
			}
		});

		setOpcode(new AbstractOpcode(0xE9,1,10,"JP\tHL")
		{
			public int exec(int pc) throws SIMException
			{
				PC = HL;

				return 10;
			}
		});




		setOpcode(new AbstractOpcode(0x36,2,10,"LD\t(HL),%byte")
		{
			public int exec(int pc) throws SIMException
			{
				setByte(HL,getByte(pc+1));

				return 10;
			}
		});

		setOpcode(new AbstractOpcode(0x22,3,16,"LD\t(%word),HL")
		{
			public int exec(int pc) throws SIMException
			{
				setWord(getWord(pc+1),HL);

				return 16;
			}
		});

		setOpcode(new AbstractOpcode(0xF9,1,6,"LD\tSP,HL")
		{
			public int exec(int pc) throws SIMException
			{
				SP = HL;
				return 6;
			}
		});
		
		setOpcode(new AbstractOpcode(0x2a,3,16,"LD\tHL,(%word)")
		{
			public int exec(int pc) throws SIMException
			{
				HL = getWord(getWord(pc+1));

				return 16;
			}
		});

		setOpcode(new AbstractOpcode(0x34,1,11,"INC\t(HL)")
		{
			public int exec(int pc) throws SIMException
			{
				int i = HL;
				setByte(i,inc(getByte(i)));

				return 11;
			}
		});


		// Operation of pair register
		for (i = 0 ; i < 4 ; i++)
		{
			
			setOpcode(new AbstractOpcode(0xc1|(i << 4),1,10,"POP\t%qq")
			{
				public int exec(int pc) throws SIMException
				{
					setValueQQ(opcode,pop());

					return 10;
				}

			});

			setOpcode(new AbstractOpcode(0xc5|(i << 4),1,11,"PUSH\t%qq")
			{
				public int exec(int pc) throws SIMException
				{
					push(getValueQQ(opcode));

					return 11;
				}

			});

			setOpcode(new AbstractOpcode(0x01|(i << 4),3,10,"LD\t%pp,%word")
			{
				public int exec(int pc) throws SIMException
				{
					setValuePP(opcode,getWord(pc+1));

					return 11;
				}

			});

			setOpcode(new AbstractOpcode(0x0B|(i << 4),1,6,"DEC\t%pp")
			{
				public int exec(int pc) throws SIMException
				{
					setValuePP(opcode,(getValuePP(opcode) - 1) & 0xffff);

					return 6;
				}

			});

			setOpcode(new AbstractOpcode(0x03|(i << 4),1,6,"INC\t%pp")
			{
				public int exec(int pc) throws SIMException
				{
					setValuePP(opcode,(getValuePP(opcode) + 1) & 0xffff);

					return 6;
				}

			});

			setOpcode(new AbstractOpcode(0x09|(i << 4),1,11,"ADD\tHL,%pp")
			{
				public int exec(int pc) throws SIMException
				{
					HL = add16( HL , getValuePP(opcode));

					return 6;
				}

			});
			
		}

		
		// Operation on 8 bit  register rrr
		for (i = 0 ; i < 8 ; i++)
		{

			setOpcode(new AbstractOpcode(0x05 |(i << 3) ,1,4,"DEC\t%rr3")
			{
				public int exec(int pc) throws SIMException
				{
					int r = opcode >> 3;
					setValueRRR(r,dec(getValueRRR(r)));

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0x90 | i ,1,4,"SUB\tA,%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					sbc8(getValueRRR(opcode),0);

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0x98 | i ,1,4,"SBC\tA,%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					 sbc8(getValueRRR(opcode),FLAG_C() ? 1 : 0);

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0x80 | i ,1,4,"ADD\tA,%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					adc8(getValueRRR(opcode),0);

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0x88 | i ,1,4,"ADC\tA,%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					adc8(getValueRRR(opcode),FLAG_C() ? 1 : 0);

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0xA0 | i ,1,4,"AND\t%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					and(getValueRRR(opcode));

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0xA8 | i ,1,4,"XOR\tA,%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					 xor(getValueRRR(opcode));

					return 4;
				}

			});

			setOpcode(new AbstractOpcode(0xB0 | i ,1,4,"OR\tA,%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					or(getValueRRR(opcode));

					return 4;
				}

			});


			setOpcode(new AbstractOpcode(0xB8 | i ,1,4,"CP\t%rrr")
			{
				public int exec(int pc) throws SIMException
				{
					cp(getValueRRR(opcode));

					return 4;
				}

			});

			// Skip (HL)
			if (i != 6)
			{
				for (j = 0 ; j < 8 ; j++)
				{
					if (j != 6) // Skip (HL)
					{
						setOpcode(new AbstractOpcode(0x40|(i << 3)|j,1,4,"LD\t"+getRRR(i)+","+getRRR(j))
						{
							public final int exec(int pc) throws SIMException
							{
								setValueRRR(opcode >> 3,getValueRRR(opcode));

								return 4;
							}

						});
					}

				}

				setOpcode(new AbstractOpcode(0x04 |(i << 3) ,1,4,"INC\t"+getRRR(i))
				{
					public int exec(int pc) throws SIMException
					{
						int r = opcode >> 3;
						setValueRRR(r,inc(getValueRRR(r)));

						return 4;
					}

				});

				setOpcode(new AbstractOpcode(0x06 | (i << 3),2,7,"LD\t%rr3,%byte")
				{
					public int exec(int pc) throws SIMException
					{
						setValueRRR(opcode >> 3,getByte(pc+1));

						return 7;
					}

				});

				setOpcode(new AbstractOpcode(0x46 | (i << 3),1,7,"LD\t%rr3,(HL)")
				{
					public int exec(int pc) throws SIMException
					{
						setValueRRR(opcode >> 3,getByte(HL));

						return 7;
					}

				});

				setOpcode(new AbstractOpcode(0x70 | i,1,7,"LD\t(HL),%rrr")
				{
					public int exec(int pc) throws SIMException
					{
						setByte(HL,getValueRRR(opcode));

						return 7;
					}

				});
			}
			

		}

		// operation on flag
		for (i = 0 ; i < 8 ; i ++)
		{
			setOpcode(new AbstractOpcode(0xC0 | (i << 3),1,5,"RET\t"+getCCC(i))
			{
				public final int exec(int pc) throws SIMException
				{
					if (getFlagCCC(opcode))
					{
						PC = pop();
						return 11;
					}
					return 5;
				}

			});

			setOpcode(new AbstractOpcode(0xC4 | (i << 3),3,10,"CALL\t%ccc,%word")
			{
				public int exec(int pc) throws SIMException
				{
					
					if (getFlagCCC(opcode))
					{
						push(PC);
						PC = getWord(pc+1);
						return 17;
					}
					return 10;
				}

			});

			setOpcode(new AbstractOpcode(0xC2 | (i << 3),3,10,"JP\t%ccc,%word")
			{
				public int exec(int pc) throws SIMException
				{
					if (getFlagCCC(opcode))
					{
						PC = getWord(pc+1);
						return 10;
					}
					return 3;
				}

			});

			
		}
	}


	public int fireISR(Interrupt isr) throws SIMException
	{
		iff2 = iff1 = false;
		push(PC);
		PC = isr.getVector();

		return 10;
	}

	public int fireNMI(Interrupt isr) throws SIMException
	{
		iff2 = iff1;
		iff1 = false;
		push(PC);
		PC = isr.getVector();

		return 10;
	}		

}

