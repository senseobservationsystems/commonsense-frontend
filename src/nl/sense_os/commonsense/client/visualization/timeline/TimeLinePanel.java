package nl.sense_os.commonsense.client.visualization.timeline;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.visualization.VizPanel;
import nl.sense_os.commonsense.shared.SensorModel;

import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;

public class TimeLinePanel extends ContentPanel implements VizPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLinePanel";
    private Timeline timeline;
    private final DataTable dataTable;
    private List<SensorModel> sensors;
    private long start;
    @SuppressWarnings("unused")
    private long end;

    private TimeLinePanel() {
        super();

        // set up layout
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new RowLayout(Orientation.VERTICAL));
        setScrollMode(Scroll.AUTOY);

        initToolBar();
        this.dataTable = createDataTable();
    }

    /**
     * Creates new TimeLinePanel instance for the given list of sensors.
     * 
     * @param sensors
     *            List with SensorModels to display in a line chart.
     * @param start
     *            Start time of the period to display.
     * @param end
     *            End time of the period to display.
     */
    public TimeLinePanel(List<SensorModel> sensors, long start, long end) {
        this();
        this.sensors = sensors;
        this.start = start;
        this.end = end;
        requestData(sensors, start, end);
    }

    /**
     * Returns a table filled with data
     * 
     * @return data
     */
    private DataTable createDataTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");

        // data.addRow();
        // data.setValue(0, 0, new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 3));
        // data.setValue(0, 1, new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2));
        // data.setValue(0, 2, "foo");
        //
        // data.addRow();
        // data.setValue(0, 0, new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 1));
        // data.setValue(0, 1, new Date());
        // data.setValue(0, 2, "bar");

        return data;
    }

    /**
     * Dispatches request for refreshing the sensor data.
     */
    private void refreshData() {
        AppEvent refreshRequest = new AppEvent(DataEvents.RefreshRequest);
        refreshRequest.setData("sensors", this.sensors);
        refreshRequest.setData("start", this.start);
        refreshRequest.setData("vizPanel", this);
        Dispatcher.forwardEvent(refreshRequest);
    }

    /**
     * Requests sensor data.
     * 
     * @param sensors
     *            List with SensorModels to get data for.
     * @param start
     *            Start time of the period to get data for.
     * @param end
     *            End time of the period to get data for.
     */
    private void requestData(List<SensorModel> sensors, long start, long end) {
        AppEvent dataRequest = new AppEvent(DataEvents.DataRequest);
        dataRequest.setData("sensors", sensors);
        dataRequest.setData("startTime", start);
        dataRequest.setData("endTime", end);
        dataRequest.setData("vizPanel", this);
        Dispatcher.forwardEvent(dataRequest);
    }

    @Override
    public void addData(JsArray<Timeseries> data) {

        Timeseries ts;
        JsArray<DataPoint> values;
        DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            values = ts.getData();
            for (int j = 0, index = this.dataTable.getNumberOfRows(); j < values.length(); j++) {
                lastPoint = dataPoint;
                if (j == 0) {
                    dataPoint = values.get(j);
                } else {
                    dataPoint = nextPoint;
                }
                if (j < values.length() - 1) {
                    nextPoint = values.get(j + 1);
                } else {
                    nextPoint = null;
                }
                if (j > 0) {
                    if (false == (lastPoint != null && lastPoint.getRawValue().equals(
                            dataPoint.getRawValue()))) {
                        // value changed! new row...
                        this.dataTable.addRow();
                        index++;
                        this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                        this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                    } else {
                        // only the end time has to be changed
                    }
                } else {
                    // insert first data point
                    this.dataTable.addRow();
                    this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                    this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                }

                // set end time
                if (nextPoint != null) {
                    long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                            .getTimestamp().getTime() - 1000);
                    this.dataTable.setValue(index, 1, new Date(endDate));
                } else {
                    this.dataTable.setValue(index, 1, new Date());
                }
            }
        }

        if (dataTable.getNumberOfRows() > 0) {
            if (null == this.timeline) {
                Timeline.Options options = Timeline.Options.create();
                this.timeline = new Timeline(this.dataTable, options);
                showChart(this.timeline);
            } else {
                this.timeline.redraw();
            }
        } else {
            String msg = "No data to visualize! "
                    + "Please make sure that you selected a proper time range.";
            MessageBox.info(null, msg, new Listener<MessageBoxEvent>() {

                @Override
                public void handleEvent(MessageBoxEvent be) {
                    hidePanel();
                }
            });
        }
    }

    private void hidePanel() {
        Widget parent = this.getParent();
        if (parent instanceof TabItem) {
            // remove tab item from tab panel
            parent.removeFromParent();
        } else {
            this.removeFromParent();
        }
        this.hide();
    }

    private void initToolBar() {

        IconButton refresh = new IconButton("x-tbar-refresh",
                new SelectionListener<IconButtonEvent>() {

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        refreshData();
                    }
                });

        ToolBar toolbar = new ToolBar();
        toolbar.add(refresh);
        this.setTopComponent(toolbar);
    }

    /**
     * Adds a chart to the charts that are already displayed, resizing them if necessary.
     * 
     * @param chart
     */
    private void showChart(Timeline timeline) {
        // Log.d(TAG, "showChart");

        // remove empty text message
        Component emptyText = this.getItemByItemId("empty_text");
        if (null != emptyText) {
            this.remove(emptyText);
        }

        this.add(timeline, new RowData(-1, 1, new Margins(5)));

        // do layout to show added chart
        try {
            this.layout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
