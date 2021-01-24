/**
   $Id: VIC6561.java 510 2011-01-18 09:25:07Z mviara $

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
package jmce.mos;

import java.awt.*;
import javax.swing.*;
import javax.sound.sampled.*;

import jmce.sim.*;
import jmce.util.Logger;
import jmce.util.Hex;


/**
 * VIC6561
 * <p>
 * Video Interface controller based over MOS 6561.
 * <p>
 * <h2>Register</h2>
 * The VIC6561 use 16 consecutive memory location mapped to the
 * register CRx from 0 to F.
 * <p>
 * <pre>
 * 00 - Control register 00
 *	BIT 7	Interlaced
 *	BIT 6-0	Screen origin X - Coordinate
 *	
 * 01 - Control register 01
 *	Screen origin Y - Coordinate
 *	
 * 02 - Control register 02
 *	Bit 7	Bit 9 of video/colour ram address.
 *	Bit 6-0	Number of columns
 *	
 * 03 - Control register 03
 *	Bit 7	User for light pen
 *	Bit 6-1	Number of rows
 *	Bit 0	Font size 0=8x8 , 1=8x16
 *	
 * 0A - Control register 0A
 *	Frequence channel 0 x 16
 *	
 * 0B - Control register 0B
 *	Frequence channel 1 x 8
 *	
 * 0C - Control register 0C
 *	Frequence channel 2 x 4
 *	
 * 0D - Control register 0D
 *	Frequence noise generator x 16
 *	
 * 0E - Control register 0E
 *	Volume 0 .. 15
 *	
 * 0F - Control register 0F
 *	Bit 7-4	Background color 0-15.
 *	Bit 3	Reverse screen when 0.
 *	BIT 2-0	Border color.
 * </pre>
 *  </ul>
 * </ul>
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.01
 */
public class VIC6561 extends AbstractPeripheral  implements MemoryWriteListener,MemoryReadListener,SwingHardware,CycleListener
{
	private static Logger log = Logger.getLogger(VIC6561.class);
	static public int sample = 44100;
	int ncycle;
	protected int scaleWidth = 1;
	protected int scaleHeight = 1;
	protected int sizeWidth = 8*22;
	protected int sizeHeight = 8*23;
	
