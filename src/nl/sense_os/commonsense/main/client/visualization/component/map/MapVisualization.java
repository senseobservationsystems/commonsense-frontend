package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.VisualizationView;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public class MapVisualization extends Composite implements VisualizationView {

    private static final Logger LOG = Logger.getLogger(MapVisualization.class.getName());
    private static final int REFRESH_PERIOD = 1000 * 10;
    private boolean isAutoRefresh;
    private Presenter presenter;
    private Timer refreshTimer = new Timer() {

        @Override
        public void run() {
            if (null != presenter) {
                presenter.refreshData();
            }
        }
    };
    private MapPanel map;
    private Map<Integer, LocationData> dataset;
    private MapVisualizationControls controlPanel;

    public MapVisualization(List<GxtSensor> sensors, long start, long end, boolean subsample) {

        ContentPanel panel = new ContentPanel(new BorderLayout());
        panel.setHeading("Map: " + createTitle(sensors));

        map = new MapPanel();
        controlPanel = new MapVisualizationControls(map);

        // Add the control panel
        BorderLayoutData controlPanelLayout = new BorderLayoutData(LayoutRegion.SOUTH, 75);
        controlPanelLayout.setMargins(new Margins(0, 5, 5, 5));
        panel.add(controlPanel, controlPanelLayout);

        // Add the map to the layout
        BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
        layoutData.setMargins(new Margins(5));
        panel.add(map, layoutData);

        initToolButtons(panel.getHeader());

        initComponent(panel);
    }

    private String createTitle(List<GxtSensor> sensors) {
        String title = null;
        for (GxtSensor sensor : sensors) {
            title = sensor.getDisplayName() + ", ";
        }

        // remove trailing ", "
        title = title.substring(0, title.length() - 2);

        return title;
    }

    /**
     * Group the incoming timeseries by their ID
     * 
     * @param data
     */
    private Map<Integer, LocationData> groupTimeseriesById(JsArray<Timeseries> data) {

        Map<Integer, LocationData> dataset = new HashMap<Integer, LocationData>();
        for (int i = 0; i < data.length(); i++) {

            Timeseries newTimeseries = data.get(i);
            int newId = newTimeseries.getId();

            // get location data for this sensor from the map
            LocationData locationData = dataset.get(newId);
            if (null == locationData) {
                locationData = new LocationData(null, null);
            }

            // add this timeseries to the location data
            String dataLabel = newTimeseries.getLabel();
            if (dataLabel.endsWith("latitude")) {
                locationData.setLatitudes(newTimeseries);
            } else if (dataLabel.endsWith("longitude")) {
                locationData.setLongitudes(newTimeseries);
            } else {
                LOG.fine("Timeseries not suitable for map: " + dataLabel);
            }

            dataset.put(newId, locationData);
        }

        // LOG.fine("The biglist size is "+ bigList.size());
        return dataset;
    }

    /**
     * Adds tool buttons to the panel's heading.
     */
    private void initToolButtons(Header header) {
        // regular refresh button
        ToolButton refresh = new ToolButton("x-tool-refresh");
        refresh.setToolTip("refresh");
        refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                if (null != presenter) {
                    presenter.refreshData();
                }
            }
        });

        // auto-refresh button
        final ToolButton autoRefresh = new ToolButton("x-tool-right");
        autoRefresh.setToolTip("start auto-refresh");
        autoRefresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                if (!isAutoRefresh) {
                    startAutoRefresh();
                    autoRefresh.setToolTip("stop autorefresh");
                    autoRefresh.setStylePrimaryName("x-tool-pin");
                } else {
                    stopAutoRefresh();
                    autoRefresh.setToolTip("start autorefresh");
                    autoRefresh.setStylePrimaryName("x-tool-right");
                }
            }
        });

        // add buttons to the header
        header.addTool(autoRefresh);
        header.addTool(refresh);
    }

    @Override
    public void onShow(Widget parent) {
        if (parent instanceof LayoutContainer) {
            ((LayoutContainer) parent).layout();
        }
        if (null != presenter) {
            if (null == dataset) {
                presenter.getData();
            } else {
                presenter.refreshData();
            }
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private void startAutoRefresh() {
        // request data refresh
        if (null != presenter) {
            presenter.refreshData();
        }

        // start timer
        refreshTimer.scheduleRepeating(REFRESH_PERIOD);
        isAutoRefresh = true;
    }

    private void stopAutoRefresh() {
        refreshTimer.cancel();
        isAutoRefresh = false;
    }

    @Override
    public void visualize(JsArray<Timeseries> data) {
        LOG.fine("Visualize " + data.length() + " timeseries");

        // group the Timeseries per sensor
        dataset = groupTimeseriesById(data);

        // filter dataset
        for (Entry<Integer, LocationData> entry : dataset.entrySet()) {
            LocationData locationData = entry.getValue();
            dataset.put(entry.getKey(), LocationDataFilter.filter(locationData));
        }

        map.setLocationDataSet(dataset);
        controlPanel.setLocatonDataSet(dataset);
    }
}
