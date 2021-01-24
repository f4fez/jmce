/**
   $Id: Display.java 601 2011-05-25 08:24:49Z mviara $

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
package jmce.f4fez.prm80;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jmce.philips.P80c552;
import jmce.philips.PCF2100C;
import jmce.sim.AbstractPeripheral;
import jmce.sim.CPU;
import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.sim.SwingHardware;
import jmce.swing.KCustomLcd;

/**
 * Display 4x20 connected to the V8052.
 * <p>
 * <ul>
 * <li>P0..7 8 bit data Display</li>
 * <li>P1.0 LCD RW (1 Read, 0 Write)</li>
 * <li>P1.1 LCD RS (0 Cmd, 1 Data)</li>
 * <li>P1.2 LCD EN (1 Enable display)</li>
 * </ul>
 * <p>
 * 
 * @author Mario Viara
 * @version 1.00
 * 
 * @since 1.02
 * 
 */
public class UI8060 extends AbstractPeripheral implements SwingHardware {

	static public final int LCD_RW = 0x01;

	static public final int LCD_RS = 0x02;

	static public final int LCD_EN = 0x04;

	private JPanel p = null;

	private KCustomLcd lcd;

	private JButton leftTopButton;

	private JButton leftCenterButton;

	private JButton leftBottomButton;

	private JButton rightTopButton;

	private JLabel volumeLabel;

	private JPanel volumePanel;

	private JSpinner volumeSpinner;

	private JCheckBox powerCB;

	private PCF2100C pcf2100;

	private final static int SEGMENT_NUMBER = 20;

	private ArrayList<ButtonListener> buttonListeners;

	public UI8060() throws SIMException {
		BufferedImage[] imgs = new BufferedImage[SEGMENT_NUMBER];
		try {
			for (int i = 0; i < SEGMENT_NUMBER; i++) {
				imgs[i] = ImageIO.read(this.getClass().getClassLoader().getResource("jmce/f4fez/prm80/images/" + i + ".png"));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		buttonListeners = new ArrayList<ButtonListener>();

		setName("Display ");

		pcf2100 = new PCF2100C();

		pcf2100.addRegisterWriteListener(new MemoryWriteListener() {

			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				if ((address & 3) == 0) {
					lcd.setActiveSegments((lcd.getActiveSegmentsAsInt() & 0xffffff00) | value);
				} else if ((address & 3) == 1) {
					lcd.setActiveSegments((lcd.getActiveSegmentsAsInt() & 0xffff00ff) | (value << 8));
				} else if ((address & 3) == 2) {
					lcd.setActiveSegments((lcd.getActiveSegmentsAsInt() & 0xff00ffff) | (value << 16));
				} else if ((address & 3) == 3) {
					lcd.setActiveSegments((lcd.getActiveSegmentsAsInt() & 0x00ffffff) | (value << 24));
				}

			}
		});
		p = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();

		g.gridx = 2;
		g.gridy = 0;
		g.gridwidth = 1;
		g.gridheight = 3;
		g.anchor = GridBagConstraints.CENTER;
		g.fill = GridBagConstraints.NONE;
		g.insets = new Insets(2, 2, 2, 2);
		lcd = new KCustomLcd(imgs);
		lcd.setPreferredSize(new Dimension(256, 256));
		p.add(lcd, g);

		g.gridx = 1;
		g.gridy = 0;
		g.gridwidth = 1;
		g.gridheight = 1;
		g.fill = GridBagConstraints.BOTH;
		g.weighty = 1.0;
		leftTopButton = new JButton();
		leftTopButton.setPreferredSize(new Dimension(150, 20));
		p.add(leftTopButton, g);
		leftTopButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonChanged(1, false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				buttonChanged(2, true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});


		g.gridx = 1;
		g.gridy = 1;
		g.gridwidth = 1;
		g.gridheight = 1;
		leftCenterButton = new JButton();
		leftCenterButton.setPreferredSize(new Dimension(150, 20));
		p.add(leftCenterButton, g);
		leftCenterButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonChanged(2, false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				buttonChanged(2, true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		g.gridx = 1;
		g.gridy = 2;
		g.gridwidth = 1;
		g.gridheight = 1;
		leftBottomButton = new JButton();
		leftBottomButton.setPreferredSize(new Dimension(150, 20));
		p.add(leftBottomButton, g);
		leftBottomButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonChanged(3, false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				buttonChanged(3, true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		g.gridx = 3;
		g.gridy = 0;
		g.gridwidth = 1;
		g.gridheight = 1;
		rightTopButton = new JButton();
		rightTopButton.setPreferredSize(new Dimension(150, 20));
		p.add(rightTopButton, g);
		rightTopButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonChanged(4, false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				buttonChanged(4, true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		// Volume button
		volumePanel = new JPanel();
		volumePanel.setLayout(new BoxLayout(volumePanel, BoxLayout.PAGE_AXIS));
		volumePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		volumeLabel = new JLabel("Volume");
		volumePanel.add(volumeLabel);
		volumeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
		volumeSpinner.setValue(3);
		volumeSpinner.setMaximumSize(new Dimension(100, 25));
		volumePanel.add(volumeSpinner);
		powerCB = new JCheckBox("Power");
		powerCB.setSelected(true);
		volumePanel.add(powerCB);

		g.gridx = 0;
		g.gridy = 0;
		g.gridwidth = 1;
		g.gridheight = 3;
		p.add(volumePanel, g);

	}

	public void registerCPU(CPU cpu) throws SIMException {
		super.registerCPU(cpu);

		P80c552 p80c552 = (P80c552) cpu;
		pcf2100.setClb(p80c552.getSfrBit(P80c552.P1, 0));
		pcf2100.setData(p80c552.getSfrBit(P80c552.P1, 1));
		pcf2100.setDlen(p80c552.getSfrBit(P80c552.P4, 2));
	}

	public java.awt.Component getComponent() {
		return p;
	}

	private void buttonChanged(final int number, boolean value) {
		for (ButtonListener bl : buttonListeners) {
			bl.action(number, value);
		}
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addButtonListener(ButtonListener e) {
		return buttonListeners.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean removeButtonListener(Object o) {
		return buttonListeners.remove(o);
	}

}
