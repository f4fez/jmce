/**
   $Id: DirDiskCPM.java 695 2011-09-21 06:09:11Z mviara $

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
package jmce.sim.disk;

import java.io.*;

import jmce.sim.*;
import jmce.sim.cpm.*;
import jmce.util.Logger;

/**
 * Directory disk in CP/M format.
 * <p>
 * This class implements a special disk mapped to one directory and
 * created in memory on the fly.<p> Files on the root directory are
 * mapped to user 0 other users 1 ... 15 are mapped in sub directories
 * name with the user name. For example if the root directory is disks
 * the file WS.COM for the user 0 will be read/write from disks/WS.COM
 * and for the user 1 disks/1/WS.COM
 * 
 * <p>When the simulator is stopped if the property @see #setRO is
 * set to false the file are written back in the directory.<p>
 * If a special file name <tt>BOOTLOADER.BIN</tt> is present in the
 * directory it will be written in the reserved track.<p>
 * <p>
 * File attribute.
 * <p>
 * File attribute (ReadOnly,Global and Archive) are stored in the local
 * file system in a file name .NAME and contain the letter R for read
 * only, G for global and A for archive. So if the file WS.COM must be
 * read only in the local file system must exist a file named .WS.COM
 * with the LETTER R in the first line.
 * <p>
 * 
 * <h2>Properties</h2>
 * <ul>
 *  <li><b>volatile</b> Is set to true the disk will not be copied to
 *  the original directory when the simulator terminate. The default
 *  value is false.</li>
 *  <li><b>directory</b> Is the directory from where the CP/M disk is
 *  created.</li>
 *  <li><b>DPB</b> Is The CP/M DPB of the disk. The
 *  default value is set to jmce.sim.cpm.DPB3740</li>
 * </ul>
 * 
 * @author Mario Viara
 * @version 1.01
 */
public class DirDiskCPM extends MemoryDisk implements DiskCPM
{
	private static Logger log = Logger.getLogger(DirDiskCPM.class);
	static private final String bootloader = "BOOTLOADER.BIN";
	private DPB dpb;
	private String directory;
	private boolean vol = false;
	private CpmDisk disk = null;
	
	public DirDiskCPM()
	{
		setDPB(new DPB3740());
		setDirectory("data");
	}

	/**
	 * Check if the specified file is a valid CPM/FILE.
	 *
	 * A valid CP/M file can have up 8 character of file name , 3
	 * character of extension and must start with a letter or digit.
	 *
	 * @param name - File to check.
	 * 
	 * @return true if the file specified is a valid CP/M file
	 * false oterwise.
	 */
	private boolean isValidCpmFile(String name)
	{
		int dot = name.indexOf('.');

		/**
		 * No extension
		 */
		if (dot == -1)
		{
			if (name.length() > 8)
				return false;
			else
				return true;
		}

		int dot2 = name.indexOf(dot+1,'.');

		
		/** 2 extension */
		if (dot2 != -1)
			return false;

		/** Start with . */
		if (dot == 0)
			return false;

				 
		/** Name more than 8 char */
		if (dot > 8)
			return false;

		/** Extension more than 3 char */
		if (name.length() - dot > 4)
			return false;

		return true;
		
	}
	
	public void setDPB(DPB dpb)
	{
		this.dpb = dpb;
	}

	public void setDirectory(String s)
	{
		directory = s;
	}

	public DPB getDPB()
	{
		return dpb;
	}

	public String getDirectory()
	{
		return directory;

	}

	public boolean getVolatile()
	{
		return vol;
	}

	public void setVolatile(boolean mode)
	{
		vol = mode;
	}

	/**
	 * During the init process the disk must be mounted because the
	 * mount use disk access and the automatic mount occours when
	 * the emulated operating system acces the disk and this can
	 * cause race condition.
	 */
	public void init(Hardware parent) throws SIMException
	{
		setNumSector(dpb.getSPT());
		setNumHead(1);
		setNumTrack(dpb.getTRK());
		setSectorSize(DPB.SECSIZ);

		super.init(parent);

		mount();
	}

	public void destroy() throws SIMException
	{
		super.destroy();

		if (disk == null)
			return;
		
		if (vol)
		{
			log.info("Directory R/O");
			return;
		}
		
		disk = new CpmDisk(dpb,this);
		
		try
		{		
			disk.mount();
		}
		catch (Exception e)
		{
			throw new SIMIOException(directory,e.getMessage());
		}
		
		log.fine("Process "+disk+" Files="+disk.getFileCount());
		
		for (int i = 0 ; i < disk.getFileCount() ; i++)
		{
			CpmFile f = disk.getFileAt(i);
			
			String to = directory+"/";
			if (f.getUser() != 0)
				to += f.getUser()+"/";
			
			to += f.getName();
			
			log.fine("Write "+f+" as "+to);
			try
			{
				disk.getFile(f,to);
			}
			catch (Exception e)
			{
				throw new SIMIOException(f.getName(),e.toString());
			}
		}


		
	}

	/**
	 * Mount one single directory
	 */
	public void mountDir(String s,int u) throws Exception
	{
		File file = new File(s);

		if (file.isDirectory() == false)
			return;

		if (file.exists() == false)
			return;
		
		File files[] = file.listFiles();

		for (int i = 0 ; i < files.length ; i++)
		{
			file = files[i];
			log.fine("Process FILE="+file);

			if (file.isDirectory())
				continue;

			String name = file.getName().toUpperCase();

			/*
			 * If present process boot loader
			 */
			if (name.equals(bootloader))
				writeBootLoader(directory+"/"+bootloader);
			else
			{
				if (isValidCpmFile(name))
				{
					FileInputStream is = new FileInputStream(file);
					log.finer("Writing FILE="+file);
					disk.putFile(u,name,is);
					is.close();
				}
				else
					log.info(name+" is not a valid CP/M file");

			}

		}

		
	}
	
	public boolean mount() throws SIMException
	{
		if (isMounted())
			return true;

		super.mount();

		disk = new CpmDisk(dpb,this);


		try
		{
			disk.format();
			disk.mount();
			mountDir(directory,0);
			for (int u = 1 ; u < 16 ; u++)
				mountDir(directory+"/"+u,u);
			
			return true;
		}
		catch (Exception e)
		{
			throw new SIMIOException(directory,e.toString());
		}
	}

	private void writeBootLoader(String name) throws SIMException
	{
		log.fine("Write bootloader from "+name);
		byte sector[] = getBuffer();
		int t = 0;
		int h = 0;
		int s = 1;

		try
		{
			FileInputStream is = new FileInputStream(name);
			while ((is.read(sector)) > 0)
			{
				log.fine("Write T="+t+" H="+h+" S="+s);
				write(t,h,s);
				if (++s > getNumSector())
				{
					s = 1;
					if (++h >= getNumHead())
					{
						h = 0;
						t++;
					}
				}
			}

			is.close();
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(directory,e.toString());
		}
		
	}
	
	public String toString()
	{
		return "Dir="+directory+" "+getNumTrack()+"/"+getNumHead()+"/"+getNumSector()+"/"+getSectorSize()+" "+getDiskSizeString()+" R/O="+vol;
	}

}
