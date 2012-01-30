package nl.sense_os.commonsense.client.viz.panels;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.main.components.NavPanel;
import nl.sense_os.commonsense.client.states.feedback.FeedbackEvents;
import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.data.DataRequestEvent;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.map.MapPanel;
import nl.sense_os.commonsense.client.viz.panels.table.SensorDataGrid;
import nl.sense_os.commonsense.client.viz.panels.timeline.TimeLinePanel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;

public class VizView extends View {

    /**
     * Timer to periodically refresh the data for the visualization.
     */
    private class RefreshTimer extends Timer {

        @Override
        public void run() {
            refreshData();
        }
    }

    private static final Logger LOG = Logger.getLogger(VizView.class.getName());

    private TabItem item;
    private VizPanel panel;

    private List<SensorModel> sensors;
    private long start;
    private long end;
    private boolean subsample;

    private JsArray<Timeseries> data;

    private static final int REFRESH_PERIOD = 1000 * 10;
    private RefreshTimer refreshTimer;
    private boolean isAutoRefresh;

    public VizView(Controller c) {
        super(c);
    }

    private void addListeners() {

        final ToolButton refresh = panel.getRefresh();
        if (null != refresh) {
            refresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

                @Override
                public void componentSelected(IconButtonEvent ce) {
                    refreshData();
                }
            });
        }

        final ToolButton autoRefresh = panel.getAutoRefresh();
        if (null != autoRefresh) {
            autoRefresh.addSelectionListener(new SelectionListener<IconButtonEvent>() {

                @Override
                public void componentSelected(IconButtonEvent ce) {

                    if (null == refreshTimer) {
                        registerHideListener();
                        refreshTimer = new RefreshTimer();
                        isAutoRefresh = false;
                    }

                    if (!isAutoRefresh) {
                        refreshData();
                        refreshTimer.scheduleRepeating(REFRESH_PERIOD);
                        isAutoRefresh = true;
                        autoRefresh.setToolTip("stop autorefresh");
                        autoRefresh.setStylePrimaryName("x-tool-pin");
                    } else {
                        refreshTimer.cancel();
                        isAutoRefresh = false;
                        autoRefresh.setToolTip("start autorefresh");
                        autoRefresh.setStylePrimaryName("x-tool-right");
                    }
                }
            });
        }

        panel.addListener(Events.BeforeHide, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                if (null != refreshTimer) {
                    refreshTimer.cancel();
                    isAutoRefresh = false;
                    refreshTimer = null;
                }

                // remove tab item from tab panel
                item.removeFromParent();
            }
        });
    }

    private String createChartTitle(List<SensorModel> sensors) {
        String title = null;
        for (SensorModel sensor : sensors) {
            title = sensor.getDisplayName() + ", ";
        }

        // remove trailing ", "
        title = title.substring(0, title.length() - 2);

        // // trim to max length
        // if (title.length() > 18) {
        // title = title.substring(0, 15) + "...";
        // }
        return title;
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(FeedbackEvents.ShowFeedback)) {
            LOG.finest("ShowFeedback");
            final VizPanel panel = event.<VizPanel> getData("panel");
            final String title = event.<String> getData("title");
            showFeedback(panel, title);

        } else if (type.equals(VizPanelEvents.ShowTimeLine)) {
            LOG.finest("ShowTimeLine");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final boolean subsample = event.getData("subsample");
            showTimeLine(sensors, start, end, subsample);

        } else if (type.equals(VizPanelEvents.ShowTable)) {
            LOG.finest("ShowTable");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long startTime = event.getData("startTime");
            final long endTime = event.getData("endTime");
            showTable(sensors, startTime, endTime);

        } else if (type.equals(VizPanelEvents.ShowMap)) {
            LOG.finest("ShowMap");
            final List<SensorModel> sensors = event.<List<SensorModel>> getData("sensors");
            final long start = event.getData("startTime");
            final long end = event.getData("endTime");
            final boolean subsample = event.getData("subsample");
            showMap(sensors, start, end, subsample);

        } else if (type.equals(DataEvents.DataReceived)) {
            LOG.finest("DataReceived");
            JsArray<Timeseries> data = event.getData("data");
            onDataReceived(data);

        } else if (type.equals(VizPanelEvents.ShowNetwork)) {
            LOG.warning("ShowNetwork not implemented");

        } else {
            LOG.severe("Unexpected event: " + event);
        }
    }
    /**
     * Adds data to the visualization.
     * 
     * @param data
     *            Timeseries to display.
     */
    private void onDataReceived(JsArray<Timeseries> data) {
        LOG.fine("AddData from visPanel is called");
        if (null == this.data) {
            this.data = data;

        } else {
            for (int i = 0; i < data.length(); i++) {
                Timeseries toAppend = data.get(i);
                boolean appended = false;
                for (int j = 0; j < this.data.length(); j++) {
                    Timeseries original = this.data.get(j);
                    if (toAppend.getLabel().equals(original.getLabel())
                            && toAppend.getIdd() == original.getIdd()) {
                        LOG.fine("Append data to " + original.getLabel());
                        original.append(toAppend);
                        appended = true;
                        break;
                    }
                }
                if (!appended) {
                    LOG.fine("Add new timeseries to the graph data " + toAppend.getLabel());
                    this.data.push(toAppend);
                }
            }
        }

        panel.onNewData(this.data);
    }

    /**
     * Dispatches request for refreshing the sensor data.
     */
    private void refreshData() {
        LOG.fine("Refresh data...");

        // don't refresh when the user has left the visualization section of the app
        if (!History.getToken().equals(NavPanel.VISUALIZATION)) {
            LOG.fine("Did not refresh because the current history token is: " + History.getToken());
            return;
        }

        if (null != sensors) {

            for (SensorModel sensor : sensors) {

                // find the latest data point for which we have data and refresh from this point
                long refreshStart = start;
                for (int i = 0; i < data.length(); i++) {
                    Timeseries ts = data.get(i);
                    if (ts.getId() == sensor.getId()) {
                        LOG.finest("Found time series for sensor " + sensor.getDisplayName());
                        LOG.fine("time series end: "
                                + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(
                                        new Date(ts.getEnd())));
                        refreshStart = ts.getEnd() > refreshStart ? ts.getEnd() : refreshStart;

                    }
                }

                DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
                LOG.fine("Refresh start time: " + dtf.format(new Date(refreshStart)));

                // submit request event
                DataRequestEvent refreshRequest = new DataRequestEvent(refreshStart, end, sensors,
                        true, false);
                refreshRequest.setSource(this);
                Dispatcher.forwardEvent(refreshRequest);
            }

        } else {
            LOG.warning("Cannot refresh data: list of sensors is null");
        }
    }

    /**
     * Registers listeners to keep track if the visibility of this panel. Used to stop the auto
     * refresh requests when the panel is hidden.
     */
    private void registerHideListener() {

        item.addListener(Events.Hide, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                LOG.finest("Panel " + panel.getHeading() + " hidden");
                if (isAutoRefresh) {
                    refreshTimer.cancel();
                }
            }
        });
        item.addListener(Events.Close, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                LOG.finest("Panel " + panel.getHeading() + " closed");
                if (isAutoRefresh) {
                    refreshTimer.cancel();
                }
            }
        });
        item.addListener(Events.Remove, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                LOG.finest("Panel " + panel.getHeading() + " removed");
                if (isAutoRefresh) {
                    refreshTimer.cancel();
                }
            }
        });
        item.addListener(Events.Show, new Listener<ComponentEvent>() {

            @Override
            public void handleEvent(ComponentEvent be) {
                LOG.finest("Panel " + panel.getHeading() + " shown");
                if (isAutoRefresh) {
                    refreshData();
                    refreshTimer.scheduleRepeating(REFRESH_PERIOD);
                }
            }
        });
    }

    /**
     * Stores sensors and time range, and dispatches event to request sensor data.
     * 
     * @param sensors
     *            List with SensorModels to visualize.
     * @param start
     *            Start time of the period to display.
     * @param end
     *            End time of the period to display.
     */
    private void requestData() {
        LOG.fine("Request data...");
        DataRequestEvent dataRequest = new DataRequestEvent(start, end, sensors, subsample, true);
        dataRequest.setSource(this);
        Dispatcher.forwardEvent(dataRequest);
    }

    private void showFeedback(VizPanel panel, String title) {

        // add feedback tab item
        final TabItem item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "setting_tools.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        this.panel = panel;
        item.add(panel);

        TabPanel tabPanel = Registry.get(Constants.REG_VIZPANEL);
        if (tabPanel == null) {
            LOG.severe("Cannot find main visualization panel!");
            return;
        }
        tabPanel.add(item);
        tabPanel.setSelection(item);

        addListeners();
        requestData();
    }

    private void showMap(List<SensorModel> sensors, long start, long end, boolean subsample) {

        this.sensors = sensors;
        this.start = start;
        this.end = end;
        this.subsample = subsample;

        // add map tab item
        String title = createChartTitle(sensors);
        item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "map.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        panel = new MapPanel(sensors, start, end, subsample, title);
        item.add(panel);

        TabPanel tabPanel = Registry.get(Constants.REG_VIZPANEL);
        if (tabPanel == null) {
            LOG.severe("Cannot find main visualization panel!");
            return;
        }
        tabPanel.add(item);
        tabPanel.setSelection(item);

        addListeners();
        requestData();
    }

    private void showTable(List<SensorModel> sensors, long start, long end) {

        this.sensors = sensors;
        this.start = start;
        this.end = end;
        this.subsample = false;

        // add table tab item
        item = new TabItem(createChartTitle(sensors));
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "table.png"));
        item.setClosable(true);
        item.setScrollMode(Scroll.AUTO);
        item.setLayout(new FitLayout());

        TabPanel tabPanel = Registry.get(Constants.REG_VIZPANEL);
        if (tabPanel == null) {
            LOG.severe("Cannot find main visualization panel!");
            return;
        }
        tabPanel.add(item);
        tabPanel.setSelection(item);

        // add sensor data grid
        panel = new SensorDataGrid(sensors, start, end);
        item.add(panel);
        item.layout();

        addListeners();
        // requestData();
    }

    private void showTimeLine(List<SensorModel> sensors, long start, long end, boolean subsample) {

        this.sensors = sensors;
        this.start = start;
        this.end = end;
        this.subsample = subsample;

        // add line chart tab item
        String title = createChartTitle(sensors);
        item = new TabItem(title);
        item.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "chart.png"));
        item.setLayout(new FitLayout());
        item.setClosable(true);

        panel = new TimeLinePanel(sensors, start, end, subsample, title);
        item.add(panel);

        TabPanel tabPanel = Registry.get(Constants.REG_VIZPANEL);
        if (tabPanel == null) {
            LOG.severe("Cannot find main visualization panel!");
            return;
        }
        tabPanel.add(item);
        tabPanel.setSelection(item);

        addListeners();
        requestData();
    }
}
