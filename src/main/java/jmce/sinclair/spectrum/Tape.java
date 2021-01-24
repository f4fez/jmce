/**
   $Id: Tape.java 627 2011-06-08 09:52:58Z mviara $

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
package jmce.sinclair.spectrum;

import jmce.sim.*;
import jmce.util.Logger;
import jmce.util.Hex;


/**
 *
 * Tape interface for sinclair spectrum.
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 *
 * @since 1.02
 */
public class Tape extends jmce.sim.tape.TapeFile implements CycleListener
{
	private static Logger log = Logger.getLogger(Tape.class);
	private int pulse;
	private Spectrum spectrum;
	private TapePulse tp;
	
	public Tape()
	{
		super("ZX-Tape");

		/** Set default tape file */
		setConfig("sinclair/binvader.tzx");
		addDecoder(new jmce.sim.tape.TapeTZX());
	}



	public void reset() throws SIMException
	{
		super.reset();
		pulse= 0;
		tp = null;
		stop();
		setPower(true);
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);

		cpu.addCycleListener(this);

		spectrum = (Spectrum)cpu;
	}

	public void cycle(int n) throws SIMException
	{
		if (!isPlay() || !getPower())
			return;

		if (pulse < 0)
			return;
		
		while (n > 0)
		{
			if (pulse >= n)
			{
				pulse -= n;
			 	n = 0;
			}
			else
			{
				n -= pulse;
				pulse = 0;
			}

			if (pulse == 0)
			{
				
				if (tp != null)
				/** Set tape in if necessary */
				switch (tp.getType())
				{
					case	TapePulse.DATA_LOW:
						spectrum.setTapeIn(false);
						break;
					case	TapePulse.DATA_HIGH:
						spectrum.setTapeIn(true);
						break;
					case	TapePulse.DATA_TOGGLE:
						spectrum.setTapeIn(!spectrum.getTapeIn());
						break;
				}

				tp = nextPulse();

				if (tp == null)
				{
					pulse = -1;
					break;
				}
				pulse = tp.getWidth();

			}
		}
	}

}
