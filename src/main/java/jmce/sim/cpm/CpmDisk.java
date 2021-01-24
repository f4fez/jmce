/**
   $Id: CpmDisk.java 632 2011-06-14 11:17:35Z mviara $

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
package jmce.sim.cpm;


import jmce.sim.*;
import jmce.util.Logger;
import java.io.*;

import jmce.util.FastArray;

/**
 * CP/M File system implementation.
 * <p>
 * Simple implementation of the CP/M file system. This class permit to
 * format a CP/M disk and read,write and delete one file from the disk.
 * <p>
 * This version work only for disk with DPB.exm == 0.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.02
 */
public class CpmDisk
{
	private static Logger log = Logger.getLogger(CpmDisk.class);

	/** Phisical disk */
	private Disk disk;

	/** Disk parametar block */
	private DPB	dpb;

	/** Array with directory entry used */
	private boolean directoryUsed[];

	/** Array with disk block used */
	private boolean blockUsed[];

	/** Buffer for one FCB */
	private byte fcbBuffer[] = new byte[DPB.SECSIZ];

	/** Buffer for one disk block */
	private byte block[];

	/** Vector with all files */
	private FastArray<CpmFile> files = new FastArray<CpmFile>();

	/**
	 * Constructor.
	 *
	 * @param dpb - Disk parametar block
	 * @param disk - Phisical disk
	 */
	public CpmDisk(DPB dpb,Disk disk)
	{
		this.dpb = dpb;
		this.disk = disk;

		directoryUsed = new boolean[dpb.drm+1];
		blockUsed	  = new boolean[dpb.dsm+1];
		block		  = new byte[getBlockSize()];
	}

	/**
	 * Format the disk with the CP/M file system
	 */
	public void format() throws Exception
	{
		
		for (int i = 0 ; i <= dpb.getOFF(); i++)
		{
			disk.setTrack(i);
			disk.format();
		}

		for (int i = 0 ; i < block.length ; i++)
			block[i] = FCB.DELETED;

		for (int i = 0 ; i < getNumDirBlock() * 2 ; i++)
			writeBlock(i,block);
	}
	
	public CpmFile getFileAt(int index)
	{
		return files.get(index);
	}

	public int getFileCount()
	{
		return files.getSize();
	}

	private CpmFile searchFileException(int user,String name) throws Exception
	{
		CpmFile file = searchFile(user,name);

		if (file == null)
			throw new Exception ("File not found "+user+":"+name);

		return file;
	}

	private CpmFile searchFile(int user,String name)
	{
		for (int i = 0 ; i < getFileCount() ; i++)
		{
			CpmFile file = getFileAt(i);

			if (file.user == user && file.name.equals(name))
				return file;
		}

		return null;
	}

	private int getFCBBlockLength()
	{
		return dpb.dsm < 0x100 ? 1 : 2;
	}

	private int getFCBBlockCount()
	{
		return 16 / getFCBBlockLength();
	}

	private int getFCBBlock(FCB fcb,int block)
	{
		if (getFCBBlockLength() == 1)
			return fcb.getBlockByte(block);
		else
			return fcb.getBlockWord(block);
	}

	private void putFCBBlock(FCB fcb,int n,int block)
	{
		if (getFCBBlockLength() == 1)
			fcb.setBlockByte(n,block);
		else
		{
			fcb.setBlockWord(n,block);
		}

	}

	private void writeFCB(int dir,FCB fcb) throws Exception
	{
		int offset = dir * FCB.SIZE;
		int sector = offset / DPB.SECSIZ;
		offset = offset % DPB.SECSIZ;

		//log.info("Write FCB #"+dir+" "+fcb.getFileName() +" at sector "+sector+" offset "+offset);
		readSector(sector,fcbBuffer);
		fcb.getBuffer(fcbBuffer,offset);

		writeSector(sector,fcbBuffer);

	}

