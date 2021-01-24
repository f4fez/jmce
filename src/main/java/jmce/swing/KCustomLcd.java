package jmce.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.security.InvalidParameterException;

/**
 * Represent a custom LCD design. The component contain N segment identified by his number
 * and draw from an image. i.e A 7 segments LCD, is composed of 7 images indexed from 0 to 6.
 *
 */
public class KCustomLcd extends Component {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * List of segment image.
	 */
	private Image[] images;
	
	/**
	 * Segment on.
	 */
	private boolean[] activeSegments;
	
	/**
	 * Define the LCD background color.
	 */
	private Color backgroundColor;
	
	/**
	 * Default constructor.
	 * @param images List of image for segments.
	 * @param backgroundColor the backgroundColor to set
	 */
	public KCustomLcd(final Image[] images, final Color backgroundColor) {
		if (images == null || images.length == 0) {
			throw new InvalidParameterException("images parameter should include at least one image");
		}
		this.images = images;
		this.activeSegments = new boolean[images.length];
		this.setBackground(backgroundColor);
	}
	
	/**
	 * Constructor with default background color.
	 * @param images List of image for segments.
	 */
	public KCustomLcd(final Image[] images) {
		this(images, Color.DARK_GRAY);
	}
	
	@Override
	public void paint(Graphics g) {
		for (int i = 0; i < images.length; i++) {
			if (activeSegments[i]) {
				g.drawImage(images[i], 0, 0, null);
			}
		}
	}
	
	/**
	 * Set active (or not) a LCD segment.
	 * @param segment Segment number.
	 * @param active true to activate.
	 */
	public void setSegmentActive(final int segment, final boolean active) {
		activeSegments[segment] = active;
		repaint();
	}

	/**
	 * Get the LCD background color.
	 * @return the backgroundColor
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Define the LCD background color.
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		this.setBackground(backgroundColor);
		repaint();
	}

	/**
	 * Get the list of active segments.
	 * @return the activeSegments
	 */
	public boolean[] getActiveSegments() {
		return activeSegments;
	}
	
	/**
	 * Get the list of active segments.
	 * @return the activeSegments
	 */
	public int getActiveSegmentsAsInt() {
		int val = 0;
		for(int i = 0; i < images.length; i++) {
			if (this.activeSegments[i]) {
				val |= 1 << i;
			}
		}
		return val;
	}

	/**
	 * Set the list of active segments.
	 * @param activeSegments the activeSegments to set
	 */
	public void setActiveSegments(boolean[] activeSegments) {
		this.activeSegments = activeSegments;
		repaint();
	}
	
	/**
	 * Set the list of active segments.
	 * @param activeSegments the activeSegments to set
	 */
	public void setActiveSegments(final int activeSegments) {
		for(int i = 0; i < images.length; i++) {
			this.activeSegments[i] = ((1 << i) & activeSegments) == (1 << i);
		}
		repaint();
	}
	
}