	static public final int  noisepattern[] =
	{
		7, 30, 30, 28, 28, 62, 60, 56,120,248,124, 30, 31,143,  7,  7,193,192,224,
		241,224,240,227,225,192,224,120,126, 60, 56,224,225,195,195,135,199,  7, 30,
		28, 31, 14, 14, 30, 14, 15, 15,195,195,241,225,227,193,227,195,195,252, 60,
		30, 15,131,195,193,193,195,195,199,135,135,199, 15, 14, 60,124,120, 60, 60,
		60, 56, 62, 28,124, 30, 60, 15, 14, 62,120,240,240,224,225,241,193,195,199,
		195,225,241,224,225,240,241,227,192,240,224,248,112,227,135,135,192,240,224,
		241,225,225,199,131,135,131,143,135,135,199,131,195,131,195,241,225,195,199,
		129,207,135,  3,135,199,199,135,131,225,195,  7,195,135,135,  7,135,195,135,
		131,225,195,199,195,135,135,143, 15,135,135, 15,207, 31,135,142, 14,  7,129,
		195,227,193,224,240,224,227,131,135,  7,135,142, 30, 15,  7,135,143, 31,  7,
		135,193,240,225,225,227,199, 15,  3,143,135, 14, 30, 30, 15,135,135, 15,135,
		31, 15,195,195,240,248,240,112,241,240,240,225,240,224,120,124,120,124,112,
		113,225,225,195,195,199,135, 28, 60, 60, 28, 60,124, 30, 30, 30, 28, 60,120,
		248,248,225,195,135, 30, 30, 60, 62, 15, 15,135, 31,142, 15, 15,142, 30, 30,
		30, 30, 15, 15,143,135,135,195,131,193,225,195,193,195,199,143, 15, 15, 15,
		15,131,199,195,193,225,224,248, 62, 60, 60, 60, 60, 60,120, 62, 30, 30, 30,
		15, 15, 15, 30, 14, 30, 30, 15, 15,135, 31,135,135, 28, 62, 31, 15, 15,142,
		62, 14, 62, 30, 28, 60,124,252, 56,120,120, 56,120,112,248,124, 30, 60, 60,
		48,241,240,112,112,224,248,240,248,120,120,113,225,240,227,193,240,113,227,
		199,135,142, 62, 14, 30, 62, 15,  7,135, 12, 62, 15,135, 15, 30, 60, 60, 56,
		120,241,231,195,195,199,142, 60, 56,240,224,126, 30, 62, 14, 15, 15, 15,  3,
		195,195,199,135, 31, 14, 30, 28, 60, 60, 15,  7,  7,199,199,135,135,143, 15,
		192,240,248, 96,240,240,225,227,227,195,195,195,135, 15,135,142, 30, 30, 63,
		30, 14, 28, 60,126, 30, 60, 56,120,120,120, 56,120, 60,225,227,143, 31, 28,
		120,112,126, 15,135,  7,195,199, 15, 30, 60, 14, 15, 14, 30,  3,240,240,241,
		227,193,199,192,225,225,225,225,224,112,225,240,120,112,227,199, 15,193,225,
		227,195,192,240,252, 28, 60,112,248,112,248,120, 60,112,240,120,112,124,124,
		60, 56, 30, 62, 60,126,  7,131,199,193,193,225,195,195,195,225,225,240,120,
		124, 62, 15, 31,  7,143, 15,131,135,193,227,227,195,195,225,240,248,240, 60,
		124, 60, 15,142, 14, 31, 31, 14, 60, 56,120,112,112,240,240,248,112,112,120,
		56, 60,112,224,240,120,241,240,120, 62, 60, 15,  7, 14, 62, 30, 63, 30, 14,
		15,135,135,  7, 15,  7,199,143, 15,135, 30, 30, 31, 30, 30, 60, 30, 28, 62,
		15,  3,195,129,224,240,252, 56, 60, 62, 14, 30, 28,124, 30, 31, 14, 62, 28,
		120,120,124, 30, 62, 30, 60, 31, 15, 31, 15, 15,143, 28, 60,120,248,240,248,
		112,240,120,120, 60, 60,120, 60, 31, 15,  7,134, 28, 30, 28, 30, 30, 31,  3,
		195,199,142, 60, 60, 28, 24,240,225,195,225,193,225,227,195,195,227,195,131,
		135,131,135, 15,  7,  7,225,225,224,124,120, 56,120,120, 60, 31, 15,143, 14,
		7, 15,  7,131,195,195,129,240,248,241,224,227,199, 28, 62, 30, 15, 15,195,
		240,240,227,131,195,199,  7, 15, 15, 15, 15, 15,  7,135, 15, 15, 14, 15, 15,
		30, 15, 15,135,135,135,143,199,199,131,131,195,199,143,135,  7,195,142, 30,
		56, 62, 60, 56,124, 31, 28, 56, 60,120,124, 30, 28, 60, 63, 30, 14, 62, 28,
		60, 31, 15,  7,195,227,131,135,129,193,227,207, 14, 15, 30, 62, 30, 31, 15,
		143,195,135, 14,  3,240,240,112,224,225,225,199,142, 15, 15, 30, 14, 30, 31,
		28,120,240,241,241,224,241,225,225,224,224,241,193,240,113,225,195,131,199,
		131,225,225,248,112,240,240,240,240,240,112,248,112,112, 97,224,240,225,224,
		120,113,224,240,248, 56, 30, 28, 56,112,248, 96,120, 56, 60, 63, 31, 15, 31,
		15, 31,135,135,131,135,131,225,225,240,120,241,240,112, 56, 56,112,224,227,
		192,224,248,120,120,248, 56,241,225,225,195,135,135, 14, 30, 31, 14, 14, 15,
		15,135,195,135,  7,131,192,240, 56, 60, 60, 56,240,252, 62, 30, 28, 28, 56,
		112,240,241,224,240,224,224,241,227,224,225,240,240,120,124,120, 60,120,120,
		56,120,120,120,120,112,227,131,131,224,195,193,225,193,193,193,227,195,199,
		30, 14, 31, 30, 30, 15, 15, 14, 14, 14,  7,131,135,135, 14,  7,143, 15, 15,
		15, 14, 28,112,225,224,113,193,131,131,135, 15, 30, 24,120,120,124, 62, 28,
		56,240,225,224,120,112, 56, 60, 62, 30, 60, 30, 28,112, 60, 56, 63
	};

