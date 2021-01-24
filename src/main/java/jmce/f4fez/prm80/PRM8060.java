package jmce.f4fez.prm80;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmce.f4fez.Nj8822;
import jmce.intel.mcs51.Serial;
import jmce.mos._74HC4094;
import jmce.philips.P80c552;
import jmce.sim.AbstractLockRangePll;
import jmce.sim.Hardware;
import jmce.sim.Loadable;
import jmce.sim.Memory;
import jmce.sim.MemoryWriteListener;
import jmce.sim.SIMException;
import jmce.sim.SwingHardware;
import jmce.sim.memory.MemoryBit;
import jmce.sim.terminal.Terminal;
import jmce.swing.JPll;
import jmce.swing.KLed;

/**
 * Virtual machine base over Intel 8052.
 * 
 */
public class PRM8060 extends P80c552 implements SwingHardware, ButtonListener {

	public final static String PRM8060_EPROM_FILE = "hex/8060_144_V4.hex";

	private UI8060 display;

	private Nj8822 pll;

	private PRM8060I2cBus i2cbus;

	private PRM8060Memory.DirectBusRegister dbr1;

	private PRM8060Memory.DirectBusRegister dbr2;

	private _74HC4094 registerLow;

	private _74HC4094 registerHigh;

	public PRM8060() {
		setName("PRM8060");
		setClock(12000000);
		setRealTime(true);
	}

	private JPanel p;

	private JPanel micPanel;

	private TitledBorder micBorder;

	private JCheckBox pttCB;

	private JCheckBox craddleCB;

	private JPanel rxPanel;

	private TitledBorder rxBorder;

	private JLabel rxRssiLabel;

	private JSpinner rxRssiSpinner;

	private JLabel rxVolumeLabel;

	private JTextField rxVolumeTF;

	private JLabel rxMuteLabel;

	private KLed rxMuteCB;

	private JLabel rxSpMuteLabel;

	private KLed rxSpMuteCB;

	private JPanel txPanel;

	private TitledBorder txBorder;

	private JLabel txTXLabel;

	private KLed txTX;

	private JLabel txPAOnLabel;

	private KLed txPAOn;

	private JLabel txQROLabel;

	private KLed txQRO;

	public Component getComponent() {
		if (p == null) {
			SwingHardware s;
			p = new JPanel(new GridBagLayout());
			GridBagConstraints g = new GridBagConstraints();

			// PRM Front
			g.gridx = 0;
			g.gridy = 0;
			g.gridwidth = 5;
			g.gridheight = 1;
			g.fill = GridBagConstraints.BOTH;
			g.insets = new Insets(2, 2, 2, 2);
			g.anchor = GridBagConstraints.CENTER;
			p.add(display.getComponent(), g);

			// Microphone panel
			g.gridx = 0;
			g.gridy = 1;
			g.gridwidth = 1;
			g.gridheight = 1;
			p.add(createMicPanel(), g);

			// RX panel
			g.gridx = 1;
			g.gridy = 1;
			g.gridwidth = 1;
			g.gridheight = 1;
			p.add(createtRxPanel(), g);

			// Pll interface
			g.gridx = 2;
			g.gridy = 1;
			g.gridwidth = 1;
			g.gridheight = 1;
			JPll pllUi = new JPll((AbstractLockRangePll) getHardware(Nj8822.class));

			TitledBorder pllBorder = BorderFactory.createTitledBorder("PLL");
			pllUi.setBorder(pllBorder);
			p.add(pllUi, g);

			// TX panel
			g.gridx = 3;
			g.gridy = 1;
			g.gridwidth = 1;
			g.gridheight = 1;
			p.add(createtTxPanel(), g);

			// Search for swing component on serial
			s = (SwingHardware) getHardwareTree(Serial.class, Terminal.class, SwingHardware.class);
			g.gridx = 0;
			g.gridy = 2;
			g.gridwidth = 5;
			g.gridheight = 1;
			p.add(s.getComponent(), g);
		}

		return p;
	}

