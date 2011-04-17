package nl.sense_os.commonsense.client.visualization.components;

import java.util.List;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.JsArray;

public class TimeLinePanel extends ContentPanel implements VizPanel {

    private static final String TAG = "TimeLinePanel";
    private TimeLineChart chart;
    private List<SensorModel> sensors;
    private long start;
    private long end;

    public TimeLinePanel() {
        super();

        // set up layout
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new RowLayout(Orientation.VERTICAL));
        setScrollMode(Scroll.AUTOY);

        initToolBar();
    }

    public TimeLinePanel(List<SensorModel> sensors, long startTime, long endTime) {
        this();
        this.sensors = sensors;
        this.start = startTime;
        this.end = endTime;
        requestData(sensors, startTime, endTime);
    }

    @Override
    public void addData(JsArray<Timeseries> data) {
        Log.d(TAG, "addData...");

        JsArray<Timeseries> floatData = JsArray.createArray().cast();
        for (int i = 0; i < data.length(); i++) {
            Timeseries ts = data.get(i);
            if (ts.getType().equalsIgnoreCase("number")) {
                Log.d(TAG, "found float data: " + ts.getLabel());
                floatData.push(ts);
            }
        }

        Log.d(TAG, "visualize!");
        if (null == this.chart) {
            this.chart = new TimeLineChart(floatData);
            showChart(this.chart);
        } else {
            this.chart.addData(floatData);
        }
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

    private void refreshData() {
        AppEvent refreshRequest = new AppEvent(DataEvents.RefreshRequest);
        refreshRequest.setData("sensors", this.sensors);
        refreshRequest.setData("start", this.start);
        refreshRequest.setData("vizPanel", this);
        Dispatcher.forwardEvent(refreshRequest);
    }

    private void requestData(List<SensorModel> sensors, long startTime, long endTime) {
        AppEvent dataRequest = new AppEvent(DataEvents.DataRequest);
        dataRequest.setData("sensors", sensors);
        dataRequest.setData("startTime", startTime);
        dataRequest.setData("endTime", endTime);
        dataRequest.setData("vizPanel", this);
        Dispatcher.forwardEvent(dataRequest);
    }

    /**
     * Adds a chart to the charts that are already displayed, resizing them if necessary.
     * 
     * @param chart
     */
    private void showChart(TimeLineChart chart) {
        // Log.d(TAG, "addChart");

        // remove empty text message
        Component emptyText = this.getItemByItemId("empty_text");
        if (null != emptyText) {
            this.remove(emptyText);
        }

        this.add(chart, new RowData(-1, 1, new Margins(5)));

        // do layout to show added chart
        try {
            this.layout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}