	class VIC6561Oscillator
	{
		int reg;
		int shift;
		boolean value;
		int counter;
		int noisectr;
		int osc;
		
		VIC6561Oscillator(int reg,int shift)
		{
			this.reg = reg;
			this.shift = shift;


		}


		boolean cycle()
		{
			
			/** Do nothing ig the channel is not active */
			if ((cr[reg] & 0x80) == 0)
				return false;

			if (counter == 0)
			{
				if (reg == 0x0d)
				{
					value = (noisepattern[(noisectr>>3)&1023] >> (noisectr&0x07) & 0x01) != 0 ? true: false;
					if (++noisectr > 1024)
						noisectr -= 1024;
				}
				else
				{
					osc = ((osc << 1) | ((osc & 128) >>> 7)) ^ 1;
					osc &= 0xff;
					value = (osc & 0x01) != 0 ? true : false;
				}
				
				counter = (~cr[reg] ) & 127;
				if (counter == 0)
					counter = 128;
				counter <<= shift;
			}

			counter--;
			
			return value;

				
		}

	}
	
	/** Color palette */
	private static Color colors[] =
	{	
		// Black
		new Color(0x000000),
		// White
		new Color(0xFFFFFF),
		// Red
		new Color(0xF00000),
		// Cyan
		new Color(0x00F0F0),
		// Magenta
		new Color(0x600060),
		// Green
		new Color(0x00A000),
		// Blue
		new Color(0x0000F0),
		// Yellow
		new Color(0xD0D000),
		// Orange
		new Color(0xC0A000),
		// Light orange
		new Color(0xFFA000),
		// Pink
		new Color(0xF08080),
		// Light cyan
		new Color(0x00FFFF),
		// Light magenta
		new Color(0xFF00FF),
		// Light green
		new Color(0x00FF00),
		// Light blue
		new Color(0x00A0FF),
		// Light yellow
		new Color(0xFFFF00),
	};

	/** Control registers */
	private int cr[] = new int[16];

	/** Array used in multi color mode.
	 *
	 * 0 - Background color
	 * 1 - External border color
	 * 2 - Foreground color
	 * 3 - Auxiliary color
	 */
	private Color multiColors[] = new Color[4];

	private VIC6561Oscillator channels[] = new VIC6561Oscillator[4];
	
	/** Swing component used for the GUI */
	class VIC6561Component extends JComponent implements jmce.swing.Repaintable
	{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Dimension size = null;
		
		public void updateComponent()
		{
			repaint();
		}

		public Dimension getPreferredSize()
		{
			if (size == null)
			{
				size = new Dimension(scaleWidth*sizeWidth,scaleHeight*sizeHeight);
				log.info(VIC6561.this+" SIZE="+size);
			}

			return size;
		}

		public void paintComponent(Graphics g)
		{
			Insets insets = getInsets();

			g.translate(insets.left,insets.top);
			g.setColor(multiColors[1]);
			g.fillRect(0,0,size.width,size.height);
			
			for (int r = 0 ; r < numRows ; r++)
				for (int c = 0 ; c < numColumns ; c++)
				{
					try
					{
						paintChar(g,r,c);
					}
					catch (Exception ex)
					{
					}
				}


		}