	private JComponent createMicPanel() {
		micPanel = new JPanel();
		pttCB = new JCheckBox("PTT");
		pttCB.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				MemoryBit pttIn = getSfrBitOpenCollector(P80c552.P4, 0);
				try {
					pttIn.set(!pttCB.isSelected());
				} catch (SIMException e1) {
					e1.printStackTrace();
				}
			}
		});
		micPanel.add(pttCB);
		craddleCB = new JCheckBox("Craddle");
		micPanel.add(craddleCB);
		micBorder = BorderFactory.createTitledBorder("Mic");
		micPanel.setBorder(micBorder);
		return micPanel;
	}

	private JComponent createtTxPanel() {
		txPanel = new JPanel();
		txPanel.setLayout(new GridLayout(2, 0));
		txBorder = BorderFactory.createTitledBorder("TX");
		txPanel.setBorder(rxBorder);

		txTXLabel = new JLabel("TX");
		txPAOnLabel = new JLabel("PA");
		txQROLabel = new JLabel("QRO");
		txPanel.add(txTXLabel);
		txPanel.add(txPAOnLabel);
		txPanel.add(txQROLabel);

		txTX = new KLed(Color.RED);
		txPAOn = new KLed(Color.RED);
		txQRO = new KLed(Color.GREEN);
		txPanel.add(txTX);
		txPanel.add(txPAOn);
		txPanel.add(txQRO);
		return txPanel;
	}

	private JComponent createtRxPanel() {
		rxPanel = new JPanel();
		rxPanel.setLayout(new GridLayout(2, 0));
		rxRssiLabel = new JLabel("RSSI");
		rxMuteLabel = new JLabel("RX Mute");
		rxVolumeLabel = new JLabel("Volume");
		rxSpMuteLabel = new JLabel("SP mute");
		rxPanel.add(rxRssiLabel);
		rxPanel.add(rxMuteLabel);
		rxPanel.add(rxVolumeLabel);
		rxPanel.add(rxSpMuteLabel);

		rxRssiSpinner = new JSpinner(new SpinnerNumberModel(40, 0, 255, 1));
		rxPanel.add(rxRssiSpinner);
		rxMuteCB = new KLed(Color.RED);
		rxPanel.add(rxMuteCB);
		rxVolumeTF = new JTextField();
		rxPanel.add(rxVolumeTF);
		rxSpMuteCB = new KLed(Color.RED);
		rxPanel.add(rxSpMuteCB);

		rxBorder = BorderFactory.createTitledBorder("RX");
		rxPanel.setBorder(rxBorder);
		return rxPanel;
	}

	protected void initPeripherals() throws SIMException {

		// Add serial port with Swing terminal
		if (getHardware(Serial.class) == null) {
			Serial s = new Serial();
			Terminal t = new jmce.sim.terminal.VT100();
			t.setNumRow(16);
			t.setNumCol(80);
			t.setFontSize(14);
			t.addHardware(new jmce.sim.terminal.SwingCRT());
			s.addHardware(t);
			s.setConnected(t);
			addHardware(s);
		}

		// Pll
		if (getHardware(Nj8822.class) == null) {
			pll = new Nj8822(10000000, 144000000, 170000000, 64);
			pll.setClock(getSfrBit(P80c552.P1, 0));
			pll.setData(getSfrBit(P80c552.P1, 1));
			pll.setCe(getSfrBit(P80c552.P1, 5));
			pll.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					MemoryBit inPllLock = getSfrBitOpenCollector(P80c552.P5, 2);
					try {
						inPllLock.set(!pll.isLocked());
					} catch (SIMException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			addHardware(pll);
		}

		// Front panel
		if (getHardware(getFrontUIClass()) == null) {
			display = getFrontUI();
			display.addButtonListener(this);
			addHardware(display);
		}

		// I2C
		if (getHardware(PRM8060I2cBus.class) == null) {
			i2cbus = new PRM8060I2cBus();
			addHardware(i2cbus);
		}

		// Load the software
		if (getHardware(Loadable.class) == null) {
			addHardware(new Loadable(getEpromFilePath(), 0));
		}

		// Shift registers
		registerLow = new _74HC4094(0xff);
		registerHigh = new _74HC4094(0xff);
		registerLow.addCascade(registerHigh);
		registerLow.setData(getSfrBit(P80c552.P1, 1));
		registerLow.setCp(getSfrBit(P80c552.P1, 0));
		registerLow.setOe(getSfrBit(P80c552.P3, 4));
		registerLow.setStr(getSfrBit(P80c552.P3, 5));
		registerHigh.setOe(getSfrBit(P80c552.P3, 4));
		registerHigh.setStr(getSfrBit(P80c552.P3, 5));
		registerLow.addMemoryWriteListener(new MemoryWriteListener() {
			
			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				lowRegisterUpdated(oldValue, value);
			}
		});
		registerHigh.addMemoryWriteListener(new MemoryWriteListener() {
			
			@Override
			public void writeMemory(Memory memory, int address, int value, int oldValue) throws SIMException {
				highRegisterUpdated(oldValue, value);
			}
		});

		MemoryBit pttIn = getSfrBitOpenCollector(P80c552.P4, 0);
		pttIn.set(true); // RX

		super.initPeripherals();

	}
	
	private void lowRegisterUpdated(final int oldValue, final int value) {
		System.out.println("Low :  "+Integer.toHexString(oldValue)+" => "+Integer.toHexString(value));
	}
	
	private void highRegisterUpdated(final int oldValue, final int value) {
		System.out.println("High : "+Integer.toHexString(oldValue)+" => "+Integer.toHexString(value));
	}

	protected String getEpromFilePath() {
		return PRM8060_EPROM_FILE;
	}

	protected UI8060 getFrontUI() throws SIMException {
		return new UI8060();
	}

	protected Class<UI8060> getFrontUIClass() {
		return UI8060.class;
	}

	/**
	 * initMemories(); initPeripherals(); initRegisters(); initOpcodes(); initOpcodeDecoder(); initNames();
	 */
	public void init(Hardware parent) throws SIMException {
		Memory xdata = getMemoryForName("XDATA");

		if (xdata == null) {
			PRM8060Memory mem = new PRM8060Memory();
			dbr1 = mem.addDirectBusRegister(0x0d000, 0x0fff, 0xff, true); // Bit 1 & 2
			dbr2 = mem.addDirectBusRegister(0x0e000, 0x0fff, 0xff, true); // Bit 1 & 2

			xdata = (Memory) addHardware(mem);
		}

		super.init(parent);
	}

	public String toString() {
		return getName();
	}

	@Override
	public void action(int button, boolean value) {
		try {
			if (value) {
				switch (button) {
					case 1:
						dbr1.pressedButton(0x02);
						break;
					case 2:
						dbr1.pressedButton(0x04);
						break;
					case 3:
						dbr2.pressedButton(0x02);
						break;
					case 4:
						dbr2.pressedButton(0x04);
						break;
				}
			} else {
				switch (button) {
					case 1:
						dbr1.releasedButton(0x02);
						break;
					case 2:
						dbr1.releasedButton(0x04);
						break;
					case 3:
						dbr2.releasedButton(0x02);
						break;
					case 4:
						dbr2.releasedButton(0x04);
						break;
				}
			}
		} catch (SIMException e) {
			e.printStackTrace();
		}
	}
}