	private void  readFCB(int dir,FCB fcb) throws Exception
	{
		int offset = dir * 32;
		int sector = offset / 128;
		offset = offset % 128;

		readSector(sector,fcbBuffer);

		fcb.setBuffer(fcbBuffer,offset);
		//log.info("Read FCB "+dir+" at "+sector+" as "+fcb.getFileName());
	}

	public void writeSector(int secno,byte buffer[]) throws Exception
	{
		int track,sector;

		track = secno / dpb.getSPT() + dpb.getOFF();
		sector = secno % dpb.getSPT() + 1;
		sector = dpb.translateSector(sector);

		writeSector(track,sector,buffer);
	}

	public void writeSector(int track,int sector,byte buffer[]) throws Exception
	{

		System.arraycopy(buffer,0,disk.getBuffer(),0,DPB.SECSIZ);

		disk.setTrack(track);
		disk.setHead(0);
		disk.setSector(sector);
		disk.write();

	}


	public void readSector(int track,int sector,byte buffer[]) throws Exception
	{
		disk.setTrack(track);
		disk.setHead(0);
		disk.setSector(sector);
		disk.read();
		System.arraycopy(disk.getBuffer(),0,buffer,0,DPB.SECSIZ);
	}

	public void readSector(int secno,byte buffer[]) throws Exception
	{
		int track,sector;

		track = secno / dpb.getSPT() + dpb.getOFF();
		sector = secno % dpb.getSPT() + 1;
		//int old = sector;
		sector = dpb.translateSector(sector);

		//log.info("Read  secno "+secno+" T="+track+" S="+sector+" ("+old+")");
		readSector(track,sector,buffer);
	}

	public int getNumDirBlock()
	{
		int count = 0;

		for (int i = 0 ; i < 8 ; i++)
		{
			int mask = 1 << i;
			if ((dpb.getAL0() &  mask) != 0)
				count++;
			if ((dpb.getAL1() & mask) != 0)
				count++;
		}

		return count;
	}
	
	public void mount() throws Exception
	{
		if (dpb.getEXM() != 0)
			throw new Exception("DPB.EXM="+dpb.getEXM()+" not supported");
		
		FCB fcb = new FCB();

		disk.mount();
		
		// Mark all block as not used
		for (int i = 0 ; i < blockUsed.length ; i++)
			blockUsed[i] = false;

		// Mark directory block as used
		for (int i = 0 ; i < 8 ; i++)
		{
			if ((dpb.getAL0() & (1 << (7 - i))) != 0)
				blockUsed[i] = true;
			if ((dpb.getAL1() & (1 << (7 -i))) != 0)
				blockUsed[8+i] = true;
		}

		files.clear();

		/**
		 * Read all FCB entryes
		 */
		for (int i = 0 ; i <= dpb.drm ; i++)
		{
			readFCB(i,fcb);

			if (fcb.isDeleted())
			{
				directoryUsed[i] = false;
				continue;
			}
				
			int user	= fcb.getUser();
			fcb.getEX();
			fcb.getRC();

			directoryUsed[i] = true;

			// Skip not valid user
			if (user > 31)
				continue;



			String fileName = fcb.getFileName();


			CpmFile file = searchFile(user,fileName);

			/**
			 * Create one new entry for this file if do not
			 * exist.
			 */
			if (file == null)
			{
				file = new CpmFile(user,fileName);
				file.setAttributeGlobal(fcb.getAttributeGlobal());
				file.setAttributeFifo(fcb.getAttributeFifo());
				file.setAttributeReadOnly(fcb.getAttributeReadOnly());
				file.setAttributeArchive(fcb.getAttributeArchive());
				files.add(file);
			}

			file.addFCB(i);

			// Read block number from FCB
			for (int j = 0 ; j < getFCBBlockCount() ; j++)
			{

				int b = getFCBBlock(fcb,j);
				if (b == 0)
					break;
				blockUsed[b] = true;
				file.addBlock(b);
			}
		}



	}

