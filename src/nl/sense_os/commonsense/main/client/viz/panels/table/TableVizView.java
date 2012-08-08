package nl.sense_os.commonsense.main.client.viz.panels.table;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.ExtSensor;
import nl.sense_os.commonsense.common.client.util.SenseIconProvider;
import nl.sense_os.commonsense.main.client.viz.panels.VizPanelEvents;
import nl.sense_os.commonsense.main.client.viz.panels.VizView;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class TableVizView extends VizView {

    private static final Logger LOG = Logger.getLogger(TableVizView.class.getName());

    private TabItem item;
    private SensorDataGrid panel;

    public TableVizView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(VizPanelEvents.ShowTable)) {
            LOG.finest("ShowTable");
            final List<ExtSensor> sensors = event.<List<ExtSensor>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final boolean subsample = event.getData("subsample");
            showTable(sensors, start, end, subsample);

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    @Override
    protected void onRefresh() {
        // nothing to do
    }

    private void showTable(List<ExtSensor> sensors, long start, long end, boolean subsample) {

        // add table tab item
        item = new TabItem(createChartTitle(sensors));
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "table.png"));
        item.setClosable(true);
        item.setScrollMode(Scroll.AUTO);
        item.setLayout(new FitLayout());

        tabPanel.add(item);
        tabPanel.setSelection(item);

        // add sensor data grid
        panel = new SensorDataGrid(sensors, start, end);
        item.add(panel);
        item.layout();
    }
}
