package nl.sense_os.commonsense.client.visualization.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.BoolDataPoint;
import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.FloatDataPoint;
import nl.sense_os.commonsense.client.json.overlays.JsonDataPoint;
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
import com.google.gwt.core.client.JavaScriptObject;

public class TimeLinePanel extends ContentPanel implements VizPanel {

    private static final String TAG = "TimeLinePanel";
    private TimeLineChart chart;
    private final List<SensorModel> sensors;

    public TimeLinePanel() {
        super();

        // set up layout
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new RowLayout(Orientation.VERTICAL));
        setScrollMode(Scroll.AUTOY);

        initToolBar();

        this.sensors = new ArrayList<SensorModel>();
    }

    public TimeLinePanel(List<SensorModel> sensors, long startTime, long endTime) {
        this();

        requestData(sensors, startTime, endTime);
    }

    /**
     * Adds a chart for simple boolean sensor values.
     * 
     * @param sensor
     *            Sensor that the data belongs to.
     * @param values
     *            DataPoint[] to plot on the chart, consisting of AbstractBoolDataPoint.
     */
    private void addBoolChart(SensorModel sensor, DataPoint[] values) {

        DataPoint value;
        double floatValue;
        for (int i = 0; i < values.length; i++) {
            value = values[i];
            floatValue = ((BoolDataPoint) value).getBoolValue() ? 1 : 0;
            // TODO
            // values[i] = new FloatDataPoint(floatValue, value.getTimestamp());
        }

        Log.w(TAG, "Skipping boolean sensor " + sensor.get("text"));
        // visualize(sensor, values);
    }

    @Override
    public void addData(JavaScriptObject data) {
        if (null == this.chart) {
            this.chart = new TimeLineChart(data);
            showChart(this.chart);
        } else {
            this.chart.addData(data);
        }
    }

    /**
     * Convenience method for adding data from more than one sensor at a time.
     * 
     * @see #addData(SensorModel, DataPoint[])
     * @param data
     *            Map with of sensors and sensor values to display
     */
    @Override
    public void addData(Map<SensorModel, DataPoint[]> data) {

        for (Entry<SensorModel, DataPoint[]> entry : data.entrySet()) {
            addData(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds data to the chart.
     * 
     * @param sensor
     *            Sensor that the data belongs to.
     * @param values
     *            DataPoint[] to plot on the chart.
     */
    @Override
    public void addData(SensorModel sensor, DataPoint[] values) {

        // Log.d(TAG, "addData... (" + data.getData().length + " points)");
        boolean isAlreadyVisualized = false;
        for (SensorModel cachedSensor : sensors) {
            if (cachedSensor.get(SensorModel.ID).equals(sensor.get(SensorModel.ID))) {
                isAlreadyVisualized = true;
                break;
            }
        }
        if (false == isAlreadyVisualized) {
            this.sensors.add(sensor);
        }

        // see of there are any data points before making chart
        if (values.length > 0) {

            // different charts for different data types
            String dataType = sensor.get(SensorModel.DATA_TYPE);
            if (dataType.equals("float")) {
                visualize(sensor, values);
            } else if (dataType.equals("json")) {
                addJsonCharts(sensor, values);
            } else if (dataType.equals("bool")) {
                addBoolChart(sensor, values);
            } else {
                Log.w(TAG, "Ignoring data type: " + dataType);
            }

        } else {
            Log.w(TAG, "No sensor values to add");
        }
    }

    /**
     * Adds one or more charts from the fields of JSON sensor values.
     * 
     * @param sensor
     *            Sensor that the data belongs to.
     * @param values
     *            DataPoint[] to plot on the chart, consisting of AbstractJsonDataPoint.
     */
    private void addJsonCharts(SensorModel sensor, DataPoint[] values) {
        // get numerical fields on the JSON object
        final Map<String, DataPoint[]> numFields = jsonToFloats(sensor, values);

        final String sensorName = sensor.get("text");

        for (final Map.Entry<String, DataPoint[]> field : numFields.entrySet()) {
            final String chartName = field.getKey();

            // Log.d(TAG, "addJsonCharts... field: " + chartName);

            final DataPoint[] fieldData = field.getValue();

            // add JSON data into float chart
            SensorModel sensorCopy = new SensorModel(sensor.getProperties());
            sensorCopy.set("text", sensorName + " " + chartName);

            visualize(sensorCopy, fieldData);
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

    /**
     * Retrieves fields with numerical values from JSON sensor values by trying to parse them as
     * doubles.
     * 
     * @param sensors
     * @param values
     * @return a Map with the retrieved pairs of field names and numerical data from the input
     */
    private Map<String, DataPoint[]> jsonToFloats(SensorModel sensor, DataPoint[] values) {

        // take each individual JSON value apart and put the field contents in a separate list
        final Map<String, ArrayList<DataPoint>> sortedValues = sortJsonFields(values);

        // convert the ArrayLists into TaggedDataModel types
        final Map<String, DataPoint[]> sortedData = new HashMap<String, DataPoint[]>();
        for (final Entry<String, ArrayList<DataPoint>> fieldData : sortedValues.entrySet()) {
            final DataPoint[] fieldValues = fieldData.getValue().toArray(new DataPoint[0]);
            sortedData.put(fieldData.getKey(), fieldValues);
        }

        return sortedData;
    }

    private void refreshData() {
        AppEvent refreshRequest = new AppEvent(DataEvents.RefreshRequest);
        refreshRequest.setData("sensors", sensors);
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

    private Map<String, ArrayList<DataPoint>> sortJsonFields(DataPoint[] unsorted) {

        // for every sensor value in the list
        final Map<String, ArrayList<DataPoint>> sortedValues = new HashMap<String, ArrayList<DataPoint>>();
        for (final DataPoint genericValue : unsorted) {
            final JsonDataPoint value = (JsonDataPoint) genericValue;
            final Map<String, DataPoint> fields = value.getFields();

            // for every JSON field in the sensor value
            for (final Map.Entry<String, DataPoint> field : fields.entrySet()) {

                // try to parse the field properties to doubles
                final String fieldName = field.getKey();
                final DataPoint fieldValue = field.getValue();
                if (fieldValue instanceof FloatDataPoint) {
                    // simple int field!
                    final FloatDataPoint parsed = (FloatDataPoint) fieldValue;
                    ArrayList<DataPoint> list = sortedValues.get(fieldName);
                    if (null != list) {
                        list.add(parsed);
                    } else {
                        list = new ArrayList<DataPoint>();
                        list.add(parsed);
                    }
                    sortedValues.put(fieldName, list);

                } else if (fieldValue instanceof BoolDataPoint) {
                    // TODO
                    Log.w(TAG, "Skipping boolean field " + field.getKey());

                } else {
                    // TODO
                    Log.w(TAG, "Skipping String field " + field.getKey());
                }
            }
        }
        return sortedValues;
    }

    /**
     * Displays the data on the chart
     * 
     * @param sensor
     *            Sensor that the data belongs to.
     * @param values
     *            SensorValueModel[] to plot on the chart, consisting of FloatValueModel.
     */
    private void visualize(SensorModel sensor, DataPoint[] values) {
        if (null == this.chart) {
            this.chart = new TimeLineChart(sensor, values);
            showChart(this.chart);
        } else {
            this.chart.addData(sensor, values);
        }
    }
}