	public int getNumBlockUsed()
	{
		int block = 0;

		for (int i = 0 ; i < blockUsed.length ; i++)
			if (blockUsed[i] == true)
				block++;

		return block;
	}

	int getBlockSize()
	{
	
		return (dpb.getBLM() + 1) * DPB.SECSIZ;
	}

	public int getNumDirectoryUsed()
	{
		int dir = 0;

		for (int i = 0 ; i < directoryUsed.length ; i++)
			if (directoryUsed[i] == true)
				dir++;

		return dir;

	}

	public void stat(String name,int value,String unit)
	{
		while (name.length() < 32)
			name = " "+name;

		String s = ""+value;
		while (s.length() < 10)
			s = " "+s;

		log.info(name+" = "+s+" "+unit);

	}

	private void stat(String name,int value)
	{
		stat(name,value,"");
	}

	public void stat()
	{
		int usedBlock;
		//dpb.dump();
		stat("Reserved track",dpb.getOFF());
		stat("Total directory",dpb.drm+1);
		stat("Directory used",getNumDirectoryUsed());
		stat("Directory free",dpb.drm+1-getNumDirectoryUsed());

		usedBlock = getNumBlockUsed();

		stat("Block size",getBlockSize(),"Bytes");
		stat("Space configured ",dpb.dsm+1,"Block");
		stat("Space used",usedBlock,"Block");
		stat("Space available",dpb.dsm+1-usedBlock,"Block");


		stat("Space configured",((dpb.dsm+1)*getBlockSize()) / 1024,"KB");
		stat("Space used",((usedBlock*getBlockSize())) / 1024,"KB");
		stat("Space free",(((dpb.dsm+1-usedBlock)*getBlockSize())) / 1024,"KB");

	}

	public void readBlock(int block,byte buffer[]) throws Exception
	{
		int sector = (dpb.blm + 1) * block;

		//log.info("Read Block # "+block + " sector "+sector);
		for (int i = 0 ; i <= dpb.blm ; i++)
		{
			readSector(sector++,fcbBuffer);
			System.arraycopy(fcbBuffer,0,buffer,i*DPB.SECSIZ,DPB.SECSIZ);
		}
	}

	public void writeBlock(int block,byte buffer[]) throws Exception
	{
		int sector = (dpb.blm + 1) * block;

		//log.info("Write block "+block+" at sector "+sector);
		
		for (int i = 0 ; i <= dpb.blm ; i++)
		{
			System.arraycopy(buffer,i*DPB.SECSIZ,fcbBuffer,0,DPB.SECSIZ);
		
			writeSector(sector++,fcbBuffer);
		}
	}

	private int allocateBlock() throws Exception
	{
		for (int i = 0 ; i < block.length ; i++)
			if (blockUsed[i] == false)
			{
				//log.info("Allocate block # "+i);
				blockUsed[i] = true;
				return i;
			}

		throw new Exception("Out of disk space");
		
	}
	
	private int allocateFCB() throws Exception
	{
		for (int i = 0 ; i < directoryUsed.length ; i++)
			if (directoryUsed[i] == false)
			{
				//log.info("Allocate fcb # "+i);
				directoryUsed[i] = true;
				return i;
			}

		throw new Exception("No free FCB entry");
		
	}

	public int getRecordForFCB()
	{
		return (dpb.exm + 1) * DPB.SECSIZ;
	}
	
