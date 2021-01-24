package jmce.philips;

import jmce.intel.mcs51.MCS51;
import jmce.intel.mcs51.Ports;
import jmce.intel.mcs51.Timer2;
import jmce.sim.*;
import jmce.sim.memory.PlainMemory;

/**
 * Implementation of CPU for Philips 80C552.
 * <p>
 * Philips 80C552 is one extension of Intel 8051 with 256 bytes of internal
 * memory (memory DATA) and a new <tt>Timer2</tt> peripheral
 * <p>
 *
 * <h3>Implemented peripheral other than MCS51 :</h3>
 * <p>
 * <ul>
 *   <li>Port 4</li>
 *   <li>Port 5</li>
 *   <li>1 x 256 byte DATA memory.</li>
 * </ul>
 * 
 * <h3>Not Implemented peripherals :</h3>
 * <p>
 * <ul>
 *   <li>Timer2</li>
 *   <li>PWM</li>
 *   <li>I2c</li>
 *   <li>Capture latch</li>
 *   <li>Comparators</li>
 *   <li>T3 Watchdog</li>
 *   <li>ADC</li>
 * </ul>
 * 
 *
 */
public class P80c552 extends MCS51 implements P80c552Constants
{
	
	/**
	 * Default constructor
	 */
	public P80c552()
	{
		setName("p80c552");
	}

	
	@Override
	protected void initMemories() 
	{
		Memory m = getMemoryForName(DATA_MEMORY);
		if (m == null)
			addHardware(new PlainMemory(DATA_MEMORY,256));
		
		super.initMemories();
	}

	@Override
	protected void initNames()
	{
		super.initNames();
		setSfrName(P4,	"P4");
		setSfrName(P5,	"P5");
	}

	@Override
	protected void initPeripherals() throws SIMException
	{
		if (getHardware(Ports.class) == null)
			addHardware(new Ports(6));
		
		if (getHardware(Adc.class) == null) {
			addHardware(new Adc());
		}
		super.initPeripherals();
		
	}


}
