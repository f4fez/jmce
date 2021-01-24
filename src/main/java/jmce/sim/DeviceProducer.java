/**
 * $Id: DeviceProducer.java 316 2010-09-09 06:26:13Z mviara $
 */
package jmce.sim;

/**
 * Produce data for device.
 *
 * <p>
 * When new data are available a <code>Device</code> must call all
 * registered <code>DeviceSonsumer<E>.
 * 
 * @author Mario Viara
 * @version 1.00
 */
public interface DeviceProducer<E>
{
	public void addConsumer(DeviceConsumer<E> cc) throws SIMException;
}
