package nl.sense_os.commonsense.client.viz.choice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class VizTypeChooser extends View {

    private static final Logger LOG = Logger.getLogger(VizTypeChooser.class.getName());

    private Window window;
    private CardLayout layout;

    private List<SensorModel> sensors;
    private List<SensorModel> locationSensors;

    private AppEvent submitEvent;

    private Button buttonComplete;
    private Button buttonToTimeRange;
    private Button buttonToTypes;

    private FormPanel typeForm;
    private RadioGroup typesField;
    private Radio timeLineRadio;
    private Radio tableRadio;
    private Radio mapRadio;
    private Radio networkRadio;

    private FormPanel timeRangeForm;
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

    public VizTypeChooser(Controller c) {
        super(c);
    }

    private boolean checkForLocationSensors(List<SensorModel> list) {

        // create array to send as parameter in RPC
        locationSensors = new ArrayList<SensorModel>();
        for (SensorModel sensor : list) {

            String structure = sensor.<String> get("data_structure");

            if (null != structure && structure.contains("longitude")) {
                locationSensors.add(new SensorModel(sensor.getProperties()));

            } else {
                // do nothing
            }
        }

        // check whether there are any sensors at all
        if (locationSensors.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(VizEvents.ShowTypeChoice)) {
            List<SensorModel> sensors = event.<List<SensorModel>> getData();
            showWindow(sensors);

        } else if (type.equals(VizEvents.TypeChoiceCancelled)) {
            hideWindow();

        } else {
            LOG.fine("Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        window.hide();
        layout.setActiveItem(typeForm);
    }

    @Override
    protected void initialize() {
        super.initialize();

        submitEvent = new AppEvent(DataEvents.DataRequest);
        submitEvent.setData("showProgress", true);

        window = new CenteredWindow();
        window.setHeading("Visualization wizard");
        window.setMinWidth(425);
        window.setMinHeight(275);

        layout = new CardLayout();
        window.setLayout(layout);

        initTypePanel();
        initTimeRangePanel();

        layout.setActiveItem(window.getItem(0));
    }

    private void initTimeRangeButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (ce.getButton().equals(buttonComplete)) {
                    saveSelectedTimes();
                    submitForm();
                } else if (ce.getButton().equals(buttonToTypes)) {
                    layout.setActiveItem(typeForm);
                } else {
                    Dispatcher.forwardEvent(VizEvents.TypeChoiceCancelled);
                }
            }
        };

        buttonToTypes = new Button("Back", l);
        timeRangeForm.addButton(buttonToTypes);

        buttonComplete = new Button("Go!", l);
        timeRangeForm.addButton(buttonComplete);

        Button cancel = new Button("Cancel", l);
        timeRangeForm.addButton(cancel);

        FormButtonBinding binding = new FormButtonBinding(timeRangeForm);
        binding.addButton(buttonComplete);
    }

    private void initTimeRangeFields() {

        final FormData formData = new FormData("-10");

        LabelField mainLabel = new LabelField("Select the time range to visualize");
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

        // defaultRangeSet.add(timeRangeField, formData);

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
        startDateWrapper.add(startDateField, formData);
        startField.add(startDateWrapper, new ColumnData(.5));
        LayoutContainer startTimeWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        startTimeWrapper.add(startTimeField, formData);
        startField.add(startTimeWrapper, new ColumnData(.5));

        // end date and time layout
        LayoutContainer endField = new LayoutContainer(new ColumnLayout());
        LayoutContainer endDateWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        endDateWrapper.add(endDateField, formData);
        endField.add(endDateWrapper, new ColumnData(.5));
        LayoutContainer endTimeWrapper = new LayoutContainer(new FormLayout(LabelAlign.TOP));
        endTimeWrapper.add(endTimeField, formData);
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
                    startTime = endTime - hour;
                } else if (dayRadio.equals(r)) {
                    startTime = endTime - day;
                } else if (weekRadio.equals(r)) {
                    startTime = endTime - week;
                } else if (monthRadio.equals(r)) {
                    startTime = endTime - 4 * week;
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

        advancedRangeSet.add(startField, formData);
        advancedRangeSet.add(endField, formData);

        LayoutContainer left = new LayoutContainer(new FormLayout());
        left.setStyleAttribute("paddingRight", "10px");
        left.add(hourRadio, formData);
        left.add(otherTimeRadio, formData);

        LayoutContainer center1 = new LayoutContainer(new FormLayout());
        center1.setStyleAttribute("paddingRight", "10px");
        center1.add(dayRadio, formData);

        LayoutContainer center2 = new LayoutContainer(new FormLayout());
        center2.setStyleAttribute("paddingRight", "10px");
        center2.add(weekRadio, formData);

        LayoutContainer right = new LayoutContainer(new FormLayout());
        right.setStyleAttribute("paddingLeft", "10px");
        right.add(monthRadio, formData);

        LayoutContainer main = new LayoutContainer(new ColumnLayout());
        main.add(left, new ColumnData(.25));
        main.add(center1, new ColumnData(.25));
        main.add(center2, new ColumnData(.25));
        main.add(right, new ColumnData(.25));

        timeRangeForm.add(mainLabel, new FormData());
        timeRangeForm.add(main, new FormData());
        timeRangeForm.add(advancedRangeSet, new FormData());

        // advancedRangeSet.collapse();
    }

    private void initTimeRangePanel() {
        timeRangeForm = new FormPanel();
        timeRangeForm.setHeaderVisible(false);
        timeRangeForm.setBodyBorder(false);
        timeRangeForm.setLabelAlign(LabelAlign.TOP);

        initTimeRangeFields();
        initTimeRangeButtons();
        saveSelectedTimes();

        window.add(timeRangeForm);
    }

    private void initTypeButtons() {
        typesField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Radio label = typesField.getValue();
                if (label.equals(timeLineRadio) || label.equals(mapRadio)
                        || label.equals(networkRadio)) {
                    buttonToTimeRange.setText("Next");

                } else if (label.equals(tableRadio)) {
                    buttonToTimeRange.setText("Go!");

                } else {
                    LOG.warning("Unexpected selection: " + label);
                }
            }
        });

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (ce.getButton().equals(buttonToTimeRange)) {
                    if (buttonToTimeRange.getText().equalsIgnoreCase("next")) {
                        layout.setActiveItem(timeRangeForm);
                    } else {
                        // skip time range selection
                        submitEvent.setData("startTime", System.currentTimeMillis());
                        submitEvent.setData("endTime", 0);
                        submitForm();
                    }
                } else {
                    Dispatcher.forwardEvent(VizEvents.TypeChoiceCancelled);
                }
            }
        };

        Button back = new Button("Back", l);
        back.disable();
        typeForm.addButton(back);

        buttonToTimeRange = new Button("Next", l);
        buttonToTimeRange.setStyleAttribute("font-weight", "bold");
        typeForm.addButton(buttonToTimeRange);

        Button cancel = new Button("Cancel", l);
        typeForm.addButton(cancel);

        FormButtonBinding binding = new FormButtonBinding(typeForm);
        binding.addButton(buttonToTimeRange);
    }

    private void initTypeFields() {

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        LayoutContainer left = new LayoutContainer(new FormLayout());
        left.setStyleAttribute("paddingRight", "10px");

        typesField = new RadioGroup();
        typesField.setFieldLabel("Select a visualization type");

        timeLineRadio = new Radio();
        timeLineRadio.setBoxLabel("Time line");
        timeLineRadio.setHideLabel(true);
        timeLineRadio.setValue(true);
        left.add(timeLineRadio, new FormData());

        tableRadio = new Radio();
        tableRadio.setBoxLabel("Table");
        tableRadio.setHideLabel(true);
        left.add(tableRadio, new FormData());

        LayoutContainer right = new LayoutContainer(new FormLayout());
        right.setStyleAttribute("paddingLeft", "10px");

        mapRadio = new Radio();
        mapRadio.setBoxLabel("Map");
        mapRadio.setHideLabel(true);
        mapRadio.disable();
        right.add(mapRadio, new FormData());

        networkRadio = new Radio();
        networkRadio.setBoxLabel("Network");
        networkRadio.setHideLabel(true);
        networkRadio.disable();
        right.add(networkRadio, new FormData());

        // listen to changes in types field
        typesField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                saveSelectedType();
            }
        });

        // add the choices to the typesfield
        typesField.add(timeLineRadio);
        typesField.add(tableRadio);
        typesField.add(mapRadio);
        typesField.add(networkRadio);
        typesField.setOriginalValue(timeLineRadio);
        typesField.setSelectionRequired(true);

        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));

        LabelField label = new LabelField("Select a visualization type:");
        label.setHideLabel(true);
        typeForm.add(label, new FormData());
        typeForm.add(main, new FormData("100%"));

        // final FormData formData = new FormData("-10");
        // this.typeForm.add(main, formData);
    }

    private void initTypePanel() {
        typeForm = new FormPanel();
        typeForm.setHeaderVisible(false);
        typeForm.setBodyBorder(false);
        typeForm.setLabelAlign(LabelAlign.TOP);

        initTypeFields();
        initTypeButtons();
        saveSelectedType();

        window.add(typeForm);
    }

    /**
     * Saves the selected time range from the form into the AppEvent that will be dispatched when
     * the user presses "Go!".
     */
    private void saveSelectedTimes() {
        long endTime = System.currentTimeMillis();
        long startTime = endTime;

        // constants
        final long hour = 1000 * 60 * 60;
        final long day = 24 * hour;
        final long week = 7 * day;

        // see which radio was selected
        Radio selected = timeRangeField.getValue();
        if (hourRadio.equals(selected)) {
            startTime = endTime - hour;

        } else if (dayRadio.equals(selected)) {
            startTime = endTime - day;

        } else if (weekRadio.equals(selected)) {
            startTime = endTime - week;

        } else if (monthRadio.equals(selected)) {
            startTime = endTime - Math.round(29.53 * day);

        } else if (otherTimeRadio.equals(selected)) {
            DateWrapper startWrapper = new DateWrapper(startDateField.getValue());
            startWrapper = startWrapper.resetTime();
            startWrapper = startWrapper.addHours(startTimeField.getValue().getHour() - 12);
            startWrapper = startWrapper.addMinutes(startTimeField.getValue().getMinutes());
            startTime = startWrapper.getTime();

            DateWrapper endWrapper = new DateWrapper(endDateField.getValue());
            endWrapper = endWrapper.resetTime();
            endWrapper = endWrapper.addHours(endTimeField.getValue().getHour() - 12);
            endWrapper = endWrapper.addMinutes(endTimeField.getValue().getMinutes());
            endTime = endWrapper.getTime();

        } else {
            LOG.warning("Unexpected radio button selected: " + selected);
        }

        DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);
        LOG.fine("Start: " + dtf.format(new Date(startTime)));
        LOG.fine("End:   " + dtf.format(new Date(endTime)));

        // save the start and end time in the event
        submitEvent.setData("startTime", startTime);
        submitEvent.setData("endTime", endTime);
    }

    /**
     * Saves the selected visualization type from the form into the AppEvent that will be dispatched
     * when the user presses "Go!".
     */
    private void saveSelectedType() {

        Radio selected = typesField.getValue();
        if (timeLineRadio.equals(selected)) {
            submitEvent = new AppEvent(VizEvents.ShowTimeLine);
            submitEvent.setData("sensors", sensors);

            buttonToTimeRange.setText("Next");

        } else if (tableRadio.equals(selected)) {
            submitEvent = new AppEvent(VizEvents.ShowTable);
            submitEvent.setData("sensors", sensors);

            buttonToTimeRange.setText("Go!");

        } else if (mapRadio.equals(selected)) {
            submitEvent = new AppEvent(VizEvents.ShowMap);
            submitEvent.setData("sensors", locationSensors);

            buttonToTimeRange.setText("Next");

        } else if (networkRadio.equals(selected)) {
            submitEvent = new AppEvent(VizEvents.ShowNetwork);
            submitEvent.setData("sensors", sensors);

            buttonToTimeRange.setText("Next");

        } else {
            LOG.warning("Unexpected selection: " + selected);
        }
    }

    private void showWindow(List<SensorModel> sensors) {
        this.sensors = sensors;
        if (this.sensors.size() > 0) {
            window.show();
            window.center();
        } else {
            MessageBox.info(null, "No sensor types or devices selected, nothing to display.", null);
        }

        if (checkForLocationSensors(this.sensors)) {
            mapRadio.enable();
        } else {
            mapRadio.disable();

            // make sure the map radio button is not selected
            if (typesField.getValue().equals(mapRadio)) {
                typesField.setValue(timeLineRadio);
            }
        }
    }

    private void submitForm() {
        saveSelectedType();
        saveSelectedTimes();
        Dispatcher.forwardEvent(submitEvent);
        hideWindow();
    }
}
