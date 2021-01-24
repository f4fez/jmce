package jmce.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmce.sim.AbstractLockRangePll;

public class JPll extends JPanel {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private AbstractLockRangePll pll;

	private JTextField frequTextField;

	private DecimalFormat freqFormat;

	private JLabel lockLedLabel;

	private JLabel freqLabel;

	private KLed lockLed;

	public JPll(final AbstractLockRangePll pll) {
		this.pll = pll;
		setLayout(new GridLayout(2, 0));
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setGroupingSeparator(' ');
		freqFormat = new DecimalFormat("###,###,###", otherSymbols);
		frequTextField = new JTextField();
		frequTextField.setEditable(false);
		freqLabel = new JLabel("Frequency (Hz)");
		lockLedLabel = new JLabel("Locked");
		lockLed = new KLed(Color.GREEN);
		this.add(freqLabel);
		this.add(lockLedLabel);
		this.add(frequTextField);
		this.add(lockLed);
		pll.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				update();
			}
		});
		update();
	}

	private void update() {
		String frequ = freqFormat.format(pll.getOutputFrequency());
		frequTextField.setText(frequ);
		lockLed.setLed(pll.isLocked());
	}
}
