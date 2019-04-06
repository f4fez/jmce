/**
   $Id: TapeFile.java 627 2011-06-08 09:52:58Z mviara $

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

package jmce.sim.tape;


import jmce.sim.*;
import jmce.util.FastArray;
import jmce.util.Logger;

import java.io.*;



/**
 * Tape emulator on file.
 *
 * @author Mario Viara
 * @version 1.00
 * @since 1.01
 */
public class TapeFile extends AbstractTape
{
	private static Logger log = Logger.getLogger(TapeFile.class);
	private String config;
	private FastArray<TapeFileDecoder> decoders = new FastArray<TapeFileDecoder>();
	private boolean playing = false;
	private boolean recording = false;
	private InputStream is = null;
	private TapeFileDecoder decoder;
	private TapeData data = null;
	private int dataPtr;
	
	public TapeFile()
	{
		super("Tape");
	}

	public TapeFile(String name)
	{
		super(name);
	}
	
	public void reset() throws SIMException
	{
		super.reset();
		close();
		
	}

	public TapeData getTapeData()
	{
		return data;
	}
	
	public int getCurrentTapeData()
	{
		if (data == null)
			return 0;
		return dataPtr;
	}
	
	public int getNumTapeData()
	{
		if (data == null)
			return 0;

		return data.getSize();
	}
	
	private void close()
	{
		playing = false;
		recording = false;
		
		if (is != null)
		{
			try
			{
				is.close();
			}
			catch (Exception ignore)
			{
			}

			is = null;
		}

	}
	
	protected void addDecoder(TapeFileDecoder decoder)
	{
		decoders.add(decoder);
	}
	
	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	public void rewind() throws SIMException
	{
		close();
		notifyStop();
	}
	
	public void play() throws SIMException
	{
		dataPtr = 0;
		
		if (playing)
			return;
		if (config == null)
			throw new SIMException("No file specified");

		try
		{
			is = new FileInputStream(config);
			is.close();
		}
		catch (Exception ex)
		{
			throw new SIMException("File not found "+config);
		}


		if (decoders.getSize() == 0)
			throw new SIMException("No decoders installed");
		
		decoder = null;
		
		for (int i = 0 ; i < decoders.getSize() ; i++)
		{
			decoder = decoders.get(i);
			try
			{
				is = new FileInputStream(config);
				
				if (decoder.isFileSupported(is))
				{
					log.info("Decoder "+decoder+" for "+config);
					break;
				}
				
				is.close();
			}
			catch (Exception ignore)
			{
				log.info(ignore);
				close();
				decoder = null;
			}
		}

		if (decoder == null)
		{
			close();
			throw new SIMException("File "+config+" not supported");
		}

		/** Decode the file */
		try
		{
			data = decoder.decode(new BufferedInputStream(is));
		}
		catch (java.io.IOException ex)
		{
			log.info(ex);
			data = null;
		}
		
		close();
		notifyPlay();
		log.info(config+" decoded with "+decoder+" size="+data.getSize());
		playing = true;
	}

	public boolean isPlay()
	{
		return playing;
	}

	public boolean isRecording()
	{
		return recording;
	}
	
	public void rec() throws SIMException
	{
		recording = true;
		notifyRec();
		//throw new SIMException("REC not supported on file tape");
	}
	
	public void stop()  throws SIMException
	{
		close();
		notifyStop();
	}
	
	public TapePulse nextPulse()
	{
		if (data == null)
			return null;

		if (dataPtr >= data.getSize())
			return null;

		if (dataPtr == data.getSize() - 1)
		{
			log.info("Play terminated");
			try
			{
				stop();
			}
			catch (Exception ex)
			{
			}
		}
		
		return data.get(dataPtr++);
	}
	
			
}
