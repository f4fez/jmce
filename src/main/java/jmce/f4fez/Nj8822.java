package jmce.f4fez;

import jmce.sim.AbstractLockRangePll;
import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.sim.memory.MemoryBit;

/**
 * A : 7 bits M : 10 bits R : 11 bits
 * 
 *
 */
public class Nj8822 extends AbstractLockRangePll {

	public int LATCH_LEN = 28;

	// Total divider MP+A;
	private final int prescaler;

	/**
	 * Clock pin.
	 */
	private MemoryBit clock;

	/**
	 * DATA pin.
	 */
	private MemoryBit data;

	/**
	 * CE pin.
	 */
	private MemoryBit ce;

	/**
	 * Listener mapped to the clock pin.
	 */
	private MemoryWriteListener clockListener;

	/**
	 * Listener mapped to the ce pin.
	 */
	private MemoryWriteListener ceListener;

	/**
	 * Hold the previous clock pin state.
	 */
	private boolean oldClock;

	private boolean oldCe;

	private int bitCount = 0;

	private int latchWord = 0;

	private DecodeState state = DecodeState.IDLE;

	public Nj8822(final int clockRef, final int lockStart, final int lockEnd, final int prescaler) {
		super(clockRef, lockStart, lockEnd);
		this.prescaler = prescaler;

		clockListener = new MemoryWriteListener() {

			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				boolean currentClock = clock.get();
 				if (oldClock != currentClock && !currentClock && state == DecodeState.RECEIVE) {
					clockFall();
				}
				oldClock = currentClock;
			}
		};

		ceListener = new MemoryWriteListener() {

			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				boolean currentCe = ce.get();
				if (oldCe != currentCe) {
					if (currentCe) {
						state = DecodeState.RECEIVE;
						bitCount = 0;
						latchWord = 0;
					} else {
						state = DecodeState.IDLE;
						receiveComplete();
					}
				}
				oldCe = currentCe;
			}
		};
	}

	private void setCounter(final int m, final int a) {
		setLoopDivider(m * prescaler + a);
	}

	private void setCounter(final int m, final int a, final int ref) {
		setDividers(ref*2, m * prescaler + a);
	}

	/**
	 * @return the clock
	 */
	public MemoryBit getClock() {
		return clock;
	}

	/**
	 * @param clb the clock to set
	 */
	public void setClock(MemoryBit clock) {
		if (this.clock != null) {
			this.clock.removeMemoryWriteListener(clockListener);
		}
		this.clock = clock;
		this.clock.addMemoryWriteListener(clockListener);
	}

	/**
	 * @return the data
	 */
	public MemoryBit getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(MemoryBit data) {
		this.data = data;
	}

	/**
	 * @return the ce
	 */
	public MemoryBit getCe() {
		return ce;
	}

	/**
	 * @param ce the ce to set
	 */
	public void setCe(MemoryBit ce) {
		if (this.ce != null) {
			this.ce.removeMemoryWriteListener(ceListener);
		}
		this.ce = ce;
		this.ce.addMemoryWriteListener(ceListener);
	}

	private void clockFall() throws SIMException {
		latchWord <<= 1;
		if (data.get()) {
			latchWord++;
		}
		bitCount++;
	}

	private void receiveComplete() {
		if (bitCount < LATCH_LEN) {
			// Load A + M
			int m = latchWord & 0x03ff;
			int a = (latchWord >> 7) & 0x7f;
			setCounter(m, a);
		} else {
			// Load A + M + R
			int r = (latchWord) & 0x07ff;
			int m = (latchWord >> 11) & 0x03ff;
			int a = (latchWord >> 21) & 0x7f;
			setCounter(m, a, r);
		}
	}

	private enum DecodeState {
		IDLE, RECEIVE;
	}
}
