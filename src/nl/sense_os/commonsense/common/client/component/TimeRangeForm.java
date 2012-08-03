package nl.sense_os.commonsense.common.client.component;

import java.util.Date;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Form to indicate a time range.
 */
public class TimeRangeForm extends FormPanel {

    private static final Logger LOG = Logger.getLogger(TimeRangeForm.class.getName());
    private RadioGroup timeRangeField;
    private Radio hourRadio;
    private Radio dayRadio;
    private Radio weekRadio;
    private Radio monthRadio;
    private Radio otherTimeRadio;
    private DateField startDateField;
    private TimeField startTimeField;
    private DateField endDateField;
    private TimeField endTimeField;
    private CheckBox subsampleField;
    private LabelField mainLabel;

    public TimeRangeForm() {
        super();

        setHeaderVisible(false);
        setBodyBorder(false);
        setLabelAlign(LabelAlign.TOP);
        setFieldWidth(350);

        initTimeRangeFields();
    }

    /**
     * @return The end time in millisecs since 1/1/1970 UTC, or -1 if no end time is selected.
     */
    public long getEndTime() {

        long endTime = -1;

        // see which radio was selected
        Radio selected = timeRangeField.getValue();
        if (hourRadio.equals(selected)) {
            endTime = -1;

        } else if (dayRadio.equals(selected)) {
            endTime = -1;

        } else if (weekRadio.equals(selected)) {
            endTime = -1;

        } else if (monthRadio.equals(selected)) {
            endTime = -1;

        } else if (otherTimeRadio.equals(selected)) {

            DateWrapper endWrapper = new DateWrapper(endDateField.getValue());
            endWrapper = endWrapper.resetTime();
            endWrapper = endWrapper.addHours(endTimeField.getValue().getHour() - 12);
            endWrapper = endWrapper.addMinutes(endTimeField.getValue().getMinutes());
            endTime = endWrapper.getTime();

        } else {
            LOG.warning("Unexpected radio button selected: " + selected);
        }

        DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);
        if (endTime != -1) {
            LOG.fine("End:   " + dtf.format(new Date(endTime)));
        } else {
            LOG.fine("No end time");
        }

