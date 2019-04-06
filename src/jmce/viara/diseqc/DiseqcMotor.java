/**
   $Id: DiseqcMotor.java 692 2011-09-02 08:38:10Z mviara $

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
package jmce.viara.diseqc;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jmce.swing.KLed;
import jmce.util.Logger;
import jmce.sim.*;
import jmce.util.Timer;
import jmce.util.TimerListener;
import jmce.swing.Util;

import jmce.intel.mcs51.*;

class DiseqcLed extends JPanel
{

	private static final long serialVersionUID = 1L;
	private KLed led;
	private boolean mode = true;
	
	public DiseqcLed(String label,Color color)
	{
		this(label,color,6);
	}
	public DiseqcLed(String label,Color color,int size)
	{
		super(new GridBagLayout());

		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
		g.fill  = GridBagConstraints.BOTH;g.insets = new Insets(2,2,2,2);
		g.anchor = GridBagConstraints.CENTER;

		led = new KLed(color);
		//JFactory.setBox(this);
		Util.setTitle(this,label,color);
		//add(new JLabel(label));
		//g.gridx++;
		add(led,g);
		set(false);
	}

	public DiseqcLed(String label)
	{
		this(label,Color.red);
	}

	public void set(boolean mode)
	{
		led.setLed(mode);
	}

	public void set()
	{
		set(true);
	}

	public void reset()
	{
		set(false);
	}

	public boolean get()
	{
		return mode;
	}

	public void on()
	{
		set(false);
	}

	public void off()
	{
		set(true);
	}

}

class JFactory
{
	public static void setBox(JComponent j)
	{
		j.setBorder(BorderFactory.createEtchedBorder());

	}


	static void setDimensionToMax(JComponent ... components)
	{
		Dimension max = new Dimension(0,0);
		
		for (JComponent c : components)
		{
			Dimension d = c.getPreferredSize();
			if (d.width > max.width)
				max.width = d.width;
			if (d.height > max.height)
				max.height = d.height;
		}

		for (JComponent c : components)
		{
			c.setPreferredSize(max);
		}

	}
	

}

/**
 * Diseqc Motor.
 * <p>
 * This class rappresent the GUI for the Diseqc motor.
 *
 * @author Mario Viara
 * @version 1.00
 */
