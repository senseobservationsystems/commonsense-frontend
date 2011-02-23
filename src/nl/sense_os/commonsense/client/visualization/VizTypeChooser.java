package nl.sense_os.commonsense.client.visualization;

import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.map.MapEvents;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.TreeModel;
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
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class VizTypeChooser extends View {

    private static final String TAG = "VizTypeChooser";
    private Window window;
    private CardLayout layout;
    private FormPanel typeForm;
    private FormPanel timeRangeForm;
    private TreeModel[] sensors;
    private TreeModel[] locationSensors;
    private AppEvent vizEvent;
    private Button buttonComplete;
    private Button buttonToTimeRange;
    private Button buttonToTypes;
    private RadioGroup timeRangeField;
    private RadioGroup typesField;
    private Radio lineChart;
    private Radio table;
    private Radio map;
    private Radio network;

    public VizTypeChooser(Controller c) {
        super(c);
    }

    private boolean checkForLocationSensors(TreeModel[] list) {

        // create array to send as parameter in RPC
        this.locationSensors = new TreeModel[0];
        for (TreeModel sensor : list) {

            String structure = sensor.<String> get("data_structure");

            if (null != structure && structure.contains("longitude")) {
                final TreeModel[] temp = new TreeModel[this.locationSensors.length + 1];
                System.arraycopy(this.locationSensors, 0, temp, 0, this.locationSensors.length);
                this.locationSensors = temp;
                this.locationSensors[this.locationSensors.length - 1] = sensor;

            } else {
                // do nothing
            }
        }

        // check whether there are any sensors at all
        if (this.locationSensors.length == 0) {
            return false;
        }
        return true;
    }

    private boolean checkForSensors(List<TreeModel> list) {

        // create array to send as parameter in RPC
        this.sensors = new TreeModel[0];
        for (TreeModel sensor : list) {

            // final TagModel tag = (TagModel) tsm.getModel();
            int tagType = sensor.<Integer> get("tagType");

            if (tagType == TagModel.TYPE_SENSOR) {
                final TreeModel[] temp = new TreeModel[this.sensors.length + 1];
                System.arraycopy(this.sensors, 0, temp, 0, this.sensors.length);
                temp[temp.length - 1] = sensor;
                this.sensors = temp;

            } else {
                // do nothing
            }
        }

        // check whether there are any sensors at all
        if (this.sensors.length == 0) {
            return false;
        }
        return true;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(VizEvents.ShowTypeChoice)) {
            onShow(event);
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

        this.vizEvent = new AppEvent(VizEvents.ShowLineChart);

        this.window = new Window();
        this.window.setHeading("Visualization wizard");
        this.window.setSize(323, 200);
        this.window.setResizable(false);

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
                if (label.equals(lineChart) || label.equals(map) || label.equals(network)) {
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
                        vizEvent.setData("startTime", System.currentTimeMillis());
                        vizEvent.setData("endTime", 0);
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

        this.typesField = new RadioGroup();
        this.typesField.setFieldLabel("Select a visualization type");

        this.lineChart = new Radio();
        this.lineChart.setBoxLabel("Line chart");
        this.lineChart.setValue(true);

        this.table = new Radio();
        this.table.setBoxLabel("Table");

        this.map = new Radio();
        this.map.setBoxLabel("Map");
        this.map.disable();

        this.network = new Radio();
        this.network.setBoxLabel("Network");
        this.network.disable();

        // listen to changes in types field
        this.typesField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                saveSelectedType();
            }
        });

        // add the choices to the typesfield
        this.typesField.add(lineChart);
        this.typesField.add(table);
        this.typesField.add(map);
        this.typesField.add(network);
        this.typesField.setOriginalValue(lineChart);
        this.typesField.setSelectionRequired(true);

        final FormData formData = new FormData("-10");
        this.typeForm.add(this.typesField, formData);
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

    private void onShow(AppEvent event) {
        List<TreeModel> tags = event.<List<TreeModel>> getData();
        if (checkForSensors(tags)) {
            this.window.show();
        } else {
            MessageBox.info(null, "No sensor types or devices selected, nothing to display.", null);
        }

        if (checkForLocationSensors(this.sensors)) {
            this.map.enable();
        } else {
            this.map.disable();
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

        /*
         * // hack the end time for some specific demos long end = System.currentTimeMillis();
         * UserModel user = Registry.get(Constants.REG_USER); if (null != user && user.getId() ==
         * 134) { Log.d(TAG, "delfgauw time hack"); end = 1283603962000l; // 4 september, 14:39.220
         * CEST } else if (null != user && user.getId() == 142) { Log.d(TAG,
         * "greenhouse time hack"); end = 1288609200000l; // 2 november, 12:00 CET }
         */

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
        this.vizEvent.setData("startTime", startTime);
        this.vizEvent.setData("endTime", endTime);
    }

    /**
     * Saves the selected visualization type from the form into the AppEvent that will be dispatched
     * when the user pressed "Go!".
     */
    private void saveSelectedType() {
        Radio label = typesField.getValue();
        if (label.equals(this.lineChart)) {
            this.vizEvent.setType(VizEvents.ShowLineChart);
            this.vizEvent.setData("sensors", this.sensors);

            buttonToTimeRange.setText("Next");
        } else if (label.equals(this.table)) {
            this.vizEvent.setType(VizEvents.ShowTable);
            this.vizEvent.setData("sensors", this.sensors);

            buttonToTimeRange.setText("Go!");
        } else if (label.equals(this.map)) {
            this.vizEvent.setType(MapEvents.Show);
            this.vizEvent.setData("sensors", this.locationSensors);
            // this.vizEvent.setData("sensors", this.sensors);

            buttonToTimeRange.setText("Next");
        } else if (label.equals(this.network)) {
            this.vizEvent.setType(VizEvents.ShowNetwork);
            this.vizEvent.setData("sensors", this.sensors);

            buttonToTimeRange.setText("Next");
        } else {
            Log.w(TAG, "Unexpected selection: " + label);
        }
    }

    private void submitForm() {
        saveSelectedType();
        saveSelectedTimes();
        Dispatcher.forwardEvent(this.vizEvent);
        hideWindow();
    }
}
