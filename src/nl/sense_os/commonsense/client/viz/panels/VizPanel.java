package nl.sense_os.commonsense.client.viz.panels;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.main.components.NavPanel;
import nl.sense_os.commonsense.client.viz.data.DataRequestEvent;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public abstract class VizPanel extends ContentPanel {

    /**
     * Timer to periodically refresh the data for the visualization.
     */
    private class RefreshTimer extends Timer {

        @Override
        public void run() {
            refreshData();
        }
    }

    private static final Logger LOG = Logger.getLogger(VizPanel.class.getName());
    private static final int REFRESH_PERIOD = 1000 * 10;
    private RefreshTimer refreshTimer;
    private boolean isAutoRefresh;

    protected List<SensorModel> sensors;
    protected JsArray<Timeseries> data;
    protected long start;
    protected long end;

    /**
     * Creates new VizPanel instance.
     */
    protected VizPanel() {
        LOG.setLevel(Level.ALL);
        addToolButtons();
    }
    
    public void onNewData() {
    	
    }

    /**
     * Adds data to the visualization.
     * 
     * @param data
     *            Timeseries to display.
     */
    public void addData(JsArray<Timeseries> data) {
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

        onNewData(this.data);
    }

    /**
     * Adds tool buttons to the panel's heading.
     */
    private void addToolButtons() {

        // regular refresh button
        final ToolButton refresh = new ToolButton("x-tool-refresh",
                new SelectionListener<IconButtonEvent>() {

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        refreshData();
                    }
                });
        refresh.setToolTip("refresh");

        // auto-refresh button
        final ToolButton autoRefresh = new ToolButton("x-tool-right");
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
        autoRefresh.setToolTip("start auto-refresh");

        // add buttons to the panel's header
        Header header = getHeader();
        header.addTool(autoRefresh);
        header.addTool(refresh);
    }

    /**
     * @return The end time of the displayed time range.
     */
    public long getEnd() {
        return end;
    }

    /**
     * @return The sensors that are visualized.
     */
    public List<SensorModel> getSensors() {
        return sensors;
    }

    /**
     * @return The start time of the displayed time range.
     */
    public long getStart() {
        return start;
    }

    @Override
    public void hide() {

        if (null != refreshTimer) {
            refreshTimer.cancel();
            isAutoRefresh = false;
            refreshTimer = null;
        }

        Widget parent = getParent();
        if (parent instanceof TabItem) {
            // remove tab item from tab panel
            parent.removeFromParent();
        } else {
            removeFromParent();
        }
        super.hide();
    }

    /**
     * Called when the data was changed and the panel should update accordingly.
     * 
     * @param Array
     *            with the new data to visualize. The data is also stored in the panel's
     *            <code>data</code> field.
     */
    protected abstract void onNewData(JsArray<Timeseries> data);

    /**
     * Dispatches request for refreshing the sensor data.
     */
    protected void refreshData() {

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
                        true, false, this);
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
        Widget parent = VizPanel.this.getParent();
        if (parent instanceof TabItem) {
            ((TabItem) parent).addListener(Events.Hide, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    LOG.finest("Panel " + getHeading() + " hidden");
                    if (isAutoRefresh) {
                        refreshTimer.cancel();
                    }
                }

            });
            ((TabItem) parent).addListener(Events.Close, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    LOG.finest("Panel " + getHeading() + " closed");
                    if (isAutoRefresh) {
                        refreshTimer.cancel();
                    }
                }

            });
            ((TabItem) parent).addListener(Events.Remove, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    LOG.finest("Panel " + getHeading() + " removed");
                    if (isAutoRefresh) {
                        refreshTimer.cancel();
                    }
                }

            });
            ((TabItem) parent).addListener(Events.Show, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    LOG.finest("Panel " + getHeading() + " shown");
                    if (isAutoRefresh) {
                        refreshData();
                        refreshTimer.scheduleRepeating(REFRESH_PERIOD);
                    }
                }

            });
        } else {
            LOG.warning("Cannot register show/hide listeners: Parent is not a tabitem!");
        }
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
    protected void visualize(List<SensorModel> sensors, long start, long end, boolean subsample) {
    	LOG.fine("visualize...");
        this.sensors = sensors;
        this.start = start;
        this.end = end;

        DataRequestEvent dataRequest = new DataRequestEvent(start, end, sensors, subsample, true,
                this);
        Dispatcher.forwardEvent(dataRequest);
    }
}