		void paintChar(Graphics g,int r,int c) throws SIMException
		{
			int ch = getVideoByte(r*numColumns + c + videoRam);
			int color = getVideoByte(r*numColumns + c + colourRam);

			Point origin = new Point((cr[0] & 0x7f)*scaleWidth,cr[1]*scaleHeight);
			
			/** multi color ? */
			if ((color & 0x08) != 0)
			{
				multiColors[2] = colors[color & 0x07];
				
				for (int y = 0 ; y < 8 ; y++)
				{
					int mask = getVideoByte(charRam+ch*8+y);

					for (int x = 0 ; x < 4 ; x ++)
					{
						int cl = (mask & 0xc0) >> 6;
						mask <<= 2;
						g.setColor(multiColors[cl & 0x03]);
						g.fillRect(origin.x+(c*8+x*2)*scaleWidth,origin.y+(r*8+y)*scaleHeight,scaleWidth*2,scaleHeight);

					}
				}

			}
			else	/** Hi resolution mode */
			{

				Color bg = multiColors[0];
				Color fg = colors[color & 0x07];

				if ((cr[CRF] & 0x08) == 0)
				{
					Color tmp = fg;
					fg = bg;
					bg = tmp;
				}
				
				for (int y = 0 ; y < 8 ; y++)
				{
					int mask = getVideoByte(charRam+ch*8+y);

					for (int x = 0 ; x < 8 ; x ++)
					{
						if ((mask & 0x80) != 0)
							g.setColor(fg);
						else
							g.setColor(bg);

						mask <<= 1;
						g.fillRect(origin.x+(c*8+x)*scaleWidth,origin.y+(r*8+y)*scaleHeight,scaleWidth,scaleHeight);
					}
				}
			}
		}
		
	};

	/** Control register 2 number of columns */
	static public final int CR2 = 0x02;

	/** Control register 3 number of rows */
	static public final int CR3 = 0x03;
	
	/** Control register 5 memory address */
	static public final int CR5 = 0x05;

	/** Control register E */
	static public final int CRE = 0x0E;
	
	/** Control register F color */
	static public final int CRF = 0x0F;
	
	/** Base address for this VIC */
	private int base = 0;

	/** Video ram address	*/
	private int videoRam = 0;

	/** Char generator address */
	private int charRam = 0;

	/** Colour ram address */
	private int colourRam = 0;
	
	/** Number of columns */
	private int numColumns = 22;

	/** Number of row */
	private int numRows = 23;

	/** Swing component */
	private VIC6561Component comp = new VIC6561Component();

	private SourceDataLine line;
	int cyclePerSample;
	int cycle;
	int bufferPtr;
	byte buffer[] = new byte[0];

	public VIC6561()
	{
		setName("VIC6561");

		channels[0] = new VIC6561Oscillator(0x0a,4);
		channels[1] = new VIC6561Oscillator(0x0b,3);
		channels[2] = new VIC6561Oscillator(0x0c,2);
		channels[3] = new VIC6561Oscillator(0x0d,4);

		try
		{

			AudioFormat fmt = new AudioFormat(sample ,8,1,false,false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(fmt);
			line.start();
			log.info("Line installed at "+sample+" sample rate");
		}
		catch (Exception ex)
		{
			System.out.println(ex);
			line = null;
		}

	}

	/**
	 * Set screen width
	 */
	public void setSizeWidth(int n)
	{
		sizeWidth = n;
	}

	/**
	 * Set screen height
	 */
	public void setSizeHeight(int n)
	{
		sizeHeight = n;
	}
	
	/**
	 * Set scale width
	 */
	public void setScaleWidth(int n)
	{
		scaleWidth = n;
	}

	/**
	 * Set scale height
	 */
	public void setScaleHeight(int n)
	{
		scaleHeight = n;
	}

	/**
	 * Return the base address
	 */
	public int getBase()
	{
		return base;
	}

	/**
	 * Set the base address
	 */
	public void setBase(int base)
	{
		this.base = base;
	}
	
	/**
	 * Convert an address from the video memory. Can be override
	 * from subclass if the 6561 is the address line are not all
	 * connected in the correct order. For example the VIC20
	 * have the A13 connected to the inverse of A15. This
	 * is necessary because the VIC6561 have only 14 address bit and
	 * normally is connected to cpu with 16 bit address.
	 *
	 * @param add - Address of memory.
	 *
	 * @return The address in CPU format.
	 */
	public int convertAddress(int add)
	{
		return add;
	}

	/*
	 * Return a byte from the cpu video memory after address translation
	 * if necessary.
	 */
	protected int getVideoByte(int add) throws SIMException
	{
		int target = convertAddress(add);

		int b = cpu.getByte(target);

		return b;
	}

	/**
	 * Check if the specified address is in the video memory. This
	 * function is used to update the screen.
	 **/
	private boolean checkRange(int video,int size,int add)
	{
		int target = convertAddress(video);
		if (add >= target && add <= target + size)
			return true;
		return false;
	}

	@Override
	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		
		for (int i = 0 ; i < 16 ; i++)
		{
			cpu.addIOWriteListener(base+i,this);
			cpu.addIOReadListener(base+i,this);
		}

		cpu.addMemoryWriteListener(new MemoryWriteListener()
		{
			public void writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
			{
				if (checkRange(videoRam,512,address))
					updateComponent();
				if (checkRange(colourRam,512,address))
					updateComponent();
			}

		});


		setClock(cpu.getClock());
		
		cpu.addCycleListener(this);
	}


