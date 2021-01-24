package jmce.sim;

public abstract class AbstractLockRangePll extends AbstractPll {

	private final int lockStart;

	private final int lockEnd;

	private boolean lockError;

	public AbstractLockRangePll(final int clockRef, final int lockStart, final int lockEnd) {
		super(clockRef);
		this.lockStart = lockStart;
		this.lockEnd = lockEnd;
	}

	public boolean isLocked() {
		return lockError || (lockStart <= getOutputFrequency() && getOutputFrequency() <= lockEnd);
	}

	/**
	 * @return the lockError
	 */
	public boolean isLockError() {
		return lockError;
	}

	/**
	 * @param lockError the lockError to set
	 */
	public void setLockError(boolean lockError) {
		this.lockError = lockError;
	}

}
