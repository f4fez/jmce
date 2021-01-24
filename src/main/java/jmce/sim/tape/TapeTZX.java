/**
   $Id: TapeTZX.java 814 2012-03-29 11:07:49Z mviara $

   Copyright (c) 2011, Mario Viara

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

package jmce.sim.tape;


import java.io.BufferedInputStream;
import java.io.InputStream;

import jmce.sim.TapePulse;
import jmce.util.Hex;

/**
 * Tape file decoder for ZX Spectrum TZX file.<p>
 * 
 * Supported block type : 10,12,13,21,22,24,25,30,32
 * <p>
 *
 * @author Mario Viara
 * 
 * @version 1.00
 * @since 1.02
 */
public class TapeTZX implements TapeFileDecoder
{
	private static jmce.util.Logger log = jmce.util.Logger.getLogger(TapeTZX.class);
	private static final int clock = 3500000;
	
	public boolean isFileSupported(InputStream is) throws java.io.IOException
	{
		byte header[] = new byte[10];

		if (is.read(header) != 10)
			return false;

		String signature = new String(header,0,7);

		if (!signature.equals("ZXTape!"))
		{
			log.info("Signature '"+signature+"' unsupported");
			return false;
		}

		
		return true;

	}

	/**
	 * Read 3 byte from the stream.
	 */
	private int get3byte(InputStream is) throws java.io.IOException
	{
		int n = is.read();
		n |= is.read() << 8;
		n |= is.read() << 16;

		return n;
	}
	
	/**
	 * Read a word (16 bit) from the input stream
	 */
	private int getWord(InputStream is) throws java.io.IOException
	{
		int low = is.read();
		int hi  = is.read();

		return low | (hi << 8);
	}

	/**
	 * Skip a number of byte from the input stream
	 */
	private void skip(InputStream is,int n) throws java.io.IOException
	{
		for (int i = 0 ; i < n ; i++)
			is.read();
	}

	
	public TapeData decode(BufferedInputStream is) throws java.io.IOException
	{
		int n;
		byte buffer[];

		log.info("Start decoding");

		TapeData data =  new TapeData(clock);

		for (;;)
		{
			
			int id = is.read();

			if (id == -1)
				break;
			
			log.info("Block id "+Hex.formatByte(id));
		

			switch (id)
			{
				case	0x10: /** Standard data */
					block10(data,is);
					break;

				case	0x12: /** Pure tone */
					block12(data,is);
					break;
					
				case	0x13: /** Pulse sequence */
					block13(data,is);
					break;
					
				case	0x14: /** Pure data block */
					block14(data,is);
					break;

				case	0x20: /** Pause */
					n = getWord(is);
					if (n == 0)
						n = 5000;
					block(data,is,n,0,0,0,0,0,0,0,0);
					break;
							
				case	0x21: /** Group start */
					n = is.read();
					buffer = new byte[n];
					if (is.read(buffer) != n)
						return null;
					log.info("Group start :"+new String(buffer,0,n));
					break;
					
				case	0x22: /** Group end */
					log.info("Group end");
					break;

				case	0x24: /** loop start */
					n = getWord(is);
					log.info("Loop start "+n);
					break;

				case	0x25: /** loop end */
					log.info("Loop end");
					break;
					
				case	0x30: /** description */
					n = is.read();
					buffer = new byte[n];
					if (is.read(buffer) != n)
						return null;
					log.info("Description :"+new String(buffer,0,n));
					break;

				case	0x32: /** archive info */
					n = getWord(is);
					skip(is,n);
					break;
					
				default:
					log.warning("Unknow block "+id);
					return null;
			}
		}
		
				
		return data;
	}


	/**
	 * Decode block 10 type
	 */
	private void block10(TapeData data,InputStream is) throws java.io.IOException
	{
		int pause = getWord(is);
		int len = getWord(is);
		
		is.mark(128);
		int v = is.read();
		is.reset();
		
		int pilot_length = v == 0x00 ? 8084 : 3220;
		block(data,is,pause,len,2168,pilot_length,667,735,855,1710,8);
				
	}

	/**
	 * Decode block 14 type - Pure data block
	 */
	private void block14(TapeData data,InputStream is) throws java.io.IOException
	{
		int bit0 = getWord(is);
		int bit1 = getWord(is);
		int last = is.read();
		int pause = getWord(is);
		int len = get3byte(is);


		block(data,is,pause,len,0,0,0,0,bit0,bit1,last);

	}

	/**
	 * Decode block 13
	 */
	private void block13(TapeData data,InputStream is) throws java.io.IOException
	{
		int n = is.read();
		for (int i = 0 ; i < n ; i++)
		{
			int p = getWord(is);
			data.add(new TapePulse(p));
		}
	}

	/**
	 * Decode block 12
	 */
	private void block12(TapeData data,InputStream is) throws java.io.IOException
	{
		int pilot = getWord(is);
		int pilotLen = getWord(is);

		block(data,is,0,0,pilot,pilotLen,0,0,0,0,0);

	}

	/**
	 * Decode data block
	 */
	void block(TapeData data,InputStream is,int pause,int len,int pilot,int pilotLen,int sync1,int sync2,int bit0,int bit1,int bitlast) throws java.io.IOException
	{
		int v;
		TapePulse pulsePilot = new TapePulse(pilot);
		TapePulse pulse0 = new TapePulse(bit0);
		TapePulse pulse1 = new TapePulse(bit1);

		log.info("Block Pause="+pause+" Pilot="+pilot+" sync1="+sync1+" sync2="+sync2+" len="+len);
		
		while (pilotLen-- > 0)
			data.add(pulsePilot);

		if (sync1 > 0)
			data.add(new TapePulse(sync1));

		if (sync2 > 0)
			data.add(new TapePulse(sync2));

		while (len-- > 0)
		{
			v = is.read();

			/** Determine the number of bit in this byte */
			int nbit = len == 0 ? bitlast : 8;
			
			for (int i = 0 ; i < nbit ; i++)
			{
				if ((v & 0x80) != 0)
				{
					data.add(pulse1);
					data.add(pulse1);
				}
				else
				{
					data.add(pulse0);
					data.add(pulse0);
				}

				v <<= 1;
			}
			
			
		}


		if (pause > 0)
		{
			data.add(new TapePulse(TapePulse.DATA_LOW,clock/1000));
			data.add(new TapePulse(TapePulse.DATA_NONE,(pause-1)*(clock/1000)));
		}

		
	}

	public String toString()
	{
		return "TZX 1.00";
	}
	
}

