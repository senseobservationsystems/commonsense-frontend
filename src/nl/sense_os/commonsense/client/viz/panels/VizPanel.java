package nl.sense_os.commonsense.client.viz.panels;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.DataEvents;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

public abstract class VizPanel extends ContentPanel {

    private class RefreshTimer extends Timer {

        @Override
        public void run() {
            refreshData();
        }
    }

    private static final Logger logger = Logger.getLogger("VizPanel");
    private static final int REFRESH_PERIOD = 1000 * 10;
    private List<SensorModel> sensors;
    private long start;
    private long end;
    private RefreshTimer refreshTimer;
    protected boolean isAutoRefresh;

    /**
     * Creates new VizPanel instance.
     */
    protected VizPanel() {
        addToolButtons();
    }

    /**
     * Adds data to the visualization.
     * 
     * @param data
     *            Timeseries to display.
     */
    public abstract void addData(JsArray<Timeseries> data);

    /**
     * Adds tool buttons to the panel's heading.
     */
    private void addToolButtons() {

        final ToolButton refresh = new ToolButton("x-tool-refresh",
                new SelectionListener<IconButtonEvent>() {

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        refreshData();
                    }
                });
        refresh.setToolTip("refresh");
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

        Header header = this.getHeader();
        header.addTool(autoRefresh);
        header.addTool(refresh);
    }

    private void registerHideListener() {
        Widget parent = VizPanel.this.getParent();
        if (parent instanceof TabItem) {
            ((TabItem) parent).addListener(Events.Hide, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    // logger.fine( "hide");
                    if (isAutoRefresh) {
                        refreshTimer.cancel();
                    }
                }

            });
            ((TabItem) parent).addListener(Events.Close, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    // logger.fine( "close");
                    if (isAutoRefresh) {
                        refreshTimer.cancel();
                    }
                }

            });
            ((TabItem) parent).addListener(Events.Show, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    // logger.fine( "show");
                    if (isAutoRefresh) {
                        refreshData();
                        refreshTimer.scheduleRepeating(REFRESH_PERIOD);
                    }
                }

            });
        } else {
            logger.warning("Cannot register show/hide listeners: Parent is not a tabitem!");
        }
    }

    @Override
    public void hide() {

        if (null != this.refreshTimer) {
            this.refreshTimer.cancel();
            this.isAutoRefresh = false;
            this.refreshTimer = null;
        }

        Widget parent = this.getParent();
        if (parent instanceof TabItem) {
            // remove tab item from tab panel
            parent.removeFromParent();
        } else {
            this.removeFromParent();
        }
        super.hide();
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

    /**
     * Dispatches request for refreshing the sensor data.
     */
    protected void refreshData() {
        if (null != this.sensors) {
            AppEvent refreshRequest = new AppEvent(DataEvents.RefreshRequest);
            refreshRequest.setData("sensors", this.sensors);
            refreshRequest.setData("start", this.start);
            refreshRequest.setData("vizPanel", this);
            Dispatcher.forwardEvent(refreshRequest);
        } else {
            logger.warning("Cannot refresh data: list of sensors is null");
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
    protected void visualize(List<SensorModel> sensors, long start, long end) {
        this.sensors = sensors;
        this.start = start;
        this.end = end;

        AppEvent dataRequest = new AppEvent(DataEvents.DataRequest);
        dataRequest.setData("sensors", sensors);
        dataRequest.setData("startTime", start);
        dataRequest.setData("endTime", end);
        dataRequest.setData("vizPanel", this);
        Dispatcher.forwardEvent(dataRequest);
    }
}