        return endTime;
    }

    /**
     * @return The start time in millisecs since 1/1/1970 UTC, or -1 if no start time is selected.
     */
    public long getStartTime() {

        long startTime = -1;

        // constants
        final long hour = 1000 * 60 * 60;
        final long day = 24 * hour;
        final long week = 7 * day;

        // see which radio was selected
        Radio selected = timeRangeField.getValue();
        if (hourRadio.equals(selected)) {
            startTime = System.currentTimeMillis() - hour;

        } else if (dayRadio.equals(selected)) {
            startTime = System.currentTimeMillis() - day;

        } else if (weekRadio.equals(selected)) {
            startTime = System.currentTimeMillis() - week;

        } else if (monthRadio.equals(selected)) {
            startTime = System.currentTimeMillis() - Math.round(29.53 * day);

        } else if (otherTimeRadio.equals(selected)) {
            DateWrapper startWrapper = new DateWrapper(startDateField.getValue());
            startWrapper = startWrapper.resetTime();
            startWrapper = startWrapper.addHours(startTimeField.getValue().getHour() - 12);
            startWrapper = startWrapper.addMinutes(startTimeField.getValue().getMinutes());
            startTime = startWrapper.getTime();

        } else {
            LOG.warning("Unexpected radio button selected: " + selected);
        }

        DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);
        LOG.fine("Start: " + dtf.format(new Date(startTime)));

        return startTime;
    }

    /**
     * @return true if the user requested subsampling. Default value is true.
     */
    public boolean getSubsample() {
        return subsampleField.getValue();
    }

    private void initTimeRangeFields() {

        mainLabel = new LabelField("Select the time range");
        mainLabel.setHideLabel(true);

        hourRadio = new Radio();
        hourRadio.setBoxLabel("Last hour");
        hourRadio.setHideLabel(true);

        dayRadio = new Radio();
        dayRadio.setBoxLabel("Last day");
        dayRadio.setValue(true);
        dayRadio.setHideLabel(true);

        weekRadio = new Radio();
        weekRadio.setBoxLabel("Last week");
        weekRadio.setHideLabel(true);

        monthRadio = new Radio();
        monthRadio.setBoxLabel("Last month");
        monthRadio.setHideLabel(true);

        otherTimeRadio = new Radio();
        otherTimeRadio.setBoxLabel("Other:");
        otherTimeRadio.setHideLabel(true);

        timeRangeField = new RadioGroup();
        timeRangeField.add(hourRadio);
        timeRangeField.add(dayRadio);
        timeRangeField.add(weekRadio);
        timeRangeField.add(monthRadio);
        timeRangeField.add(otherTimeRadio);
        timeRangeField.setOriginalValue(dayRadio);
        timeRangeField.setSelectionRequired(true);

        // advanced date chooser
        final FieldSet advancedRangeSet = new FieldSet();
        advancedRangeSet.setLayout(new FormLayout(LabelAlign.TOP));
        advancedRangeSet.setEnabled(false);

        Date start = new Date(System.currentTimeMillis() + 1000 * 60 * (15 - 60 * 24));

        startDateField = new DateField();
        startDateField.setFieldLabel("Start date");
        startDateField.setValue(start);

        startTimeField = new TimeField();
        startTimeField.setFieldLabel("Start time");
        startTimeField.setValue(startTimeField.findModel(start));
        startTimeField.setTriggerAction(TriggerAction.ALL);

        Date end = new Date(System.currentTimeMillis() + 1000 * 60 * 15);

        endDateField = new DateField();
        endDateField.setFieldLabel("End date");
        endDateField.setValue(end);

        endTimeField = new TimeField();
        endTimeField.setFieldLabel("End time");
        endTimeField.setValue(endTimeField.findModel(end));
        endTimeField.setTriggerAction(TriggerAction.ALL);

        // start date and time layout
        LayoutContainer startField = new LayoutContainer(new ColumnLayout());
        LayoutContainer startDateWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        startDateWrapper.add(startDateField, new FormData("-10"));
        startField.add(startDateWrapper, new ColumnData(.5));
        LayoutContainer startTimeWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        startTimeWrapper.add(startTimeField, new FormData("-10"));
        startField.add(startTimeWrapper, new ColumnData(.5));

        // end date and time layout
        LayoutContainer endField = new LayoutContainer(new ColumnLayout());
        LayoutContainer endDateWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        endDateWrapper.add(endDateField, new FormData("-10"));
        endField.add(endDateWrapper, new ColumnData(.5));
        LayoutContainer endTimeWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        endTimeWrapper.add(endTimeField, new FormData("-10"));
        endField.add(endTimeWrapper, new ColumnData(.5));

        // enable or disable specific date chooser
        timeRangeField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                boolean enable = otherTimeRadio.equals(timeRangeField.getValue());
                advancedRangeSet.setEnabled(enable);
                startDateField.setAllowBlank(!enable);
                startTimeField.setAllowBlank(!enable);
                endDateField.setAllowBlank(!enable);
                endTimeField.setAllowBlank(!enable);

                final long endTime = System.currentTimeMillis() + 1000 * 60 * 15;
                final long hour = 1000 * 60 * 60;
                final long day = 24 * hour;
                final long week = 7 * day;

                long startTime = endTime;
                Radio r = timeRangeField.getValue();
                if (hourRadio.equals(r)) {
                    startTime = System.currentTimeMillis() - hour;
                } else if (dayRadio.equals(r)) {
                    startTime = System.currentTimeMillis() - day;
                } else if (weekRadio.equals(r)) {
                    startTime = System.currentTimeMillis() - week;
                } else if (monthRadio.equals(r)) {
                    startTime = System.currentTimeMillis() - 4 * week;
                } else if (otherTimeRadio.equals(r)) {
                    return;
                } else {
                    LOG.warning("Unexpected radio button selected: " + r);
                }

                // update fields
                startDateField.setValue(new Date(startTime));
                startTimeField.setValue(startTimeField.findModel(new Date(startTime)));
                endDateField.setValue(new Date(endTime));
                endTimeField.setValue(endTimeField.findModel(new Date(endTime)));
            }
        });

        advancedRangeSet.add(startField, new FormData("-10"));
        advancedRangeSet.add(endField, new FormData("-10"));

        LayoutContainer left = new LayoutContainer(new FormLayout());
        left.setStyleAttribute("paddingRight", "10px");
        left.add(hourRadio, new FormData("-10"));
        left.add(otherTimeRadio, new FormData("-10"));

        LayoutContainer center1 = new LayoutContainer(new FormLayout());
        center1.setStyleAttribute("paddingRight", "10px");
        center1.add(dayRadio, new FormData("-10"));

        LayoutContainer center2 = new LayoutContainer(new FormLayout());
        center2.setStyleAttribute("paddingRight", "10px");
        center2.add(weekRadio, new FormData("-10"));

        LayoutContainer right = new LayoutContainer(new FormLayout());
        right.setStyleAttribute("paddingLeft", "10px");
        right.add(monthRadio, new FormData("-10"));

        LayoutContainer main = new LayoutContainer(new ColumnLayout());
        main.add(left, new ColumnData(.25));
        main.add(center1, new ColumnData(.25));
        main.add(center2, new ColumnData(.25));
        main.add(right, new ColumnData(.25));

        subsampleField = new CheckBox();
        subsampleField.setBoxLabel("Use subsampling (recommended)");
        subsampleField.setHideLabel(true);
        subsampleField.setValue(true);

        add(mainLabel, new FormData());
        add(main, new FormData());
        add(advancedRangeSet, new FormData());
        add(subsampleField, new FormData());
    }

    /**
     * @param label
     *            The label to display above the time range selection field.
     */
    public void setLabel(String label) {
        mainLabel.setText(label);
    }

    /**
     * Toggles enabled status of the subsample checkbox, unchecking the box when it is disabled.
     * 
     * @param enabled
     *            The desired status.
     */
    public void setSubsampleEnabled(boolean enabled) {
        subsampleField.setEnabled(enabled);
        if (!enabled) {
            subsampleField.setValue(false);
        }
    }
}
