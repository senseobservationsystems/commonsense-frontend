package nl.sense_os.commonsense.client.states.feedback;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.TimeRangeForm;
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
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class FeedbackChooser extends View {

    private static final Logger LOG = Logger.getLogger(FeedbackChooser.class.getName());

    public FeedbackChooser(Controller c) {
        super(c);
        // LOG.setLevel(Level.ALL);
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
        w.setMinWidth(425);
        w.setMinHeight(305);

        final TimeRangeForm form = new TimeRangeForm();
        form.setLabel("Select the time range to give feedback:");

        w.add(form);

        Button submit = new Button("Go!", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {

                final long startTime = form.getStartTime();
                final long endTime = form.getEndTime();
                final boolean subsample = form.getSubsample();

                AppEvent proceed = new AppEvent(FeedbackEvents.FeedbackChosen);
                proceed.setData("state", state);
                proceed.setData("sensors", sensors);
                proceed.setData("labels", labels);
                proceed.setData("start", startTime);
                proceed.setData("end", endTime);
                proceed.setData("subsample", subsample);
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
