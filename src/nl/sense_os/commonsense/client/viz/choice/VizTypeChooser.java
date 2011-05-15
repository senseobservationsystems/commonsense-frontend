package nl.sense_os.commonsense.client.viz.choice;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
import nl.sense_os.commonsense.shared.models.SensorModel;

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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class VizTypeChooser extends View {

    private static final String TAG = "VizTypeChooser";

    private Window window;
    private CardLayout layout;
    private FormPanel typeForm;
    private FormPanel timeRangeForm;
    private Button buttonComplete;
    private Button buttonToTimeRange;
    private Button buttonToTypes;
    private RadioGroup timeRangeField;
    private RadioGroup typesField;
    private Radio timeLine;
    private Radio table;
    private Radio map;
    private Radio network;

    private List<SensorModel> sensors;
    private List<SensorModel> locationSensors;
    private AppEvent submitEvent;

    public VizTypeChooser(Controller c) {
        super(c);
    }

    private boolean checkForLocationSensors(List<SensorModel> list) {

        // create array to send as parameter in RPC
        this.locationSensors = new ArrayList<SensorModel>();
        for (SensorModel sensor : list) {

            String structure = sensor.<String> get("data_structure");

            if (null != structure && structure.contains("longitude")) {
                this.locationSensors.add(new SensorModel(sensor.getProperties()));

            } else {
                // do nothing
            }
        }

        // check whether there are any sensors at all
        if (this.locationSensors.size() == 0) {
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
            Log.d(TAG, "Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        this.window.hide();
        this.layout.setActiveItem(this.typeForm);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.submitEvent = new AppEvent(DataEvents.DataRequest);

        this.window = new CenteredWindow();
        this.window.setHeading("Visualization wizard");
        this.window.setMinWidth(323);
        this.window.setMinHeight(200);

        this.layout = new CardLayout();
        this.window.setLayout(this.layout);

        initTypePanel();
        initTimeRangePanel();

        this.layout.setActiveItem(this.window.getItem(0));
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

        this.buttonToTypes = new Button("Back", l);
        this.timeRangeForm.addButton(this.buttonToTypes);

        this.buttonComplete = new Button("Go!", l);
        this.timeRangeForm.addButton(this.buttonComplete);

        Button cancel = new Button("Cancel", l);
        this.timeRangeForm.addButton(cancel);

        FormButtonBinding binding = new FormButtonBinding(this.timeRangeForm);
        binding.addButton(this.buttonComplete);
    }

    private void initTimeRangeFields() {
        this.timeRangeField = new RadioGroup();
        this.timeRangeField.setFieldLabel("Select the time range to visualize");

        final Radio radio1Hr = new Radio();
        radio1Hr.setBoxLabel("1 hour");

        final Radio radioDay = new Radio();
        radioDay.setBoxLabel("1 day");
        radioDay.setValue(true);

        final Radio radioWeek = new Radio();
        radioWeek.setBoxLabel("1 week");

        final Radio radioMonth = new Radio();
        radioMonth.setBoxLabel("4 weeks");

        this.timeRangeField.add(radio1Hr);
        this.timeRangeField.add(radioDay);
        this.timeRangeField.add(radioWeek);
        this.timeRangeField.add(radioMonth);
        this.timeRangeField.setOriginalValue(radioDay);
        this.timeRangeField.setSelectionRequired(true);

        final FormData formData = new FormData("-10");
        this.timeRangeForm.add(this.timeRangeField, formData);
    }

    private void initTimeRangePanel() {
        this.timeRangeForm = new FormPanel();
        this.timeRangeForm.setHeaderVisible(false);
        this.timeRangeForm.setBodyBorder(false);
        this.timeRangeForm.setLabelAlign(LabelAlign.TOP);

        initTimeRangeFields();
        initTimeRangeButtons();
        saveSelectedTimes();

        this.window.add(this.timeRangeForm);
    }

    private void initTypeButtons() {
        this.typesField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Radio label = typesField.getValue();
                if (label.equals(timeLine) || label.equals(map) || label.equals(network)) {
                    buttonToTimeRange.setText("Next");

                } else if (label.equals(table)) {
                    buttonToTimeRange.setText("Go!");

                } else {
                    Log.w(TAG, "Unexpected selection: " + label);
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
        this.typeForm.addButton(back);

        this.buttonToTimeRange = new Button("Next", l);
        this.buttonToTimeRange.setStyleAttribute("font-weight", "bold");
        this.typeForm.addButton(this.buttonToTimeRange);

        Button cancel = new Button("Cancel", l);
        this.typeForm.addButton(cancel);

        FormButtonBinding binding = new FormButtonBinding(this.typeForm);
        binding.addButton(this.buttonToTimeRange);
    }

    private void initTypeFields() {

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px");
        FormLayout layout = new FormLayout();
        left.setLayout(layout);

        this.typesField = new RadioGroup();
        this.typesField.setFieldLabel("Select a visualization type");

        this.timeLine = new Radio();
        this.timeLine.setBoxLabel("Time line");
        this.timeLine.setHideLabel(true);
        this.timeLine.setValue(true);
        left.add(this.timeLine, new FormData());

        this.table = new Radio();
        this.table.setBoxLabel("Table");
        this.table.setHideLabel(true);
        left.add(this.table, new FormData());

        LayoutContainer right = new LayoutContainer();
        right.setStyleAttribute("paddingLeft", "10px");
        layout = new FormLayout();
        right.setLayout(layout);

        this.map = new Radio();
        this.map.setBoxLabel("Map");
        this.map.setHideLabel(true);
        this.map.disable();
        right.add(this.map, new FormData());

        this.network = new Radio();
        this.network.setBoxLabel("Network");
        this.network.setHideLabel(true);
        this.network.disable();
        right.add(this.network, new FormData());

        // listen to changes in types field
        this.typesField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                saveSelectedType();
            }
        });

        // add the choices to the typesfield
        this.typesField.add(timeLine);
        this.typesField.add(table);
        this.typesField.add(map);
        this.typesField.add(network);
        this.typesField.setOriginalValue(timeLine);
        this.typesField.setSelectionRequired(true);

        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));

        LabelField label = new LabelField("Select a visualization type:");
        label.setHideLabel(true);
        this.typeForm.add(label, new FormData());
        this.typeForm.add(main, new FormData("100%"));

        // final FormData formData = new FormData("-10");
        // this.typeForm.add(main, formData);
    }

    private void initTypePanel() {
        this.typeForm = new FormPanel();
        this.typeForm.setHeaderVisible(false);
        this.typeForm.setBodyBorder(false);
        this.typeForm.setLabelAlign(LabelAlign.TOP);

        initTypeFields();
        initTypeButtons();
        saveSelectedType();

        this.window.add(this.typeForm);
    }

    private void showWindow(List<SensorModel> sensors) {
        this.sensors = sensors;
        if (this.sensors.size() > 0) {
            this.window.show();
            this.window.center();
        } else {
            MessageBox.info(null, "No sensor types or devices selected, nothing to display.", null);
        }

        if (checkForLocationSensors(this.sensors)) {
            this.map.enable();
        } else {
            this.map.disable();

            // make sure the map radio button is not selected
            if (this.typesField.getValue().equals(this.map)) {
                this.typesField.setValue(this.timeLine);
            }
        }
    }

    /**
     * Saves the selected time range from the form into the AppEvent that will be dispatched when
     * the user pressed "Go!".
     */
    private void saveSelectedTimes() {
        final long endTime = System.currentTimeMillis();

        // constants
        final long hour = 1000 * 60 * 60;
        final long day = 24 * hour;
        final long week = 7 * day;

        String label = this.timeRangeField.getValue().getBoxLabel();
        long startTime = endTime;
        if (label.equals("1 hour")) {
            startTime = endTime - hour;
        } else if (label.equals("1 day")) {
            startTime = endTime - day;
        } else if (label.equals("1 week")) {
            startTime = endTime - week;
        } else if (label.equals("4 weeks")) {
            startTime = endTime - (4 * week);
        } else {
            Log.w(TAG, "Unexpected radio button label: " + label);
        }

        // save the start and end time in the event
        this.submitEvent.setData("startTime", startTime);
        this.submitEvent.setData("endTime", endTime);
    }

    /**
     * Saves the selected visualization type from the form into the AppEvent that will be dispatched
     * when the user pressed "Go!".
     */
    private void saveSelectedType() {
        Radio label = typesField.getValue();
        if (label.equals(this.timeLine)) {
            this.submitEvent = new AppEvent(VizEvents.ShowTimeLine);
            this.submitEvent.setData("sensors", this.sensors);

            this.buttonToTimeRange.setText("Next");

        } else if (label.equals(this.table)) {
            this.submitEvent = new AppEvent(VizEvents.ShowTable);
            this.submitEvent.setData("sensors", this.sensors);

            this.buttonToTimeRange.setText("Go!");

        } else if (label.equals(this.map)) {
            this.submitEvent = new AppEvent(VizEvents.ShowMap);
            this.submitEvent.setData("sensors", this.locationSensors);
            // this.submitEvent.setData("sensors", this.sensors);

            this.buttonToTimeRange.setText("Next");

        } else if (label.equals(this.network)) {
            this.submitEvent = new AppEvent(VizEvents.ShowNetwork);
            this.submitEvent.setData("sensors", this.sensors);

            this.buttonToTimeRange.setText("Next");

        } else {
            Log.w(TAG, "Unexpected selection: " + label);
        }
    }

    private void submitForm() {
        saveSelectedType();
        saveSelectedTimes();
        Dispatcher.forwardEvent(this.submitEvent);
        hideWindow();
    }
}
