/**
   $Id: PersistentMemory.java 632 2011-06-14 11:17:35Z mviara $

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
package jmce.sim.memory;

import java.io.*;
import java.util.StringTokenizer;

import jmce.sim.*;

import jmce.util.Hex;
import jmce.util.Timer;
import jmce.util.TimerListener;
import jmce.util.Logger;

/*
 * Memory implementation persistent<p>
 *
 * On initialization the memory is loaded form one file and
 * when one location is changed the memory is written to the same file.
 * <p>
 * For performance reason the file is update not at every changed but
 * at regular interval.
 *
 * @author Mario Viara
 * @version 1.00
 *
 */
public class PersistentMemory extends PlainMemory implements TimerListener,MemoryWriteListener,ResetListener
{
	private static Logger log = Logger.getLogger(PersistentMemory.class);
	private boolean changed = false;
	private String filename;
	
	public PersistentMemory()
	{
	}
	
	public PersistentMemory(String name,String filename,int size)
	{
		super(name,size);
		setFilename(filename);
	}

	public void writeMemory(Memory m,int a,int v,int o)
	{
		if (v != o)
		{
			if (cpu != null && cpu.isRunning())
				log.info(getName()+" changed at "+getMemoryName(a)+" from "+Hex.formatByte(o)+" to "+Hex.formatByte(v));
			changed = true;
		}
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}


	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addResetListener(this);
	}

	public void reset(CPU cpu) throws SIMException
	{
		load();
	}


	public void init(Hardware parent) throws SIMException
	{
		super.init(parent);


		addMemoryWriteListener(this);
		
		Timer.createTimer(1000,true,this);
	}


	/**
	 * Timer called at regular interval. Check if the memory is
	 * changed and than write the memory to one file if necessary.
	 */
	public void timerExpired()
	{
		// Memory changed ?
		if (!changed)
			return;
		FileOutputStream os = null;

		log.info("Save "+PersistentMemory.this.toString()+" to "+filename());

		try
		{
			changed = false;
			os = new FileOutputStream(new File(filename()));
			for (int i = 0 ; i < getSize() ; i++)
				os.write(getMemory(i));
			os.close();
		}
		catch (Exception e)
		{
			System.out.println(e);
			try
			{
				os.close();
			}
			catch (Exception discard)
			{
			}
		}
		try
		{
			os.close();
		}
		catch (Exception discard)
		{
		}
		os = null;

	}


	/**
	 * Return the filename used for read/write memory.
	 */
	protected String filename()
	{
		StringTokenizer st = new StringTokenizer(getFilename(),".");
		int n = st.countTokens();

		if (n == 1)
			return getFilename()+"."+getSize();
		else
		{
			String s = "";
			for (int i = 0 ; i < n ; i++)
			{
				String s1 = st.nextToken();

				if (i == n - 1)
					s+= "."+getSize();
				if (i > 0)
					s += ".";
				s += s1;
			}

			return s;
		}
	}

	protected void load() throws SIMException
	{
		File file = new File(filename());

		try
		{
			if (file.exists())
			{
				log.info("Loading from "+file);
				FileInputStream is = new FileInputStream(file);

				int i = 0;
				int c;

				while ((c = is.read()) >= 0)
					setMemory(i++,c);
				is.close();
			}
		}
		catch (java.io.FileNotFoundException ex)
		{
			throw new SIMIOException(filename(),"File not found"); 
		}
		catch (java.io.IOException ex)
		{
			throw new SIMIOException(filename(),"Error reading"); 

		}

	}


}

   