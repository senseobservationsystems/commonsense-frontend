package nl.sense_os.commonsense.client.states.feedback;

import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class FeedbackView extends View {

    private static final String TAG = "FeedbackView";

    public FeedbackView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(FeedbackEvents.FeedbackInit)) {
            Log.d(TAG, "FeedbackInit");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            initPanel(state, sensors);

        } else if (type.equals(FeedbackEvents.LabelsSuccess)) {
            Log.d(TAG, "LabelsSuccess");
            final List<String> labels = event.<List<String>> getData("labels");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            showPanel(state, sensors, labels);

        } else if (type.equals(FeedbackEvents.LabelsFailure)) {
            Log.w(TAG, "LabelsFailure");
            onLabelsFailure();

        } else {
            Log.w(TAG, "Unexpected event type!");
        }
    }

    private void onLabelsFailure() {
        // TODO
        MessageBox.alert(null, "Failed to initialize feedback panel!", null);
    }

    private void initPanel(SensorModel state, List<SensorModel> sensors) {
        AppEvent labelRequest = new AppEvent(FeedbackEvents.LabelsRequest);
        labelRequest.setData("state", state);
        labelRequest.setData("sensors", sensors);
        fireEvent(labelRequest);
    }

    private void showPanel(SensorModel state, List<SensorModel> sensors, List<String> labels) {
        String title = state.<String> get("text");
        FeedbackPanel panel = new FeedbackPanel(state, sensors, title);

        AppEvent showEvent = new AppEvent(FeedbackEvents.ShowFeedback);
        showEvent.setData("panel", panel);
        showEvent.setData("title", title);
        Dispatcher.forwardEvent(showEvent);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

}
