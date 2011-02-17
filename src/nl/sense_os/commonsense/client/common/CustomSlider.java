package nl.sense_os.commonsense.client.common;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Slider;

/**
 * This class lets you move the slider in bigger intervals,
 * without modifying the value given by the slider. 
 * 
 * @author fede
 *
 */
public class CustomSlider extends Slider {

	private int movementIncrement = 10;

	public CustomSlider(int movementIncrement) {
		super();
		this.movementIncrement = movementIncrement;
	}

	/**
	 * Modify the text displayed in the tool tip removing the movement increment.
	 */
	protected String onFormatValue(int value) {
		return Format.substitute(super.getMessage(), value / movementIncrement);
	}
}