	void setClock(long clock)
	{
		int n = (int)((clock / sample));
		if (n != cyclePerSample)
		{
			cyclePerSample = n;
			buffer = new byte[sample/50];
			bufferPtr = 0;
			log.info("CyclePerSample="+cyclePerSample);
		}
	}
	
	
	public int readMemory(Memory memory,int address,int value) throws SIMException
	{
		address  &= 0x0f;

		return cr[address];
	}
	
	public void writeMemory(Memory memory,int address,int value,int oldValue) throws SIMException
	{
		/** Use only bit 0-3 of address */
		address  &= 0x0f;

		if (cr[address] != value)
		{
			log.info("Changed CR"+address+" from "+Hex.formatByte(cr[address])+" to "+Hex.formatByte(value));
			cr[address] = value;

			updateRegister();
		}
	}

	private void updateComponent()
	{
		jmce.swing.Util.repaintLater(comp);
		//comp.repaint();
	}

	/**
	 * Called after each register change update all internal
	 * variable. Calculate the number of row, number of columns and the
	 * address of video memory.
	 */
	private void updateRegister()
	{
		numColumns = cr[CR2] & 0x7F;
		numRows = (cr[CR3] >> 1) & 0x3F;	
		videoRam = ((cr[CR2] & 0x80) << 2) | ((cr[CR5] & 0xF0) << 6);
		charRam = (cr[CR5] & 0x0f) << 10;
		colourRam = 0x1400 + 4 * (cr[CR2] & 0x80);

		multiColors[0] = colors[cr[CRF] >> 4];
		multiColors[1] = colors[cr[CRF] & 0x07];
		multiColors[3] = colors[cr[CRE] >> 4];

		
		updateComponent();

		log.info("VIC VRAM="+Hex.formatWord(videoRam)+" CGRAM="+Hex.formatWord(charRam)+" CRAM="+Hex.formatWord(colourRam));
		log.info("CPU VRAM="+Hex.formatWord(convertAddress(videoRam))+" CGRAM="+Hex.formatWord(convertAddress(charRam))+" CRAM="+Hex.formatWord(convertAddress(colourRam)));
	}



	public java.awt.Component getComponent()
	{
		return comp;
	}

	public void cycle(int n) throws SIMException
	{
		boolean value ;
		
		if (line == null)
			return;


		while (n-- > 0)
		{
			value = false;
			for (int i = 0 ; i < channels.length ; i++)
				value |= channels[i].cycle();

			/** Flush buffer */
			if (bufferPtr >= buffer.length)
			{
				if (line.available() >= buffer.length)
					line.write(buffer,0,buffer.length);
				bufferPtr = 0;

			}

			/** Sample buffer if necessary */
			if (++cycle >= cyclePerSample)
			{
				int v = 0;
				if (value)
					v = 255;
				v *= (cr[CRE] & 0x0f);
				v /= 15;

				buffer[bufferPtr++] = (byte)v;
				cycle = 0;
			}
			
		}

	}
	
	public String toString()
	{
		return getName()+ " at 0x"+Hex.formatWord(base);
	}


}
