package nl.sense_os.commonsense.client.visualization.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.sense_os.commonsense.client.data.DataEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.sensorvalues.BooleanValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

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
    private TimeLineChart floatChart;
    private TimeLineChart boolChart;
    private final Map<String, TimeLineChart> jsonCharts;
    private int nrOfCharts;
    private final Map<SensorModel, SensorValueModel[]> data;

    public TimeLinePanel() {
        super();

        // set up layout
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new RowLayout(Orientation.VERTICAL));
        setScrollMode(Scroll.AUTOY);

        initToolBar();

        this.data = new HashMap<SensorModel, SensorValueModel[]>();
        this.jsonCharts = new HashMap<String, TimeLineChart>();
        this.nrOfCharts = 0;
    }

    public TimeLinePanel(List<SensorModel> sensors, long startTime, long endTime) {
        this();

        requestData(sensors, startTime, endTime);
    }

    /**
     * Adds a chart for simple boolean sensor values.
     * 
     * @param data
     *            the sensor values to display
     */
    private void addBoolChart(SensorModel sensor, SensorValueModel[] values) {

        SensorValueModel value;
        double floatValue;
        for (int i = 0; i < values.length; i++) {
            value = values[i];
            floatValue = ((BooleanValueModel) value).getValue() ? 1 : 0;
            Log.d(TAG, "floatValue: " + floatValue);
            values[i] = new FloatValueModel(value.getTimestamp(), floatValue);
        }

        addFloatChart(sensor, values);

        // if (null == this.boolChart) {
        // this.boolChart = new TimeLineChart(sensor, values, null);
        // addChart(this.boolChart);
        // } else {
        // this.boolChart.addData(sensor, values);
        // }
    }

    /**
     * Adds a chart to the charts that are already displayed, resizing them if necessary.
     * 
     * @param chart
     */
    private void addChart(TimeLineChart chart) {

        // Log.d(TAG, "addChart");
        TimeLineChart firstChart = null;

        if (0 == this.nrOfCharts) {
            // remove empty text message
            Component emptyText = this.getItemByItemId("empty_text");
            if (null != emptyText) {
                this.remove(emptyText);
            }

            chart.setId("chart_" + this.nrOfCharts);
            this.add(chart, new RowData(-1, 1, new Margins(5)));

        } else if (1 == this.nrOfCharts) {
            // re-add first chart with new size
            firstChart = (TimeLineChart) this.getItemByItemId("chart_" + 0);
            this.remove(firstChart);
            this.add(firstChart, new RowData(-1, 0.8, new Margins(5)));

            this.add(chart, new RowData(-1, 0.8, new Margins(5)));

        } else {
            this.add(chart, new RowData(-1, 0.8, new Margins(5)));

        }
        this.nrOfCharts++;

        // do layout to show added charts
        try {
            this.layout();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != firstChart) {
            firstChart.redraw();
        }
    }

    /**
     * Convenience method for adding data from more than one tag at a time.
     * 
     * @see #addData(SensorModel, SensorValueModel[])
     * @param data
     *            Map with of sensors and sensor values to display
     */
    @Override
    public void addData(Map<SensorModel, SensorValueModel[]> data) {

        for (Entry<SensorModel, SensorValueModel[]> entry : data.entrySet()) {
            addData(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds extra tagged sensor value data to the already visible charts.
     * 
     * @param data
     *            the sensor values to display.
     */
    @Override
    public void addData(SensorModel sensor, SensorValueModel[] values) {

        // Log.d(TAG, "addData... (" + data.getData().length + " points)");
        SensorValueModel[] cachedValues = data.get(sensor);
        if (null != cachedValues) {
            appendValues(sensor, values, cachedValues);
            return;
        } else {
            data.put(sensor, values);
        }

        // see of there are any data points before making chart
        if (values.length > 0) {

            // different charts for different data types
            String dataType = sensor.get(SensorModel.DATA_TYPE);
            if (dataType.equals("float")) {
                addFloatChart(sensor, values);
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
     * Adds a chart for simple float sensor values.
     * 
     * @param data
     *            the sensor values to display
     */
    private void addFloatChart(SensorModel sensor, SensorValueModel[] values) {
        if (null == this.floatChart) {
            this.floatChart = new TimeLineChart(sensor, values, null);
            addChart(this.floatChart);
        } else {
            this.floatChart.addData(sensor, values);
        }
    }

    /**
     * Adds one or more charts from the fields of JSON sensor values.
     * 
     * @param data
     *            the sensor values to display
     */
    private void addJsonCharts(SensorModel sensor, SensorValueModel[] values) {
        // get numerical fields on the json object
        final Map<String, SensorValueModel[]> numFields = jsonToFloats(sensor, values);

        final String sensorName = sensor.get("text");

        for (final Map.Entry<String, SensorValueModel[]> field : numFields.entrySet()) {
            final String chartName = field.getKey();

            // Log.d(TAG, "addJsonCharts... field: " + chartName);

            final SensorValueModel[] fieldData = field.getValue();

            // add json data into float chart
            SensorModel sensorCopy = new SensorModel(sensor.getProperties());
            sensorCopy.set("text", sensorName + " " + chartName);

            addFloatChart(sensorCopy, fieldData);

            // TimeLineChart chart = this.jsonCharts.get(chartName);
            // if (null == chart) {
            // chart = new TimeLineChart(sensor, fieldData, chartName);
            // addChart(chart);
            // this.jsonCharts.put(chartName, chart);
            // } else {
            // chart.addData(sensor, fieldData);
            // }
        }
    }

    private void appendValues(SensorModel sensor, SensorValueModel[] values,
            SensorValueModel[] cachedValues) {
        Log.d(TAG, "append " + values.length + " values to " + sensor.get("text"));
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
     * @param data
     *            the tagged json data to convert to numerical data
     * @return a Map with the retrieved pairs of field names and numerical data from the input
     */
    private Map<String, SensorValueModel[]> jsonToFloats(SensorModel sensor,
            SensorValueModel[] values) {

        // take each individual JSON value apart and put the field contents in a separate list
        final Map<String, ArrayList<SensorValueModel>> sortedValues = sortJsonFields(values);

        // convert the ArrayLists into TaggedDataModel types
        final Map<String, SensorValueModel[]> sortedData = new HashMap<String, SensorValueModel[]>();
        for (final Entry<String, ArrayList<SensorValueModel>> fieldData : sortedValues.entrySet()) {
            final SensorValueModel[] fieldValues = fieldData.getValue().toArray(
                    new SensorValueModel[0]);
            sortedData.put(fieldData.getKey(), fieldValues);
        }

        return sortedData;
    }

    private void refreshData() {
        final List<SensorModel> sensors = new ArrayList<SensorModel>(data.keySet());

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

    private Map<String, ArrayList<SensorValueModel>> sortJsonFields(SensorValueModel[] unsorted) {

        // for every sensor value in the list
        final Map<String, ArrayList<SensorValueModel>> sortedValues = new HashMap<String, ArrayList<SensorValueModel>>();
        for (final SensorValueModel genericValue : unsorted) {
            final JsonValueModel value = (JsonValueModel) genericValue;
            final Map<String, Object> fields = value.getFields();

            // for every JSON field in the sensor value
            for (final Map.Entry<String, Object> field : fields.entrySet()) {

                // try to parse the field properties to doubles
                final String fieldName = field.getKey();
                final Object fieldValue = field.getValue();
                if (fieldValue instanceof Integer) {
                    // simple int field!
                    final int parsed = (Integer) fieldValue;
                    ArrayList<SensorValueModel> list = sortedValues.get(fieldName);
                    final FloatValueModel floatValue = new FloatValueModel(value.getTimestamp(),
                            parsed);
                    if (null != list) {
                        list.add(floatValue);
                    } else {
                        list = new ArrayList<SensorValueModel>();
                        list.add(floatValue);
                    }
                    sortedValues.put(fieldName, list);
                } else if (fieldValue instanceof Double) {
                    // simple double field!
                    final double parsed = (Double) fieldValue;
                    ArrayList<SensorValueModel> list = sortedValues.get(fieldName);
                    final FloatValueModel floatValue = new FloatValueModel(value.getTimestamp(),
                            parsed);
                    if (null != list) {
                        list.add(floatValue);
                    } else {
                        list = new ArrayList<SensorValueModel>();
                        list.add(floatValue);
                    }
                    sortedValues.put(fieldName, list);
                } else if (fieldValue instanceof String) {
                    // Strings might be parsed as doubles
                    try {
                        final double parsed = Double.parseDouble((String) fieldValue);
                        ArrayList<SensorValueModel> list = sortedValues.get(fieldName);
                        final FloatValueModel floatValue = new FloatValueModel(
                                value.getTimestamp(), parsed);
                        if (null != list) {
                            list.add(floatValue);
                        } else {
                            list = new ArrayList<SensorValueModel>();
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
}