package nl.sense_os.commonsense.client.mvc.views;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.VizEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
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

public class VizTypeChooser extends View {

    private static final String TAG = "VizTypeChooser";
    private Window window;
    private CardLayout layout;
    private FormPanel typeForm;
    private FormPanel timeRangeForm;
    private TreeModel[] sensors;
    private EventType eventType;
    private Button buttonComplete;
    private Button buttonToTimeRange;
    private Button buttonToTypes;
    private RadioGroup timeRangeField;
    private RadioGroup typesField;
    private long startTime;
    private long endTime;

    public VizTypeChooser(Controller c) {
        super(c);
    }

    private void saveSelectedType() {
        String label = typesField.getValue().getBoxLabel();
        if (label.equalsIgnoreCase("Line chart")) {
            eventType = VizEvents.ShowLineChart;
        } else if (label.equalsIgnoreCase("Table")) {
            eventType = VizEvents.ShowTable;
        } else if (label.equalsIgnoreCase("Map")) {
            eventType = VizEvents.ShowMap;
        } else if (label.equalsIgnoreCase("Network")) {
            eventType = VizEvents.ShowNetwork;
        } else {
            Log.w(TAG, "Unexpected radio button label: " + label);
        }
    }

    private void saveSelectedTimes() {
        endTime = System.currentTimeMillis();

        // constants
        final long hour = 1000 * 60 * 60;
        final long day = 24 * hour;
        final long week = 7 * day;

        String label = timeRangeField.getValue().getBoxLabel();
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

        this.window = new Window();
        this.window.setHeading("Visualization wizard");
        this.window.setSize(350, 200);
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
                    AppEvent event = new AppEvent(eventType);
                    event.setData("sensors", sensors);
                    event.setData("startTime", startTime);
                    event.setData("endTime", endTime);
                    Dispatcher.forwardEvent(event);
                    hideWindow();
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

        FormButtonBinding binding = new FormButtonBinding(typeForm);
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

        this.timeRangeForm.add(this.timeRangeField);
    }

    private void initTimeRangePanel() {
        this.timeRangeForm = new FormPanel();
        this.timeRangeForm.setHeaderVisible(false);
        this.timeRangeForm.setLabelAlign(LabelAlign.TOP);

        initTimeRangeFields();
        initTimeRangeButtons();

        this.window.add(this.timeRangeForm);
    }

    private void initTypeButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (ce.getButton().equals(buttonToTimeRange)) {
                    saveSelectedType();
                    layout.setActiveItem(timeRangeForm);
                } else {
                    Dispatcher.forwardEvent(VizEvents.TypeChoiceCancelled);
                }
            }
        };

        Button back = new Button("Back", l);
        back.disable();
        this.typeForm.addButton(back);

        this.buttonToTimeRange = new Button("Next", l);
        this.typeForm.addButton(this.buttonToTimeRange);

        Button cancel = new Button("Cancel", l);
        this.typeForm.addButton(cancel);

        FormButtonBinding binding = new FormButtonBinding(this.typeForm);
        binding.addButton(this.buttonToTimeRange);
    }

    private void initTypeFields() {
        this.typesField = new RadioGroup();
        this.typesField.setFieldLabel("Select a visualization type");

        Radio lineChart = new Radio();
        lineChart.setBoxLabel("Line chart");

        Radio table = new Radio();
        table.setBoxLabel("Table");

        Radio map = new Radio();
        map.setBoxLabel("Map");
        map.disable();

        Radio network = new Radio();
        network.setBoxLabel("Network");
        network.disable();

        this.typesField.add(lineChart);
        this.typesField.add(table);
        this.typesField.add(map);
        this.typesField.add(network);
        this.typesField.setOriginalValue(lineChart);
        this.typesField.setSelectionRequired(true);

        this.typeForm.add(this.typesField);
    }

    private void initTypePanel() {
        this.typeForm = new FormPanel();
        this.typeForm.setHeaderVisible(false);
        this.typeForm.setLabelAlign(LabelAlign.TOP);

        initTypeFields();
        initTypeButtons();

        this.window.add(this.typeForm);
    }

    private boolean checkSensors(List<TreeModel> list) {
        // create array to send as parameter in RPC
        sensors = new TreeModel[0];
        for (TreeModel tag : list) {
            // final TagModel tag = (TagModel) tsm.getModel();
            int tagType = tag.<Integer> get("tagType");
            if (tagType == TagModel.TYPE_SENSOR) {
                final TreeModel[] temp = new TreeModel[sensors.length + 1];
                System.arraycopy(sensors, 0, temp, 0, sensors.length);
                temp[temp.length - 1] = tag;
                sensors = temp;
            } else {
                // do nothing
            }
        }

        // check whether there are any tags at all
        if (sensors.length == 0) {
            return false;
        }
        return true;
    }

    private void onShow(AppEvent event) {
        List<TreeModel> tags = event.<List<TreeModel>> getData();
        if (checkSensors(tags)) {
            this.window.show();            
        } else {
            MessageBox.info(null, "No sensor types or devices selected, nothing to display.", null);
        }
    }
}
