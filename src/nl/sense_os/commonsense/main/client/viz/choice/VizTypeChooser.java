package nl.sense_os.commonsense.main.client.viz.choice;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.ext.component.TimeRangeForm;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.viz.panels.VizPanelEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

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

    private static final Logger LOG = Logger.getLogger(VizTypeChooser.class.getName());

    private Window window;
    private CardLayout layout;

    private List<ExtSensor> sensors;
    private List<ExtSensor> locationSensors;

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

    private TimeRangeForm timeRangeForm;

    public VizTypeChooser(Controller c) {
        super(c);
    }

    private boolean checkForLocationSensors(List<ExtSensor> list) {

        // create array to send as parameter in RPC
        locationSensors = new ArrayList<ExtSensor>();
        for (ExtSensor sensor : list) {

            String structure = sensor.<String> get("data_structure");

            if (null != structure && structure.contains("longitude")) {
                locationSensors.add(new ExtSensor(sensor.getProperties()));

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
            List<ExtSensor> sensors = event.<List<ExtSensor>> getData();
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

        submitEvent = new AppEvent(VizPanelEvents.ShowTimeLine);

        window = new CenteredWindow();
        window.setHeadingText("Visualization wizard");
        window.setMinWidth(425);
        window.setMinHeight(305);
        window.setClosable(false);

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

    private void initTimeRangePanel() {
        timeRangeForm = new TimeRangeForm();
        timeRangeForm.setLabel("Select the time range to visualize:");
        initTimeRangeButtons();
        saveSelectedTimes();

        window.add(timeRangeForm);
    }

    private void initTypeButtons() {
        typesField.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Radio label = typesField.getValue();
                if (label.equals(timeLineRadio) || label.equals(tableRadio)
                        || label.equals(mapRadio) || label.equals(networkRadio)) {
                    buttonToTimeRange.setText("Next");
                    timeRangeForm.setSubsampleEnabled(!label.equals(tableRadio));

                } else {
                    LOG.warning("Unexpected selection: " + label);
                }
            }
        });

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (ce.getButton().equals(buttonToTimeRange)) {
                    String buttonLabel = buttonToTimeRange.getHtml();
                    if (buttonLabel.equalsIgnoreCase("next")) {
                        layout.setActiveItem(timeRangeForm);
                    } else if (buttonLabel.equalsIgnoreCase("go!")) {
                        submitForm();
                    } else {
                        LOG.warning("Unexpected button pressed: " + buttonLabel);
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
        submitEvent.setData("startTime", timeRangeForm.getStartTime());
        submitEvent.setData("endTime", timeRangeForm.getEndTime());
        submitEvent.setData("subsample", timeRangeForm.getSubsample());
    }

    /**
     * Saves the selected visualization type from the form into the AppEvent that will be dispatched
     * when the user presses "Go!".
     */
    private void saveSelectedType() {

        Radio selected = typesField.getValue();
        if (timeLineRadio.equals(selected)) {
            submitEvent = new AppEvent(VizPanelEvents.ShowTimeLine);
            submitEvent.setData("sensors", sensors);

            buttonToTimeRange.setText("Next");

        } else if (tableRadio.equals(selected)) {
            submitEvent = new AppEvent(VizPanelEvents.ShowTable);
            submitEvent.setData("sensors", sensors);

            buttonToTimeRange.setText("Next");

        } else if (mapRadio.equals(selected)) {
            submitEvent = new AppEvent(VizPanelEvents.ShowMap);
            submitEvent.setData("sensors", locationSensors);

            buttonToTimeRange.setText("Next");

        } else if (networkRadio.equals(selected)) {
            submitEvent = new AppEvent(VizPanelEvents.ShowNetwork);
            submitEvent.setData("sensors", sensors);

            buttonToTimeRange.setText("Next");

        } else {
            LOG.warning("Unexpected selection: " + selected);
        }
    }

    private void showWindow(List<ExtSensor> sensors) {
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
