package nl.sense_os.commonsense.client.visualization.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.json.overlays.AbstractBoolDataPoint;
import nl.sense_os.commonsense.client.json.overlays.AbstractDataPoint;
import nl.sense_os.commonsense.client.json.overlays.AbstractJsonDataPoint;
import nl.sense_os.commonsense.client.json.overlays.FloatDataPoint;
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
     *            AbstractDataPoint[] to plot on the chart, consisting of AbstractBoolDataPoint.
     */
    private void addBoolChart(SensorModel sensor, AbstractDataPoint[] values) {

        AbstractDataPoint value;
        double floatValue;
        for (int i = 0; i < values.length; i++) {
            value = values[i];
            floatValue = ((AbstractBoolDataPoint) value).getBoolValue() ? 1 : 0;
            values[i] = new FloatDataPoint(floatValue, value.getTimestamp());
        }

        visualize(sensor, values);
    }

    /**
     * Convenience method for adding data from more than one sensor at a time.
     * 
     * @see #addData(SensorModel, AbstractDataPoint[])
     * @param data
     *            Map with of sensors and sensor values to display
     */
    @Override
    public void addData(Map<SensorModel, AbstractDataPoint[]> data) {

        for (Entry<SensorModel, AbstractDataPoint[]> entry : data.entrySet()) {
            addData(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds data to the chart.
     * 
     * @param sensor
     *            Sensor that the data belongs to.
     * @param values
     *            AbstractDataPoint[] to plot on the chart.
     */
    @Override
    public void addData(SensorModel sensor, AbstractDataPoint[] values) {

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
     *            AbstractDataPoint[] to plot on the chart, consisting of AbstractJsonDataPoint.
     */
    private void addJsonCharts(SensorModel sensor, AbstractDataPoint[] values) {
        // get numerical fields on the JSON object
        final Map<String, AbstractDataPoint[]> numFields = jsonToFloats(sensor, values);

        final String sensorName = sensor.get("text");

        for (final Map.Entry<String, AbstractDataPoint[]> field : numFields.entrySet()) {
            final String chartName = field.getKey();

            // Log.d(TAG, "addJsonCharts... field: " + chartName);

            final AbstractDataPoint[] fieldData = field.getValue();

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
    private Map<String, AbstractDataPoint[]> jsonToFloats(SensorModel sensor,
            AbstractDataPoint[] values) {

        // take each individual JSON value apart and put the field contents in a separate list
        final Map<String, ArrayList<AbstractDataPoint>> sortedValues = sortJsonFields(values);

        // convert the ArrayLists into TaggedDataModel types
        final Map<String, AbstractDataPoint[]> sortedData = new HashMap<String, AbstractDataPoint[]>();
        for (final Entry<String, ArrayList<AbstractDataPoint>> fieldData : sortedValues.entrySet()) {
            final AbstractDataPoint[] fieldValues = fieldData.getValue().toArray(
                    new AbstractDataPoint[0]);
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

    private Map<String, ArrayList<AbstractDataPoint>> sortJsonFields(AbstractDataPoint[] unsorted) {

        // for every sensor value in the list
        final Map<String, ArrayList<AbstractDataPoint>> sortedValues = new HashMap<String, ArrayList<AbstractDataPoint>>();
        for (final AbstractDataPoint genericValue : unsorted) {
            final AbstractJsonDataPoint value = (AbstractJsonDataPoint) genericValue;
            final Map<String, Object> fields = value.getFields();

            // for every JSON field in the sensor value
            for (final Map.Entry<String, Object> field : fields.entrySet()) {

                // try to parse the field properties to doubles
                final String fieldName = field.getKey();
                final Object fieldValue = field.getValue();
                if (fieldValue instanceof Integer) {
                    // simple int field!
                    final int parsed = (Integer) fieldValue;
                    ArrayList<AbstractDataPoint> list = sortedValues.get(fieldName);
                    final FloatDataPoint floatValue = new FloatDataPoint(parsed,
                            value.getTimestamp());
                    if (null != list) {
                        list.add(floatValue);
                    } else {
                        list = new ArrayList<AbstractDataPoint>();
                        list.add(floatValue);
                    }
                    sortedValues.put(fieldName, list);
                } else if (fieldValue instanceof Double) {
                    // simple double field!
                    final double parsed = (Double) fieldValue;
                    ArrayList<AbstractDataPoint> list = sortedValues.get(fieldName);
                    final FloatDataPoint floatValue = new FloatDataPoint(parsed,
                            value.getTimestamp());
                    if (null != list) {
                        list.add(floatValue);
                    } else {
                        list = new ArrayList<AbstractDataPoint>();
                        list.add(floatValue);
                    }
                    sortedValues.put(fieldName, list);
                } else if (fieldValue instanceof String) {
                    // Strings might be parsed as doubles
                    try {
                        final double parsed = Double.parseDouble((String) fieldValue);
                        ArrayList<AbstractDataPoint> list = sortedValues.get(fieldName);
                        final FloatDataPoint floatValue = new FloatDataPoint(parsed,
                                value.getTimestamp());
                        if (null != list) {
                            list.add(floatValue);
                        } else {
                            list = new ArrayList<AbstractDataPoint>();
                            list.add(floatValue);
                        }
                        sortedValues.put(fieldName, list);
                    } catch (final NumberFormatException e) {
                        // not a valid field
                        // Log.e(TAG, "field " + fieldName + " is not parsable! Value: " +
                        // fieldValue);
                    }

                } else {
                    // not a valid field
                    Log.e(TAG, "field " + field.getKey()
                            + " is not a String, int or double! Value: " + fieldValue);
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
    private void visualize(SensorModel sensor, AbstractDataPoint[] values) {
        if (null == this.chart) {
            this.chart = new TimeLineChart(sensor, values, null);
            showChart(this.chart);
        } else {
            this.chart.addData(sensor, values);
        }
    }
}