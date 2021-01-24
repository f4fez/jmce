package jmce.philips;

import jmce.sim.AbstractPeripheral;
import jmce.sim.CPU;
import jmce.sim.CycleListener;
import jmce.sim.Memory;
import jmce.sim.MemoryReadListener;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;

public class Adc extends AbstractPeripheral implements CycleListener {

	private int adcon;
	private int cycleCounter = -1;
	private int[] values;
	
	public Adc() {
		values = new int[8];
		setName("ADC");
	}
	
	@Override
	public void cycle(int n) throws SIMException {
		
		if (cycleCounter >= 0) {
			cycleCounter-=n;
			if (cycleCounter <= 0) {
				adcon &= 0xf7;
				adcon |= 0x10;
			}
		}
		
	}
	
	@Override
	public void registerCPU(CPU cpu) throws SIMException {
		super.registerCPU(cpu);
		cpu.addIOReadListener(P80c552Constants.ADCH, new MemoryReadListener() {
			
			@Override
			public int readMemory(Memory memory, int address, int value)
					throws SIMException {
				return (values[adcon & 0x07] >> 2) & 0xff;
			}
		});
		cpu.addIOReadListener(P80c552Constants.ADCON,  new MemoryReadListener() {
			
			@Override
			public int readMemory(Memory memory, int address, int value)
					throws SIMException {
				return adcon;
			}
		});
		cpu.addIOWriteListener(P80c552Constants.ADCON, new MemoryWriteListener() {
			
			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue)
					throws SIMException {
				
				if (((value & 0x08) != 0) && ((adcon & 0x08) == 0) ) {
					cycleCounter = 50;
				}
				
				adcon = value;
			}
		});
		
		cpu.addCycleListener(this);
	}
	

	/**
	 * @return the values
	 */
	public int getAdcValue(final int adc) {
		return values[adc];
	}

	/**
	 * @param values the values to set
	 */
	public void setAdcValue(final int adc, final int values) {
		this.values[adc] = values;
	}

	
}
