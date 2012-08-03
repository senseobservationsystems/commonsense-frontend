package nl.sense_os.commonsense.client.states.feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizView;
import nl.sense_os.commonsense.common.client.model.SensorModel;
import nl.sense_os.commonsense.common.client.util.SenseIconProvider;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;

public class FeedbackView extends VizView {

    private static final Logger LOG = Logger.getLogger(FeedbackView.class.getName());

    private TabItem item;
    private FeedbackPanel panel;

    private SensorModel state;
    private List<SensorModel> sensors;
    private long start;
    private long end;
    private boolean subsample;

    private JsArray<Timeseries> data;

    public FeedbackView(Controller c) {
        super(c);
        // LOG.setLevel(Level.ALL);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(FeedbackEvents.FeedbackInit)) {
            LOG.finest("FeedbackInit");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onInitEvent(state, sensors);

        } else

        /*
         * Request for labels
         */
        if (type.equals(FeedbackEvents.LabelsSuccess)) {
            LOG.finest("LabelsSuccess");
            final List<String> labels = event.<List<String>> getData("labels");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            onLabelsSuccess(state, sensors, labels);

        } else if (type.equals(FeedbackEvents.LabelsFailure)) {
            LOG.warning("LabelsFailure");
            onLabelsFailure();

        } else

        /*
         * Feedback chooser panel has finished
         */
        if (type.equals(FeedbackEvents.FeedbackChosen)) {
            LOG.finest("FeedbackChosen");
            final List<String> labels = event.<List<String>> getData("labels");
            final SensorModel state = event.<SensorModel> getData("state");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("start");
            final long end = event.getData("end");
            final boolean subsample = event.getData("subsample");
            showPanel(state, sensors, labels, start, end, subsample);

        } else

        if (type.equals(DataEvents.DataReceived)) {
            LOG.finest("DataReceived");
            JsArray<Timeseries> data = event.getData("data");
            onDataReceived(data);

        } else

        /*
         * Feedback results
         */
        if (type.equals(FeedbackEvents.FeedbackComplete)) {
            LOG.fine("FeedbackComplete");
            final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
            panel.onFeedbackComplete();

        } else if (type.equals(FeedbackEvents.FeedbackFailed)) {
            LOG.warning("FeedbackFailed");
            final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
            panel.onFeedbackFailed();

        } else

        {
            LOG.warning("Unexpected event type!");
        }
    }

    private void onDataReceived(JsArray<Timeseries> newData) {
        data = appendNewData(data, newData);
        panel.onNewData(data);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    private void onInitEvent(SensorModel state, List<SensorModel> sensors) {
        AppEvent labelRequest = new AppEvent(FeedbackEvents.LabelsRequest);
        labelRequest.setData("state", state);
        labelRequest.setData("sensors", sensors);
        fireEvent(labelRequest);
    }

    private void onLabelsFailure() {
        MessageBox.alert(null, "Failed to initialize feedback panel!", null);
    }

    private void onLabelsSuccess(SensorModel state, List<SensorModel> sensors, List<String> labels) {
        AppEvent showChooser = new AppEvent(FeedbackEvents.ShowChooser);
        showChooser.setData("state", state);
        showChooser.setData("sensors", sensors);
        showChooser.setData("labels", labels);
        fireEvent(showChooser);
    }

    @Override
    protected void onRefresh() {
        List<SensorModel> allSensors = new ArrayList<SensorModel>(sensors);
        allSensors.add(state);
        refreshData(data, allSensors, start, end, subsample);
    }

    private void showPanel(SensorModel state, List<SensorModel> sensors, List<String> labels,
            long start, long end, boolean subsample) {

        this.state = state;
        this.sensors = sensors;
        this.start = start;
        this.end = end;
        this.subsample = subsample;

        List<SensorModel> allSensors = new ArrayList<SensorModel>(sensors);
        allSensors.add(state);
        requestData(allSensors, start, end, subsample);

        String title = state.getDisplayName();

        // add feedback tab item
        item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "setting_tools.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        panel = new FeedbackPanel(state, sensors, start, end, subsample, title, labels);
        item.add(panel);

        tabPanel.add(item);
        tabPanel.setSelection(item);

        addRefreshListeners(panel, item);
    }
}
