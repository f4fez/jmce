/**
   $Id: Serial.java 510 2011-01-18 09:25:07Z mviara $

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

import jmce.sim.CPU;
import jmce.sim.Memory;
import jmce.sim.MemoryReadListener;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.sim.TimerOverflowListener;
import jmce.util.Logger;

/**
 * Standard 8051 serial interface.
 * <p>
 * All function to send receive data are supported in polling and interrupt mode. The timing of the uart are not
 * simulated and all character sent or received are processed as soon as possible.
 * <p>
 * The serial interface extends the <tt>jmce.sim.serial</tt> class an order to connect the uart to another CPU or to one
 * terminal.
 *
 * @author Mario Viara
 * @version 1.01
 *
 * @see jmce.sim.Serial
 */
public class Serial extends jmce.sim.Serial implements MemoryReadListener, MemoryWriteListener, MCS51Constants {

	protected static final Logger log = Logger.getLogger(Serial.class);
	
	private int sbuf, scon;

	private boolean sbufSent = true;

	private Interrupt8051 irqTx;

	private Interrupt8051 irqRx;
	
	private int serialMode;
	
	private int txClockCounter = -1;
	
	private boolean smod;
	
	private SerialClockSource serialClockSource;
	
	private CPU cpu;

	/**
	 * Default constructor
	 */
	public Serial() {
		super("Serial");
	}

	public void registerCPU(CPU cpu) throws SIMException {
		super.registerCPU(cpu);
		this.cpu = cpu;

		cpu.addIOWriteListener(SBUF, this);
		cpu.addIOReadListener(SBUF, this);
		cpu.addIOWriteListener(SCON, this);
		cpu.addIOWriteListener(PCON, this);

		irqTx = new Interrupt8051((MCS51) cpu, "Serial Tx", 0x0023);
		// Enable interrupt on SIO
		irqTx.addInterruptCondition(IE, IE_ES);
		// RX or TX interrupt
		irqTx.addInterruptRisingFrontCondition(SCON, SCON_TI);

		irqRx = new Interrupt8051((MCS51) cpu, "Serial Rx", 0x0023);
		// Enable interrupt on SIO
		irqRx.addInterruptCondition(IE, IE_ES);
		// RX or TX interrupt
		irqRx.addInterruptCondition(SCON, SCON_RI);
	}

	@Override
	public void reset() throws SIMException {
		super.reset();
		sbuf = '?';
		scon = 0;
		sbufSent = true;
		serialModeUpdated(0);
	}

	@Override
	public void writeInput(Integer c) throws SIMException {
		super.writeInput(c);
		feedCpu();
	}

	/**
	 * Feed the CPU is new data area available
	 */
	private void feedCpu() throws SIMException {
		if (!sbufSent || (scon & SCON_RI) != 0) {
			return;
		}

		if (readyRead()) {
			sbuf = read();
			sbufSent = false;
			cpu.setIOByte(SCON, cpu.getIOByte(SCON) | SCON_RI);
		}
	}

	public int readMemory(Memory m, int a, int v) throws SIMException {
		switch (a) {
			case SBUF:
				v = sbuf;
				sbufSent = true;
				feedCpu();
				return v;
		}

		return 0;
	}

	public void writeMemory(Memory m, int a, int v, int oldValue) throws SIMException {
		switch (a) {
			case SCON:
				scon = v;
				feedCpu();
				final int tmpSerial = v >> 6;
				if (tmpSerial != serialMode) {
					serialModeUpdated(tmpSerial);
				}
				break;

			case SBUF:
				sbuf = v;
				startTxClockCounter();
				break;
				
			case PCON:
				smod = (v & 0x70) != 0;
				break;
		}
	}
	
	/** 
	 * Occur when serial mode change. Update ref clock source.
	 * @param newSerialMode
	 */
	private void serialModeUpdated(final int newSerialMode) {
		if(serialClockSource != null) {
			serialClockSource.release();
		}
		switch (newSerialMode) {
			case 0:
				serialClockSource = new SerialClockSourceMode0();
				break;
			case 1:
				serialClockSource = new SerialClockSourceMode1();
				break;
			case 2:
				log.severe("Serial mode 2 not implemented");
				break;
			case 3:
				log.severe("Serial mode 3 not implemented");
				break;
		}
		serialMode = newSerialMode;
		if(serialClockSource != null) {
			serialClockSource.init();
		}
	}
	
	/** 
	 * Start data transmission.
	 */
	private void startTxClockCounter() {
		txClockCounter = 10;
	}
	
	/**
	 * Call each time the serial clock source generate a front.
	 * @throws SIMException
	 */
	private void txClockTriggered() {
		if (txClockCounter == -1) {
			return;
		}
		if (txClockCounter == 0) {
			try {
				cpu.setIOByte(SCON, cpu.getIOByte(SCON) | SCON_TI);
				write(sbuf);
			} catch (SIMException e) {
				log.severe(e.getMessage());
			}
		}
		txClockCounter--;
	}
	
	private interface SerialClockSource {
		void init();
		void release();
	}
	
	private class SerialClockSourceMode0 implements SerialClockSource {

		@Override
		public void init() {
			//TODO
		}

		@Override
		public void release() {
			//TODO
		}
		
	}
	private class SerialClockSourceMode1 implements SerialClockSource {
		Timer timer;
		TimerOverflowListener listener;
		int divider = 16;
		int smodCounter = smod ? 0 : 1;
		@Override
		public void init() {
			timer = (Timer) cpu.getHardware(Timer.class);
			listener = new TimerOverflowListener() {
				
				@Override
				public void overflow(final int timer) {
					if (timer != 0) {
						divider--;
						if (divider == -1) {
							divider = 16;
							smodCounter--;
							if (smodCounter == -1) {
								smodCounter = smod ? 0 : 1;
								txClockTriggered();
							}
						}
					}
				}
			};
			timer.addOverflowListener(listener);
		}

		@Override
		public void release() {
			timer.removeOverflowListener(listener);
		}
		
	}
}
