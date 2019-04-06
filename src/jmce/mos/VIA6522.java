/**
   $Id: VIA6522.java 518 2011-01-21 08:18:50Z mviara $

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

import jmce.sim.*;
import jmce.util.Logger;
import jmce.util.Hex;
import jmce.util.FastArray;
import jmce.mos.IRQ6502;
import jmce.mos.M6502;

/**
 * MOS VIA6522  - Versatile Interface Adapter.
 * <p>
 * The VIA6522 support two 8 bit bidirectional port (port A and port B)
 * and 2 different timers.
 *
 * <h2>Register</h2>
 * <p>
 * All address are relative to the base address. The base address can
 * be set using a method setBase().
 * <p>
 * <ul>
 *  <li>00 Port register B - ORB</li>
 *  <li>01 Port register A. (Read/Write controls handshake) - ORA</li>
 *  <li>02 Direction register B - DDRB</li>
 *  <li>03 Direction register A - DDRA</li>
 *  <li>0C Peripheral control register - PCR</li>
 *  
 *  <li>0F Port register A (No handshake controls) - ORAN</li>
 * </ul>
 * <p>
 * <h2>Limitations</h2>
 * <ul>
 *  <li>Must be connected to a MOS 65xx microprocessor.</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public class VIA6522 extends AbstractPeripheral implements MemoryWriteListener,MemoryReadListener,CycleListener
{
	private static Logger log = Logger.getLogger(VIA6522.class);

	/** Base address */
	private int base = 0;
	
	/** Port B register */
	static public final int PBR		= 0x00;

	/** Port A register */
	static public final int PAR		= 0x01;

	/** Port B direction register */
	static public final int PBD		= 0x02;

	/** Port A Direction Register */
	static public final int PAD		= 0x03;

	/** Interrupt flag register */
	static public final int IFR		= 0x0d;

	/** Interrupt enable register */
	static public final int IER		= 0x0E;
	static public final int IER_CONTROL	= 0x80;
	static public final int IER_TIMER1	= 0x40;
	static public final int IER_TIMER2	= 0x20;
	static public final int IER_CB1		= 0x10;
	static public final int IER_CB2		= 0x08;
	static public final int IER_SHIFT	= 0x04;
	static public final int IER_CA1		= 0x02;
	static public final int IER_CA2		= 0x01;

	/** Auxiliary control register */
	static public final int ACR		= 0x0B;

	/** Peripheral control register */
	static public final int PCR		= 0x0C;

	/** Timer 1 low byte counter register */
	static public final int T1CL		= 0x04;

	/** Timer 1 high byte counter register */
	static public final int T1CH		= 0x05;

	/** Timer 1 low byte latch register */
	static public final int T1LL		= 0x06;

	/** Yimer 1 high byte latch register */
	static public final int T1LH		= 0x07;

	/** Timer2 low byte */
	static public final int T2L		= 0x08;

	/** Timer2 hi byte */
	static public final int T2H		= 0x09;

	/** Port a no handshake */
	static public final int PARA		= 0x0F;

	/** Timer 1 interrupt */
	private IRQ6502 irqTimer1;

	/** Timer 2 interrupt */
	private IRQ6502 irqTimer2;

	/** Interrupt enable register */
	private int ier = 0;

	/** Auxliary control register */
	private int acr = 0;

	/** Peripheral control register */
	private int pcr = 0;

	private int timer1Counter = 0;
	private int timer1Latch   = 0;
	private int timer2Counter = 0;
	private int timer2Latch	  = 0;

	private boolean timer1Interrupt = false;
	private boolean timer2Interrupt = false;
	private Port6522 pa = null;
	private Port6522 pb = null;

	class Port6522
	{
		//private FastArray<MemoryWriteListener> mwl1 = new FastArray<MemoryWriteListener>();
		private FastArray<MemoryWriteListener> mwl2 = new FastArray<MemoryWriteListener>();
		private boolean c1,c2,autoSetC2;
		private IRQ6502 irq1,irq2;
		private int shift;

		/** 1 bit means output */
		private int dir;
		private int in;
		private int out;
		char    portName;
		
		Port6522(char name,int shift) throws SIMException
		{
			c1 = c2 = false;
			irq1    = new IRQ6502((M6502)cpu,VIA6522.this.getName()+"/C"+name+"1");
			irq2    = new IRQ6502((M6502)cpu,VIA6522.this.getName()+"/C"+name+"2");

			this.shift = shift;
			this.portName = name;
		}

		public void setDir(int dir)
		{
			this.dir = dir;
		}

		public int getDir()
		{
			return dir;
		}
		
		public int getPort()
		{
			return ((in & ~dir) | (out & dir)) & 0xff;
		}

		public void setOutput(int n)
		{
			out = n;
		}

		public void setInput(int n)
		{
			in = n;
		}
		
		
		public void updateIrqs() throws SIMException
		{
			if (shift == 0)
			{
				irq1.setEnabled((ier & IER_CA1) != 0);
				irq2.setEnabled((ier & IER_CA2) != 0);
			}
			else
			{
				irq1.setEnabled((ier & IER_CB1) != 0);
				irq2.setEnabled((ier & IER_CB2) != 0);

			}
		}
		
		public void pcr(int value) throws SIMException
		{
			int i = (value >>> (1+shift)) & 0x07;
			
			/** C2 control */
			switch (i)
			{
				case	0:
				case	1:
				case	2:
				case	3:
					irq2.setEnabled(true);
					break;
				default:
					irq2.setEnabled(false);
					
					/** Manual output mode C2 low */
					if (i == 6)
						setC2(false);
					/** Manual output mode C2 high */
					if (i == 7)
						setC2(true);
					break;
			}
			
		}
		
		void resetIfC2() throws SIMException
		{
			int i = (pcr >>> (1+shift)) & 0x07;

			if (i != 1 && i != 3)
				irq2.setActive(false);
		}

		public int read() throws SIMException
		{
			irq1.setActive(false);
			resetIfC2();

			return getPort();
		}
		
		public void write(int n) throws SIMException
		{
			int i = (pcr >>> (1+shift)) & 0x07;

			setOutput(n);
			
			irq1.setActive(false);
			resetIfC2();
			
			if (i == 4)
				setC2(false);

			if (i == 5)
			{
				setC2(false);
				autoSetC2 = true;
			}
			
		}

		public void cycle(int n) throws SIMException
		{
			if (autoSetC2)
			{
				autoSetC2 = false;
				setC2(true);
			}
		}
		
		public boolean irq1() throws SIMException
		{
			return irq1.isActive();
		}

		public boolean irq2() throws SIMException
		{
			return irq2.isActive();
		}

		public void setC1(boolean mode) throws SIMException
		{
			int i = (pcr >>> (1+shift)) & 0x07;
			boolean cbctrl = (pcr & (0x01 << shift) ) != 0;
			
			
			if (c1 != mode)
			{

				if (i == 4)
					setC2(true);
				if (cbctrl == mode)
					irq1.setActive(true);
				c1 = mode;
			}
			
		}

		public boolean getC2()
		{
			return c2;
		}

		public boolean getC1()
		{
			return c1;
		}


		void addC2MemoryWriteListener(MemoryWriteListener l)
		{
			mwl2.add(l);
		}
		
		public void setC2(boolean mode) throws SIMException
		{
			int i;
			

			for (i = mwl2.getSize() ; --i >= 0 ;)
				mwl2.get(i).writeMemory(null,2,mode ? 1 : 0,c2 ? 1 : 0);

			i = (pcr >>> (1+shift)) & 0x07;

			if (mode != c2)
			{
				

				switch (i)
				{
					case	0:
					case	1:
						if (c2 && !mode)
							irq2.setActive(true);
						break;
					case	2:
					case	3:
						if (!c2 && mode)
							irq2.setActive(true);
						break;
				}

				c2 = mode;
			}

			
			
		}


	}
	

	/**
	 * Default constructor
	 */
	public VIA6522() throws SIMException
	{
		this("VIA6522",0);
	}
	
	/**
	 * Constructor with name and base address
	 */
	public VIA6522(String name,int base) throws SIMException
	{
		setName(name);
		setBase(base);


	}

	/** Return the base for this VIA */
	public int getBase()
	{
		return base;
	}

	/** Set the base address of this VIA */
	public void setBase(int base    )
	{
		this.base = base;
	}

	@Override
	public void reset() throws SIMException
	{
		super.reset();
		ier = 0;
		timer1Interrupt = false;
		updateIrqs();
		
	}

	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		irqTimer1 = new IRQ6502((M6502)cpu,getName()+"/Timer1");
		irqTimer2 = new IRQ6502((M6502)cpu,getName()+"/Timer2");


		pa = new Port6522('A',0);
		pb = new Port6522('B',4);


		for (int i = 0 ; i < 16 ; i++)
		{
			cpu.addIOWriteListener(base+i,this);
			cpu.addIOReadListener(base+i,this);
		}

		cpu.addCycleListener(this);
	}

	private void startTimer2()
	{
		timer2Interrupt = true;
		timer2Counter = timer2Latch;

	}
	
	private void startTimer1()
	{
		timer1Interrupt = true;
		timer1Counter = timer1Latch;
	}

	public void cycle(int n) throws SIMException
	{
		pa.cycle(n);
		pb.cycle(n);

		for (;n > 0 ; n--)
		{
			timer1Counter = (timer1Counter - 1) & 0xffff;

			if (timer1Counter == 0)
			{
				if (timer1Interrupt)
				{
					irqTimer1.setActive(true);
				}

				/** Reload counter ? */
				if ((acr & 0x40) != 0)
				{
					timer1Counter = timer1Latch;
				}
				else
				{
					timer1Interrupt = false;
				}
			}

			timer2Counter = (timer2Counter - 1) & 0xffff;

			if (timer2Counter == 0)
			{
				if (timer2Interrupt)
				{
					irqTimer2.setActive(true);
					timer2Interrupt = false;
				}

			}



		}
	}



	public void writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		address &= 0x0f;

		//log.info(this+" Write "+Hex.formatByte(address)+"="+Hex.formatByte(value));

		switch (address)
		{
			case	PAD:
				pa.setDir(value);
				break;
				
			case	PBD:
				pb.setDir(value);
				break;
				
			case	IFR:
				log.info(this+" IFR="+Hex.formatByte(value));
				System.exit(1);
				break;
				
			case	PARA:
				pa.setOutput(value);
				log.info(this+" PARA="+Hex.formatByte(value));
				break;
					
			case	PAR:
				pa.write(value);
				break;
				
			case	PBR:
				pb.write(value);
				break;
				
			case	PCR:
				pcr(value);
				break;
				
				
			case	T1CL:
			case	T1LL:
				timer1Latch &= 0xff00;
				timer1Latch |= value;
				break;

				
			case	T1LH:
				timer1Latch &= 0xFF;
				timer1Latch |= value << 8;
				irqTimer1.setActive(false);
				break;

			case	T1CH:
				timer1Latch &= 0xFF;
				timer1Latch |= value << 8;
				irqTimer1.setActive(false);
				startTimer1();
				break;

			case	T2L:
				timer2Latch &= 0xff00;
				timer2Latch |= value;
				break;

			case	T2H:
				timer2Latch &= 0xFF;
				timer2Latch |= value << 8;
				irqTimer2.setActive(false);
				startTimer2();
				break;

			case	ACR:
				acr = value;
				break;
				
			case	IER:
				ier(value);
				break;
		}
	}



	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		address &= 0x0f;
		
		switch (address)
		{
			case	PBD:
				value = pb.getDir();
				break;

			case	PAD:
				value = pa.getDir();
				break;
				
			case	PBR:
				value = pb.read();
				//log.info(this+" PB="+Hex.formatByte(value));
				break;

			case	PAR:
				value = pa.read();
				//log.info(this+" PA="+Hex.formatByte(value));
				break;
				

			case	IFR:
				value = 0;
				
				if (irqTimer1.isActive())
					value |= IER_TIMER1;
				
				if (irqTimer2.isActive())
					value |= IER_TIMER2;
				
				if (pa.irq1())
					value |= IER_CA1;
				
				if (pa.irq2())
					value |= IER_CA2;

				if (pb.irq1())
					value |= IER_CB1;
				
				if (pb.irq2())
					value |= IER_CB2;

				if ((value & ier) != 0)
					value |= 0x80;
				break;

			case	T2L:
				irqTimer2.setActive(false);
				value = timer2Counter;
				break;

			case	T2H:
				value = timer2Counter >>> 8;
				break;
				
			case	T1CL:
				irqTimer1.setActive(false);
				value = timer1Counter;
				break;

			case	T1CH:
				value = timer1Counter >>> 8;
				break;

			case	T1LL:
				value = timer1Latch & 0xff;
				break;

			case	T1LH:
				value = timer1Latch >>> 8;
				break;

			case	ACR:
				value = acr;
				break;

			case	IER:
				value = ier | 0x80;
				break;

			case	PARA:
				value = pa.getPort();
				//log.info(this+" PAA="+Hex.formatByte(value));
				break;
		}

		value &= 0xff;
		//log.info(this+" Read "+Hex.formatByte(address)+"="+Hex.formatByte(value));

		return value ;
	}

	/**
	 * Write the PCR
	 */
	private void pcr(int value) throws SIMException
	{

		pcr = value;
		pa.pcr(value);
		pb.pcr(value);
		
	}
	
	/**
	 * Write the IER register.
	 */
	private void ier(int value) throws SIMException
	{
		/** Clear bit ? */
		if ((value & IER_CONTROL) == 0)
			ier &= ~(value & 0x7F);
		else
			ier |= (value & 0x7F);
		updateIrqs();
	}

	/**
	 * Update the interrupt enable of all irq supported. Called
	 * when the IER is changed.
	 */
	private void updateIrqs() throws SIMException
	{

		irqTimer1.setEnabled((ier & IER_TIMER1) != 0);
		irqTimer2.setEnabled((ier & IER_TIMER2) != 0);
		
		pa.updateIrqs();
		pb.updateIrqs();
	}

	/**
	 * Set the value for port A input called from other peripheral.
	 */
	public void writePortA(int a) throws SIMException
	{
		pa.setInput(a);
	}
	
	/**
	 * Return the value for the Port A 
	 */
	public int readPortA() throws SIMException
	{
		return pa.getPort();
	}

	/**
	 * Set the value of the input for port B
	 */
	public void writePortB(int b) throws SIMException
	{
		pb.setInput(b);
	}
	
	/*
	 * Return the value for the Port B 
	 */
	public int readPortB() throws SIMException
	{
		return pb.getPort();
	}


	/**
	 * Add a new memory write listener to Port A register. Catch
	 * only write from the cpu.
	 */
	public void addPortAWriteListener(MemoryWriteListener l)
	{
		cpu.addIOWriteListener(base+PAR,l);
	}

	/**
	 * Add a new memory write listener to Port B register. Catch
	 * only write from the cpu.
	 */
	public void addPortBWriteListener(MemoryWriteListener l)
	{
		cpu.addIOWriteListener(base+PBR,l);
	}

	public boolean readCB2()
	{
		return pb.getC2();
	}
	
	public void writeCB2(boolean mode) throws SIMException
	{
		pb.setC2(mode);
	}


	public boolean readCA1()
	{
		return pa.getC1();
	}

	public void writeCA1(boolean mode) throws SIMException
	{
		pa.setC1(mode);
	}

	public void addCA2MemoryWriteListener(MemoryWriteListener l)
	{
		pa.addC2MemoryWriteListener(l);
	}
	
	public boolean readCA2()
	{
		return pa.getC2();
	}

	public void writeCA2(boolean mode) throws SIMException
	{
		pa.setC2(mode);
	}


	public boolean readCB1()
	{
		return pb.getC1();
	}

	public void writeCB1(boolean mode) throws SIMException
	{
		pb.setC1(mode);
	}

	public int getTimer2Counter()
	{
		return timer2Counter;
	}
	
	public String toString()
	{
		return getName()+ " at 0x"+Hex.formatWord(base);
	}


}

