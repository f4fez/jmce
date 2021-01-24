/**
   $Id: Speaker.java 695 2011-09-21 06:09:11Z mviara $

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
package jmce.sim.audio;

import javax.sound.sampled.*;

import jmce.sim.*;
import jmce.util.Timer;
import jmce.util.TimerListener;
import jmce.util.Logger;


/**
 * Sample speaker peripheral.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Speaker extends jmce.sim.AbstractPeripheral
{
	private static Logger log = Logger.getLogger(Speaker.class);
	
	/** Feed audio streeam time in ms */
	private static final int feedTime = 20;

	/** Sample rate in sample for second */
	private int sample = 4000;
	private int freq = 0;

	private boolean installed = false;
	private AudioFormat fmt;
	private SourceDataLine line;
	private int bufferLen = 0;
	private boolean speaker = true;
	
	private byte buffer[] ;
    
    private float pwmRatio = 0.5f;

	public Speaker()
	{
		setName("Speaker");
		setSampleRate(8000);
	}

	public void setSpeaker(boolean mode)
	{
		if (!installed)
			return;
		
		if (mode != speaker)
		{
			
			speaker = mode;

			if (speaker)
				line.start();
			else
				line.stop();
			
			log.fine("Speaker="+mode);
			if (mode)
				line.start();
			else
				line.stop();
		}
	}
	
	public void setSampleRate(int n)
	{
		sample = n;
		buffer = new byte[sample];
		log.fine("Sample rate="+n);
	}
	
	public void init(Hardware p) throws SIMException
	{
		super.init(p);
		
		try
		{
			fmt = new AudioFormat(sample ,8,1,false,false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(fmt);
			line.start();

	
			Timer timer = new Timer(feedTime,true,new TimerListener()
			{
				public void timerExpired()
				{
					feed();
				}
			});

			installed = true;
			Timer.addTimer(timer);
			
		}
		catch (Exception e)
		{
			installed = false;
			System.out.println(e);
		}

	}


	/**
	 * Return true if the speaker is installed and correctly
	 * initialized.
	 *
	 * since 1.02
	 */
	public boolean isInstalled()
	{
		return installed;
	}
	
	public void setFreq(int f)
	{
		if (f == freq)
			return;
		freq = f;

		if (!installed)
			return;
		
		log.fine("Freq="+f);
		if (f <= 0 )
		{
			bufferLen = 0;
			line.stop();
		}
		else
		{
			if (speaker)
				line.start();
			bufferLen =  sample / f ;
			if (bufferLen > buffer.length)
				bufferLen = buffer.length;
			for (int i = 0 ; i < bufferLen ; i++)
				buffer[i] = (byte)(i < bufferLen*pwmRatio ? 255 : 0);
		}

		line.flush();
	}
    
    public void setPwmRatio(final float pwmRatio)
	{
		this.pwmRatio = pwmRatio;
	}
    
    public float getPwmRatio()
	{
		return pwmRatio;
	}

	/**
	 * Must be called every feedTime ms
	 */
	void feed()
	{
		if (!speaker)
			return;
		
		if (bufferLen <= 0)
			return;
		
		int count = 0;
		
		for (;;)
		{
			if (line.available() < bufferLen)
				break;
			count += line.write(buffer,0,bufferLen);
			
		}

		if (count > 0)
			log.finer("Written "+count+" bytes");
			
	}

	static public void delay(int n)
	{
		try
		{
			Thread.sleep(n);
		}
		catch (Exception ex)
		{
		}
	}

	static int music[] = {262,294,330,349,392,440,494,523,587,659};
	
	static public void main(String argv[])
	{
		Speaker s = new Speaker();
		try
		{
			s.init(null);
			for (int i = 0 ; i < music.length ; i++)
			{
				s.setFreq(music[i]);
				delay(200);
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		System.exit(0);
	}

	public String toString()
	{
		return "Speaker SR="+sample+" installed="+installed;
	}
}
