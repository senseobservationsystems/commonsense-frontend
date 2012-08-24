package nl.sense_os.commonsense.main.client.viz.panels.map;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.util.SenseIconProvider;
import nl.sense_os.commonsense.main.client.viz.data.DataEvents;
import nl.sense_os.commonsense.main.client.viz.panels.VizPanelEvents;
import nl.sense_os.commonsense.main.client.viz.panels.VizView;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;

public class MapVizView extends VizView {

    private static final Logger LOG = Logger.getLogger(MapVizView.class.getName());

    private TabItem item;
    private MapPanel panel;

    private List<GxtSensor> sensors;
    private long start;
    private long end;
    private boolean subsample;

    private JsArray<Timeseries> data;

    public MapVizView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(VizPanelEvents.ShowMap)) {
            LOG.finest("ShowMap");
            final List<GxtSensor> sensors = event.<List<GxtSensor>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final boolean subsample = event.getData("subsample");
            showMap(sensors, start, end, subsample);

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

    private void showMap(List<GxtSensor> sensors, long start, long end, boolean subsample) {

        this.sensors = sensors;
        this.start = start;
        this.end = end;
        this.subsample = subsample;
        requestData(sensors, start, end, subsample);

        // add map tab item
        String title = createChartTitle(sensors);
        item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "map.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        panel = new MapPanel(sensors, start, end, subsample, title);
        item.add(panel);

        tabPanel.add(item);
        tabPanel.setSelection(item);

        addRefreshListeners(panel, item);
    }
}
