package nl.sense_os.commonsense.client.viz.panels.timeline;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizPanelEvents;
import nl.sense_os.commonsense.client.viz.panels.VizView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;

public class TimeLineVizView extends VizView {

    private static final Logger LOG = Logger.getLogger(TimeLineVizView.class.getName());

    private TabItem item;
    private TimeLinePanel panel;

    private List<SensorModel> sensors;
    private long start;
    private long end;
    private boolean subsample;

    private JsArray<Timeseries> data;

    public TimeLineVizView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(VizPanelEvents.ShowTimeLine)) {
            LOG.finest("ShowTimeLine");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final boolean subsample = event.getData("subsample");
            showTimeLine(sensors, start, end, subsample);

        } else if (type.equals(DataEvents.DataReceived)) {
            LOG.finest("DataReceived");
            JsArray<Timeseries> data = event.getData("data");
            onDataReceived(data);

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    private void onDataReceived(JsArray<Timeseries> newData) {
        data = appendNewData(data, newData);
        panel.onNewData(data);
    }

    @Override
    protected void onRefresh() {
        refreshData(data, sensors, start, end, subsample);
    }

    private void showTimeLine(List<SensorModel> sensors, long start, long end, boolean subsample) {

        this.sensors = sensors;
        this.start = start;
        this.end = end;
        this.subsample = subsample;
        requestData(sensors, start, end, subsample);

        // add line chart tab item
        String title = createChartTitle(sensors);
        item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "chart.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        panel = new TimeLinePanel(sensors, start, end, subsample, title);
        item.add(panel);

        tabPanel.add(item);
        tabPanel.setSelection(item);

        addRefreshListeners(panel, item);
    }
}
