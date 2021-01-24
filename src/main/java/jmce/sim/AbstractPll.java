package jmce.sim;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class AbstractPll extends AbstractHardware {

	private int clockRef;

	private int loopDivider = 1;

	private int refDivider = 1;

	private ArrayList<ChangeListener> changesListeners = new ArrayList<ChangeListener>();

	public AbstractPll(final int clockRef) {
		this.clockRef = clockRef;
	}

	public int getOutputFrequency() {
		return clockRef / refDivider * loopDivider;
	}

	/**
	 * @return the clockRef
	 */
	public int getClockRef() {
		return clockRef;
	}

	/**
	 * @param clockRef the clockRef to set
	 */
	public void setClockRef(int clockRef) {
		this.clockRef = clockRef;
		callChangeListeners();
	}

	/**
	 * @return the loopDivider
	 */
	public int getLoopDivider() {
		return loopDivider;
	}

	/**
	 * @param loopDivider the loopDivider to set
	 */
	public void setLoopDivider(int loopDivider) {
		if (loopDivider > 0) {
			this.loopDivider = loopDivider;
		}
		callChangeListeners();
	}

	/**
	 * @return the refDivider
	 */
	public int getRefDivider() {
		return refDivider;
	}

	/**
	 * @param refDivider the refDivider to set
	 */
	public void setRefDivider(int refDivider) {
		this.refDivider = refDivider;
		callChangeListeners();
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addChangeListener(ChangeListener e) {
		return changesListeners.add(e);
	}

	public void setDividers(final int refDivider, final int loopDivider) {
		if (loopDivider > 0) {
			this.loopDivider = loopDivider;
		}
		if (refDivider > 0) {
			this.refDivider = refDivider;
		}
		callChangeListeners();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean removeChangeListener(ChangeListener o) {
		return changesListeners.remove(o);
	}

	private void callChangeListeners() {
		for (ChangeListener l : changesListeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}
}