	public void putFile(int user,String name,java.io.InputStream is) throws Exception
	{
		FCB fcb = new FCB();
		int i;
		int entry;
		int blockCount = 0;
		int record = 0;
		
		CpmFile file = searchFile(user,name);
		if (file != null)
			throw new Exception(name+" Already exist");

		file = new CpmFile(user,name);
		fcb.clear();
		fcb.setUser(user);
		fcb.setFileName(name);
		fcb.setEX(0);
		fcb.setRC(0);
		
		entry = allocateFCB();

		files.add(file);
		
		// Write empty directory
		writeFCB(entry,fcb);

		file.addFCB(entry);

		
		for (;;)
		{
			
			int byteCount = is.read(block,0,getBlockSize());
			if (byteCount <= 0)
				break;
			
			/** Fill the rest of the block with 1A */
			for (i = byteCount ; i < getBlockSize() ; i++)
				block[i] = 0x1A;
			
			int recordCount = (byteCount + DPB.SECSIZ - 1) / DPB.SECSIZ;

			while (recordCount > 0)
			{
				if (blockCount >= getFCBBlockCount())
				{
					entry = allocateFCB();
					fcb.clearBlocks();
					fcb.setRC(0);
					fcb.setEX(fcb.getEX()+1);
					//fcb[12] = (byte)(fcb[12]+getRecordForFCB()/128);
					writeFCB(entry,fcb);
					file.addFCB(entry);
					blockCount = 0;
				}
				
				int blockNo = allocateBlock();
				putFCBBlock(fcb,blockCount++,blockNo);
				if (recordCount > dpb.blm + 1)
					record = dpb.blm + 1;
				else
					record = recordCount;

				recordCount -= record;

				record += fcb.getRC();

				record &= 0xff;
				
				if (record > 128)
				{
					fcb.setEX(fcb.getEX()+1);
					if (record > 128)
						record -= 128;
				}
				fcb.setRC(record);
				writeFCB(entry,fcb);
				file.addBlock(blockNo);
				writeBlock(blockNo,block);
			}
			
		}
		
		
		
	}

	public int getFileSize(CpmFile file) throws Exception
	{
		int size = 0;
		FCB fcb = new FCB();
		
		for (int i  = 0 ; i < file.getFCBCount() ; i++)
		{
			readFCB(file.getFCBAt(i),fcb);
			int ex = (fcb.getEX() & 0x1f) & ~dpb.exm;
			int record = fcb.getRC() & 0xff;
			size = ex * 128 + ((dpb.exm & fcb.getEX())+1) * 128 - (128 - record);
		}

		return size;
	}

	public void getFile(int user,String name,String to) throws Exception
	{
		FileOutputStream os = new FileOutputStream(to);
		getFile(user,name,os);
		os.close();
	}
	
	public void getFile(int user,String name,java.io.OutputStream os) throws Exception
	{
		CpmFile file = searchFileException(user,name);
		 getFile(file,os);

	}

	public void getFile(CpmFile file,String name) throws Exception
	{
		FileOutputStream os = new FileOutputStream(name);
		getFile(file,os);
		os.close();
	}
	
	public void getFile(CpmFile file,java.io.OutputStream os) throws Exception
	{
		int length = getFileSize(file) * 128;

		for (int i = 0; i < file.getBlockCount() ; i++)
		{
			readBlock(file.getBlockAt(i),block);
			int count = getBlockSize();
			if (length < count)
				count = length;
			
			os.write(block,0,count);
			
			length -= count;
		}
		
	}
	
	public void deleteFile(int user,String name) throws Exception
	{
		CpmFile file = searchFileException(user,name);

		for (int i = 0 ; i < file.getBlockCount() ; i++)
		{
			//log.info("Free block "+file.getBlockAt(i));
			blockUsed[file.getBlockAt(i)] = false;
		}

		FCB fcb = new FCB();
		
		for (int i = 0 ; i < file.getFCBCount() ; i++)
		{
			int entry = file.getFCBAt(i);
			//log.info("free FCB "+entry);
			readFCB(entry,fcb);
			fcb.setDeleted();
			writeFCB(entry,fcb);
			directoryUsed[entry] = false;
		}

		files.remove(file);
	}

	public void umount() throws Exception
	{
		disk.dismount();
	}

	public String toString()
	{
		return "CP/M Disk "+disk;
	}
}
