/**
   $Id: Spectrum.java 625 2011-06-07 06:52:42Z mviara $

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

import jmce.sim.*;

/**
 * Spectrum base class.
 *
 * @author Mario Viara
 * @version 1.01
 */
public class Spectrum extends jmce.zilog.z80.Z80 implements SpectrumConstants,ResetListener
{
	private int ula;
	
	public Spectrum()
	{
		setClock(3500000);
		setRealTime(true);
		addLoader(new ZXSnapshot());
		addResetListener(this);

	}

	protected void initPeripherals() throws SIMException
	{
		Screen scr;
		Speaker spk;
		Tape tape;
		
		if (getHardware(Timer.class) == null)
			addHardware(new Timer());
		
		scr = (Screen)getHardware(Screen.class);
		if (scr == null)
		{
			scr = new Screen(2);
			addHardware(scr);
		}

		spk = (Speaker)getHardware(Speaker.class);
		if (spk == null)
		{
			spk = new Speaker();
			addHardware(spk);
		}

		spk.setScreen(scr);


		tape = (Tape)getHardware(Tape.class);
		
		if (tape == null)
		{
			tape = new Tape();
			addHardware(tape);
		}
		
		super.initPeripherals();


	}

	/**
	 * Return the state of the tape in.
	 *
	 * @since 1.02
	 */
	public final boolean getTapeIn()
	{
		return (ula & ULA_IN) != 0;
	}

	/**
	 * Set the state of the tape input.
	 *
	 * @since 1.02
	 */
	public final void setTapeIn(boolean mode)
	{
		if (mode)
			ula |= ULA_IN;
		else
			ula &= ~ULA_IN;
	}


	/**
	 * Return the value of the current ula
	 */
	public final int getUla()
	{
		return ula;
	}
	
	public void reset(CPU cpu) throws SIMException
	{
		ula = 0;
	}

}

