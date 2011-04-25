package nl.sense_os.commonsense.client.visualization;

import java.util.List;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;

public abstract class VizPanel extends ContentPanel {

    private static final String TAG = "VizPanel";
    private List<SensorModel> sensors;
    private long start;
    private long end;

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

        this.getHeader().addTool(refresh);
    }

    @Override
    public void hide() {
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
            Log.w(TAG, "Cannot refresh data: list of sensors is null");
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