public class DiseqcMotor extends AbstractPeripheral implements
   jmce.sim.SwingHardware,MemoryWriteListener,MCS51Constants
{
	private static Logger log = Logger.getLogger(DiseqcMotor.class);
	private int position = 0;
	private JPanel p = null;
	JSlider slider;
	DiseqcLed ledWest = new DiseqcLed("West",Color.blue);
	DiseqcLed ledEst = new DiseqcLed("Est",Color.red);
	DiseqcLed ledStatus = new DiseqcLed("Status",Color.green);
	DiseqcLed ledPower = new DiseqcLed("Power",Color.orange);
	JToggleButton bEst = new JToggleButton("GO East");
	JToggleButton bWest = new JToggleButton("GO West");

	// Motor startup time in ms
	static final int MOTOR_STARTUP_TIME = 200;

	// Motor pulse time
	static final int MOTOR_PULSE_TIME = 20;

	// Min/max motor Position
	static final int MOTOR_PULSE_MAX = 1000;

	// Previus tatus of motor
	boolean motor;

	// Current direction
	boolean direction;

	Timer timerPowerOn = null;
	Timer timerPowerOff = null;
	Timer timerPulse = null;

	public DiseqcMotor()
	{
		super("Motor");
		java.util.Hashtable<Integer,JLabel> d = new java.util.Hashtable<Integer,JLabel>();
		for (int i = 1 ; i < 4 ; i++)
		{
			d.put(-i*30,new JLabel((i*30)+"W"));
			d.put(i*30,new JLabel((i*30)+"E"));
		}
		d.put(0,new JLabel("000"));

		JFactory.setDimensionToMax(ledEst,ledWest,ledPower,ledStatus);
		slider = new JSlider(-90,90,0);
		slider.setMajorTickSpacing(30);
		slider.setMinorTickSpacing(10);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setEnabled(false);
		slider.setLabelTable(d);
	}


	public void reset() throws SIMException
	{
		super.reset();
		motor = false;
	}

	public void registerCPU(CPU cpu) throws SIMException
	{
		super.registerCPU(cpu);
		cpu.addIOWriteListener(P0,this);
		cpu.addIOWriteListener(P1,this);

		timerPowerOff = new Timer((int)(MOTOR_STARTUP_TIME*cpu.getCycleMillis()),false,new TimerListener()
		{
			public void timerExpired()
			{
				MotorOff();
			}

		});

		timerPowerOn = new Timer((int)(MOTOR_STARTUP_TIME*cpu.getCycleMillis()),false,new TimerListener()
		{
			public void timerExpired()
			{
				MotorOn();
			}

		});

		timerPulse = new Timer((int)(MOTOR_PULSE_TIME*cpu.getCycleMillis()),true,new TimerListener()
		{
			public void timerExpired()
			{
				MotorPulse();
			}

		});

	}

	private void setP1(int v)
	{
		ledEst.set((v & 0x80) != 0);
		ledWest.set((v & 0x40) != 0);

		//log.fine("P1="+Hex.formatByte(v));
	}

	private void setP0(int v)
	{
		//log.fine("P0="+Hex.formatByte(v));
		// Motor power active low ! 
		boolean newMotor = (v & (1 << 3)) == 0;

		if (newMotor && !motor)
		{
			cpu.addTimerCycle(timerPowerOn);
		}
		else if (!newMotor && motor)
		{
			cpu.addTimerCycle(timerPowerOff);
		}

		direction = (v & (1 << 4)) != 0;

		motor = newMotor;
		ledStatus.set((v & 4) == 0);
		ledPower.set((v & 0x08) == 0);
	}

	public void MotorOff()
	{
		log.fine("Motor OFF");
		timerPulse.cancel();
	}

	public void MotorOn()
	{
		log.fine("Motor ON");
		cpu.addTimerCycle(timerPulse);
	}

	private void MotorPulse()
	{
		try
		{
			if (direction)
			{
				if (position  < MOTOR_PULSE_MAX )
				{
					position ++;
					updatePosition();
					((MCS51)cpu).cplBit(P1 |  4);
				}
			}
			else
			{
				if (position  > -MOTOR_PULSE_MAX)
				{
					position--;
					updatePosition();
					((MCS51)cpu).cplBit(P1 |  4);
				}
			}
		}
		catch (Exception e)
		{
			log.info(e);
		}
	}

	public void writeMemory(Memory m,int a,int v,int oldValue) throws SIMException
	{
		if (a == P0)
			setP0(v);
		else if (a == P1)
			setP1(v);
	}

	void updatePosition()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int value  = 90 * (position ) / DiseqcMotor.MOTOR_PULSE_MAX;
				slider.setValue(-value);
			}

		});

	}

	public void setPosition(int position)
	{
		this.position = position;
		updatePosition();
	}

	public JComponent getComponent()
	{
		if (p == null)
		{
			p =  new JPanel(new GridBagLayout());
			GridBagConstraints g = new GridBagConstraints();
			g.gridx = 0;g.gridy = 0;g.gridwidth = 1;g.gridheight = 1;
			g.fill  = GridBagConstraints.BOTH;g.insets = new Insets(2,2,2,2);
			g.anchor = GridBagConstraints.CENTER;
			g.weightx = 1;

			
			g.gridwidth = 4;
			p.add(slider,g);
			
			g.gridy++;g.gridwidth = 1;g.gridx = 0;
			p.add(ledWest,g);g.gridx++;
			p.add(ledEst,g);g.gridx++;
			p.add(ledPower,g);g.gridx++;
			p.add(ledStatus,g);g.gridx++;
			
			g.gridx = 0;g.gridy++;g.gridwidth=2;
			p.add(bWest,g);
			g.gridx += 2;
			p.add(bEst,g);
			
			JTextField diseqc = new JTextField(20);
			Util.setTitle(diseqc,"Diseqc Command");
			
			g.gridy++;g.gridx=0;
			g.gridx = 0;g.gridwidth = 4;
			//p.add(diseqc,g);

			bEst.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent av)
				{
					try
					{
						if (bEst.isSelected())
							((MCS51)cpu).sfrReset(P0,1);
						else
							((MCS51)cpu).sfrSet(P0,1);
					}
					catch (Exception discard)
					{
					};
				}
			});

			bWest.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent av)
				{
					try
					{
						if (bWest.isSelected())
							((MCS51)cpu).sfrReset(P0,2);
						else
							((MCS51)cpu).sfrSet(P0,2);
					}
					catch (Exception discard)
					{
					};
				}
			});

		}

		return p;
	}

}


