package nl.sense_os.commonsense.client.states.feedback;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class FeedbackChooser extends View {

    private static final Logger LOG = Logger.getLogger(FeedbackChooser.class.getName());

    public FeedbackChooser(Controller c) {
        super(c);
        LOG.setLevel(Level.WARNING);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(FeedbackEvents.ShowChooser)) {
            LOG.finest("ShowChooser");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final List<String> labels = event.<List<String>> getData("labels");
            showChooser(state, sensors, labels);

        } else {
            LOG.warning("Unexpected event: " + event);
        }

    }

    private void showChooser(final SensorModel state, final List<SensorModel> sensors,
            final List<String> labels) {
        final Window w = new Window();
        w.setLayout(new FitLayout());
        w.setHeading("Feedback panel settings");
        w.setMinWidth(323);
        w.setMinHeight(200);

        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setLabelAlign(LabelAlign.TOP);

        final RadioGroup timeRangeField = new RadioGroup();
        timeRangeField.setFieldLabel("Select the time range to give feedback");

        final Radio radio1Hr = new Radio();
        radio1Hr.setBoxLabel("1 hour");

        final Radio radioDay = new Radio();
        radioDay.setBoxLabel("1 day");
        radioDay.setValue(true);

        final Radio radioWeek = new Radio();
        radioWeek.setBoxLabel("1 week");

        final Radio radioMonth = new Radio();
        radioMonth.setBoxLabel("4 weeks");

        timeRangeField.add(radio1Hr);
        timeRangeField.add(radioDay);
        timeRangeField.add(radioWeek);
        timeRangeField.add(radioMonth);
        timeRangeField.setOriginalValue(radioDay);
        timeRangeField.setSelectionRequired(true);

        final FormData formData = new FormData("-10");
        form.add(timeRangeField, formData);

        w.add(form);

        Button submit = new Button("Go!", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {

                final long endTime = System.currentTimeMillis();

                // constants
                final long hour = 1000 * 60 * 60;
                final long day = 24 * hour;
                final long week = 7 * day;

                String label = timeRangeField.getValue().getBoxLabel();
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
                    LOG.warning("Unexpected radio button label: " + label);
                }

                LOG.finest("Start time: " + startTime + ", end time: " + endTime);

                AppEvent proceed = new AppEvent(FeedbackEvents.FeedbackChosen);
                proceed.setData("state", state);
                proceed.setData("sensors", sensors);
                proceed.setData("labels", labels);
                proceed.setData("start", startTime);
                proceed.setData("end", endTime);
                fireEvent(proceed);

                w.hide();
            }
        });
        submit.setMinWidth(75);
        Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                w.hide();
            }
        });
        cancel.setMinWidth(75);

        ButtonBar buttons = new ButtonBar();
        buttons.setAlignment(HorizontalAlignment.CENTER);
        buttons.add(submit);
        buttons.add(cancel);

        w.setBottomComponent(buttons);

        w.show();
    }
}
