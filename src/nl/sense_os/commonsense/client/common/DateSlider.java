package nl.sense_os.commonsense.client.common;

import java.util.Date;

import com.extjs.gxt.ui.client.widget.Slider;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Slider to select a date. Value should represent the Unix time stamp (in seconds).
 * 
 * @author fede
 * 
 */
public class DateSlider extends Slider {

    public DateSlider() {
        super();
    }

    /**
     * Formats the value as a date.
     */
    @Override
    protected String onFormatValue(int value) {
        Date date = new Date(value * 1000l);
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
        return format.format(date);
    }
}
