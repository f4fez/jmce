/**
   $Id: Z80Pack.java 596 2011-05-24 07:12:27Z mviara $

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
package jmce.z80pack;

import jmce.sim.*;
import jmce.sim.serial.SerialFile;
import jmce.sim.serial.TCPServer;
import jmce.sim.serial.TCPClient;

/**
 * Z80Pack system implementation.
 *<p>
 *
 * This class implement the main CPU for Z80Pack and configure the
 * peripherals to the default value.
 * 
 * <p>
 * <h2>Implemented peripheral</h2> :
 * <ul>
 *   <li>Z80 cpu</li>
 *   <li>4 x IBM 3740 floppy disk.</li>
 *   <li>2 x 4 MB harddisk.</li>
 *   <li>1 x 512 MB harddisk.</li>
 *   <li>1 x serial console connected to CRT.</li>
 *   <li>5 x serial port for networking.</li>
 *   <li>1 x serial auxiliary port.</li>
 *   <li>1 x serial printer port.</li>
 *   <li>1 x interrupt timer every 10 ms.</li>
 *   <li>1 x hardware RTC.</li>
 *   <li>1 x automatic boot loader from first drive.</li>
 * </ul>
 *
 * <h2>Peripheral not included in the standard Z80Ppack</h2>
 * <ul>
 *  <li>1 x System kit peripheral</li>
 * </ul>
 * 
 * <h2>Default configuration :</h2>
 * <ul>
 *   <li>Drive A file z80pack/disks/library/cpm3-1.dsk with bootable
 *   CP/M 3.0 operating system</li>
 *   <li>Drive B z80pack/disks/library/cpm2-2.dsk with CP/M 3.0
 *   utilites and source.</li>
 *   <li>Drive C directory disks/fd1</li>
 *   <li>Drive D directory disks/fd2</li>
 *   <li>Drive I z80pack/disks/library/hd-work.dsk Empty harddisk.</li>
 *   <li>Drive J Directory disks/hd Many CP/M application</li>
 * </ul>
 *
 * <p>
 * With the binary distribution only the CP/M 3.0 disk are available,
 * for space reason, but more operating systems and utilities can be downloaded
 * <a href="http://www.unix4fun.org/z80pack/#download"/>here</a>
 * from the Z80pack web site.
 * 
 * @author Mario Viara
 * @version 1.00
 *
 * @see jmce.sim.SysKit
 */
public class Z80Pack extends jmce.zilog.z80.Z80 implements Z80PackConstants
{
	public Z80Pack()
	{

	}

	protected void initMemories()
	{
		jmce.sim.Memory m = getMemoryForName(MAIN_MEMORY);
		if (m == null)
			addHardware(new Memory());

		super.initMemories();
	}
	
	protected void initPeripherals() throws SIMException
	{
		/**
		 * Install console
		 */
		if (getHardware(Console.class) == null)
		{
			Console c = new Console();
			jmce.sim.terminal.Terminal t;
			t = jmce.sim.terminal.Terminal.createTerminal();
			c.addHardware(t);
			c.setConnected(t);
			addHardware(c);

		}

		/**
		 * Install FDC
		 */
		if (getHardware(FDC.class) == null)
		{
			FDC fdc = new FDC();
			
			fdc.addImageDiskIBM3740("z80pack/disks/library/cpm3-1.dsk");	// A 0
			fdc.addImageDiskIBM3740("z80pack/disks/library/cpm3-2.dsk");	// B 1
			fdc.addDirDiskIBM3740("disks/fd1");			// C 2
			fdc.addDirDiskIBM3740("disks/fd2");			// D 3

			fdc.addNullDisk();
			fdc.addNullDisk();
			fdc.addNullDisk();
			fdc.addNullDisk();
			
			fdc.addImageDiskZ80PackHD("z80pack/disks/library/hd-work.dsk");		// I 8
			fdc.addDirDiskZ80PackHD("disks/hd");					// J 9

			fdc.addNullDisk();
			fdc.addNullDisk();
			fdc.addNullDisk();
			fdc.addNullDisk();
			fdc.addNullDisk();
			fdc.addNullDisk();


			addHardware(fdc);
		}
		
		/**
		 * Install boot loader.
		 */
		if (getHardware(jmce.sim.disk.BootLoader.class) == null)
			addHardware(new jmce.sim.disk.BootLoader());
		
		/**
		 * Install timer
		 */
		if (getHardware(Timer.class) == null)
			addHardware(new Timer());

		/**
		 * Install software delay
		 */
		if (getHardware(Delay.class) == null)
			addHardware(new Delay());
		
		/**
		 * Install network interfaces
		 */
		if (getHardware(Network.class) == null)
		{
			Network n;
			Device<Integer> s;
			
			n = new Network("Network # 1",NETWORK_SERVER_BASE_1);
			s = new TCPServer(4040);
			n.addHardware(s);
			n.setConnected(s);
			addHardware(n);

			n = new Network("Network # 2",NETWORK_SERVER_BASE_2);
			s = new TCPServer(4042);
			n.addHardware(s);
			n.setConnected(s);
			addHardware(n);

			n = new Network("Network # 3",NETWORK_SERVER_BASE_3);
			s = new TCPServer(4044);
			n.addHardware(s);
			n.setConnected(s);
			addHardware(n);

			
			n = new Network("Network # 4",NETWORK_SERVER_BASE_4);
			s = new TCPServer(4046);
			n.addHardware(s);
			n.setConnected(s);
			addHardware(n);

			n = new Network("Network # 5",NETWORK_CLIENT_BASE_1);
			s = new TCPClient(4002);
			n.addHardware(s);
			n.setConnected(s);
			addHardware(n);

		}

		/**
		 * Install printer
		 */
		if (getHardware(Printer.class) == null)
		{
			Printer p = new Printer();
			SerialFile s = new SerialFile("printer.txt");
			p.addHardware(s);
			p.setConnected(s);
			addHardware(p);
		}		

		if (getHardware(SysKit.class) == null)
			addHardware(new SysKit());

	}


}
