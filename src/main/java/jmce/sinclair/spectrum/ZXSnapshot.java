/**
   $Id: ZXSnapshot.java 512 2011-01-18 09:28:29Z mviara $

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
package jmce.sinclair.spectrum;

import java.io.*;

import jmce.sim.*;



import jmce.util.Logger;
import jmce.util.Hex;

/**
 * ZX Spectrum shapshot loader.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class ZXSnapshot extends jmce.sim.cpu.AbstractLoader
{
	private static Logger log = Logger.getLogger(ZXSnapshot.class);
	
	/**
	 * Memory map
	 */
	public static final int RAM_MEMORY_START	= 0x4000;

	protected Spectrum cpu;


	public ZXSnapshot()
	{
		super("z80");
	}


	public void setCPU(CPU cpu)
	{
		super.setCPU(cpu);
		this.cpu = (Spectrum)cpu;
	}
	

	/**
	 * Load snap shot in memory
	 */
	public void load(Memory m, String name,int address,LoadInfo info ) throws SIMException
	{
		
		File file = new File(name);
		int snapshotLength = (int)file.length();

		info.start = RAM_MEMORY_START;
		info.end   = snapshotLength + info.start;
		
		log.info("ZXSnapshot "+name+" lenght="+snapshotLength);
		try
		{
			FileInputStream is = new FileInputStream(file);

			// Crude check but it'll work (SNA is a fixed size)
			if ( (snapshotLength == 49179) ) {
				loadSNA(cpu, name, is );
			}
			else {
				loadZ80( name, is, snapshotLength );
			}

			is.close();
		}
		catch (java.io.IOException e)
		{
			throw new SIMIOException(name,e.toString());
		}
	}

	public int readBytes(InputStream is,int mem[],int len) throws java.io.IOException
	{
		for (int i = 0 ; i < len ; i++)
		{
			int c = is.read();
			if (c < 0)
				return i;

			mem[i] = c & 0xff;
		}

		return len;
	}

	public void loadSNA( Spectrum cpu,String name, InputStream is ) throws java.io.IOException,SIMException
	{
		log.info("loadSNA "+name);
		int        header[] = new int[27];

		readBytes(is,header,27);
		for (int i = 0 ; i < 49152 ; i++)
			cpu.setByte(RAM_MEMORY_START+i,is.read());

		cpu.I =  header[0];

		cpu.HL =  header[1] | (header[2]<<8);
		cpu.DE =  header[3] | (header[4]<<8);
		cpu.BC =  header[5] | (header[6]<<8);
		cpu.A  =  header[7];
		cpu.F  =  header[8];

		cpu.exx();
		cpu.ex_af_af1();

		cpu.HL = header[9]  | (header[10]<<8);
		cpu.DE =  header[11] | (header[12]<<8);
		cpu.BC = header[13] | (header[14]<<8);

		cpu.IY = header[15] | (header[16]<<8);
		cpu.IX = header[17] | (header[18]<<8);

		if ( (header[19] & 0x04)!= 0 ) {
			cpu.iff2 =  true ;
		}
		else {
			cpu.iff2 =  false;
		}

		cpu.R =  header[20];

		cpu.A = header[21];
		cpu.F = header[22];
		
		cpu.SP  =  header[23] | (header[24]<<8) ;

		
		switch( header[25] ) {
			case 0:
				cpu.im(0);
				break;
			case 1:
				cpu.im(1);
				break;
			default:
				cpu.im(2);
				break;
		}
		
		cpu.out( 254, header[26], 0 ); // border

		/* Emulate RETN to start */
		cpu.iff1 = cpu.iff2;
		//REFRESH( 2 );
		cpu.PC = cpu.pop();

	}


	public void loadZ80( String name, InputStream is, int bytesLeft ) throws SIMException,java.io.IOException
	{
		log.info("LoadZ80 "+name);

		int        header[] = new int[30];
		boolean    compressed = false;

		bytesLeft -= readBytes( is, header, 30 );

		cpu.A = header[0];
		cpu.F = header[1];

		cpu.BC = header[2] | (header[3] << 8);
		cpu.HL = header[4] | (header[5] << 8);

		cpu.PC = header[6] | (header[7]<<8);
		cpu.SP = header[8] | (header[9]<<8);

		cpu.I = header[10];
		cpu.R = header[11];

		int tbyte = header[12];
		if ( tbyte == 255 ) {
			tbyte = 1;
		}

		cpu.out( 254, ((tbyte >> 1) & 0x07), 0 ); // border

		if ( (tbyte & 0x01) != 0 ) {
			cpu.R = cpu.R | 0x80;
		}
		compressed = ((tbyte & 0x20) != 0);

		cpu.DE = header[13] | (header[14] << 8);

		cpu.ex_af_af1();
		cpu.exx();

		cpu.BC = header[15] | (header[16] << 8);
		cpu.DE = header[17] | (header[18] << 8);
		cpu.HL = header[19] | (header[20] << 8);

		cpu.A = header[21];
		cpu.F = header[22];

		cpu.ex_af_af1();
		cpu.exx();

		cpu.IY = header[23] | (header[24]<<8);
		cpu.IX = header[25] | (header[26]<<8);

		cpu.iff1 =  header[27] != 0;
		cpu.iff2 =  header[28] != 0 ;

		
		switch ( header[29] & 0x03 ) {
			case 0:
				cpu.im(0);
				break;
			case 1:
				cpu.im(1);
				break;
			default:
				cpu.im(2);
				break;
		}


		if ( cpu.PC == 0 )
		{
			loadZ80_extended( is, bytesLeft );

			return;
		}
		/* Old format Z80 snapshot */

		if ( compressed ) {
			int data[] = new int[ bytesLeft ];
			int addr   = RAM_MEMORY_START;

			int size = readBytes( is, data, bytesLeft );
			log.info("Byte "+size+" at "+Hex.formatWord(addr));
			int i    = 0;

			while ( (addr < 65536) && (i < size) ) {
				tbyte = data[i++];
				if ( tbyte != 0xed ) {
					cpu.setByte( addr, tbyte );
					addr++;
				}
				else {
					tbyte = data[i++];
					if ( tbyte != 0xed ) {
						cpu.setByte( addr, 0xed );
						i--;
						addr++;
					}
					else {
						int        count;
						count = data[i++];
						tbyte = data[i++];
						while ( (count--) != 0 ) {
							cpu.setByte( addr, tbyte );
							addr++;
						}
					}
				}
			}
		}
		else {
			for (int i = 0 ; i < 49152 ; i++)
				cpu.setByte(RAM_MEMORY_START+i,is.read());
			
		}

	}

	private void loadZ80_extended( InputStream is, int bytesLeft ) throws SIMException,java.io.IOException
	{
		int header[] = new int[2];
		bytesLeft -= readBytes( is, header, header.length );

		log.info("LoadZ80_extended");
		
		int type = header[0] | (header[1] << 8);

		switch( type ) {
			case 23: /* V2.01 */
				loadZ80_v201( cpu,is, bytesLeft );
				break;
			case 54: /* V3.00 */
				loadZ80_v300( cpu,is, bytesLeft );
				break;
			case 58: /* V3.01 */
				loadZ80_v301( cpu,is, bytesLeft );
				break;
			default:
				throw new SIMException( "Z80 (extended): unsupported type " + type );
		}
	}

	private void loadZ80_v201( Spectrum cpu,InputStream is, int bytesLeft ) throws SIMException,java.io.IOException
	{
		int header[] = new int[23];
		bytesLeft -= readBytes( is, header, header.length );

		cpu.PC =  header[0] | (header[1]<<8);

		/* 0 - 48K
		 * 1 - 48K + IF1
		 * 2 - SamRam
		 * 3 - 128K
		 * 4 - 128K + IF1
		 */
		int type = header[2];


		/*
		if ( type > 1 ) {
			throw new Exception( "Z80 (v201): unsupported type " + type );
		}
*/
		int data[] = new int[ bytesLeft ];
		readBytes( is, data, bytesLeft );

		for ( int offset = 0, j = 0; j < 3; j++ ) {
			offset = loadZ80_page( data, offset,type );
		}
	}

	private void loadZ80_v300( Spectrum cpu,InputStream is, int bytesLeft ) throws SIMException,java.io.IOException
	{
		int        header[] = new int[54];
		bytesLeft -= readBytes( is, header, header.length );

		cpu.PC =  header[0] | (header[1]<<8);

		/* 0 - 48K
		 * 1 - 48K + IF1
		 * 2 - 48K + MGT
		 * 3 - SamRam
		 * 4 - 128K
		 * 5 - 128K + IF1
		 * 6 - 128K + MGT
		 */
		int type = header[2];

		if ( type > 6 ) {
			throw new SIMException( "Z80 (v300): unsupported type " + type );
		}

		int data[] = new int[ bytesLeft ];
		readBytes( is, data,  bytesLeft );

		for ( int offset = 0, j = 0; j < 3; j++ ) {
			offset = loadZ80_page( data, offset,type );
		}
	}

	private void loadZ80_v301( Spectrum cpu,InputStream is, int bytesLeft ) throws SIMException,java.io.IOException
	{
		int        header[] = new int[58];
		bytesLeft -= readBytes( is, header,  header.length );

		cpu.PC = header[0] | (header[1]<<8);

		/* 0 - 48K
		 * 1 - 48K + IF1
		 * 2 - 48K + MGT
		 * 3 - SamRam
		 * 4 - 128K
		 * 5 - 128K + IF1
		 * 6 - 128K + MGT
		 * 7 - +3
		 */
		int type = header[2];

		if ( type > 7 ) {
			throw new SIMException( "Z80 (v301): unsupported type " + type );
		}

		int data[] = new int[ bytesLeft ];
		readBytes( is, data, bytesLeft );

		for ( int offset = 0, j = 0; j < 3; j++ ) {
			offset = loadZ80_page( data, offset ,type);
		}
	}

	private int page2address(int type,int page) throws SIMException
	{
		int addr = -1;

		if (type == 0) // Spectrum 48k
		{
			switch(page)
			{
				case 4:
					addr = 0x8000;
					break;
				case 5:
					addr = 0xc000;
					break;
				case 8:
					addr = 0x4000;
					break;
			}

		}
		else if (type == 4) // spectrum 128k
		{
			if (page >= 3 && page <= 10)
			{
				addr = 0xc000;
				cpu.out(0xfd,page-3,0x7f);
			}
		}

		if (addr == -1)
		{
			throw new SIMException("z80 page "+page+" type "+type+" unsupported");
		}

		log.info("z80 page "+page+" type "+type+" at "+Integer.toHexString(addr));

		return addr;

	}

	private int loadZ80_page( int data[], int i,int type ) throws SIMException {
		int blocklen;
		int page;

		blocklen  = data[i++];
		blocklen |= (data[i++]) << 8;
		page = data[i++];

		int addr = page2address(type,page);

		int        k = 0;
		while (k < blocklen) {
			int        tbyte = data[i++]; k++;
			if ( tbyte != 0xed ) {
				cpu.setByte(addr, ~tbyte);
				cpu.setByte(addr, tbyte);
				addr++;
			}
			else {
				tbyte = data[i++]; k++;
				if ( tbyte != 0xed ) {
					cpu.setByte(addr, 0);
					cpu.setByte(addr, 0xed);
					addr++;
					i--; k--;
				}
				else {
					int        count;
					count = data[i++]; k++;
					tbyte = data[i++]; k++;
					while ( count-- > 0 ) {
						cpu.setByte(addr, ~tbyte);
						cpu.setByte(addr, tbyte);
						addr++;
					}
				}
			}
		}

		if ((addr & 16383) != 0) {
			throw new SIMException( "Z80 (page): overrun" );
		}

		return i;
	}



}
