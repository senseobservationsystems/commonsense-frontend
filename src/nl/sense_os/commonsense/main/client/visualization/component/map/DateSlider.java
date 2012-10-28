package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.Slider;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;

/**
 * Slider to select a date. Value should represent the Unix time stamp (in seconds).
 * 
 * @author fede
 * 
 */
public class DateSlider extends Slider {

	private static final DateTimeFormat FORMAT = DateTimeFormat
			.getFormat(PredefinedFormat.DATE_TIME_SHORT);

	/**
	 * Formats the value as a date.
	 */
	@Override
	protected String onFormatValue(int value) {
		return FORMAT.format(new Date(value * 1000l));
	}
}
