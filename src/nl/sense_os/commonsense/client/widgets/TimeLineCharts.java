package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.FloatValueModel;
import nl.sense_os.commonsense.dto.JsonValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;

public class TimeLineCharts extends VisualizationTab {

    private static final String TAG = "TimeLineCharts";
    // private double nrOfCharts;
    private TimeLineChart floatChart;
    private final Map<String, TimeLineChart> jsonCharts;
    private int nrOfCharts;

    public TimeLineCharts() {
        super();

        this.jsonCharts = new HashMap<String, TimeLineChart>();
        this.nrOfCharts = 0;
    }

    public TimeLineCharts(List<TaggedDataModel> dataList) {
        this();

        // add data to charts
        for (final TaggedDataModel data : dataList) {
            addData(data);
        }
    }

    public TimeLineCharts(TaggedDataModel data) {
        this();

        // add data to charts
        addData(data);
    }

    /**
     * Adds a chart to the charts that are already displayed, resizing them if necessary.
     * 
     * @param chart
     */
    private void addChart(TimeLineChart chart) {

        if (0 == this.nrOfCharts) {
            // remove empty text message
            Component emptyText = this.getItemByItemId("empty_text");
            if (null != emptyText) {
                this.remove(emptyText);
            }

            chart.setId("chart_" + this.nrOfCharts);
            this.add(chart, new RowData(-1, 0.9));
        } else if (1 == this.nrOfCharts) {
            // re-add first chart with new size
            TimeLineChart firstChart = (TimeLineChart) this.getItemByItemId("chart_" + 0);
            this.remove(firstChart);

            // re-layout the first chart with new size
            this.add(firstChart, new RowData(-1, 0.4));
            firstChart.layout(true);

            this.add(chart, new RowData(-1, 0.4));
        } else {
            this.add(chart, new RowData(-1, 0.4));
        }
        this.nrOfCharts++;
        this.doLayout();
    }

    /**
     * Convenience method for adding data from more than one tag at a time.
     * 
     * @see #addData(TaggedDataModel)
     * @param datas
     *            list of tagged data to display
     */
    @Override
    public void addData(List<TaggedDataModel> datas) {

        for (final TaggedDataModel data : datas) {
            addData(data);
        }
    }

    /**
     * Adds extra tagged sensor value data to the already visible charts.
     * 
     * @param data
     *            the sensor values to display.
     */
    @Override
    public void addData(TaggedDataModel data) {

        // see of there are any data points before making chart
        final SensorValueModel[] values = data.getData();
        if (values.length > 0) {

            // different charts for different data types
            switch (values[0].getType()) {
            case SensorValueModel.FLOAT:
                addFloatChart(data);
                break;
            case SensorValueModel.JSON:
                addJsonCharts(data);
                break;
            default:
                Log.w(TAG, "Unexpected data type");
            }
            // TODO: boolean and String handling in TimeLineCharts
        }
    }

    /**
     * Adds a chart for simple float sensor values.
     * 
     * @param data
     *            the sensor values to display
     */
    private void addFloatChart(TaggedDataModel data) {
        if (null == this.floatChart) {
            this.floatChart = new TimeLineChart(data, null);
            addChart(this.floatChart);
        } else {
            this.floatChart.addData(data);
        }
    }

    /**
     * Adds one or more charts from the fields of JSON sensor values.
     * 
     * @param data
     *            the sensor values to display
     */
    private void addJsonCharts(TaggedDataModel data) {
        // get numerical fields on the json object
        final Map<String, TaggedDataModel> numFields = jsonToFloats(data);

        for (final Map.Entry<String, TaggedDataModel> field : numFields.entrySet()) {
            final String chartName = field.getKey();
            final TaggedDataModel taggedData = field.getValue();

            TimeLineChart chart = this.jsonCharts.get(chartName);
            if (null == chart) {
                chart = new TimeLineChart(taggedData, chartName);
                addChart(chart);
                this.jsonCharts.put(chartName, chart);
            } else {
                chart.addData(taggedData);
            }
        }
    }

    /**
     * Retrieves fields with numerical values from JSON sensor values by trying to parse them as
     * doubles.
     * 
     * @param data
     *            the tagged json data to convert to numerical data
     * @return a Map with the retrieved pairs of field names and numerical data from the input
     */
    private Map<String, TaggedDataModel> jsonToFloats(TaggedDataModel data) {

        final TagModel tag = data.getTag();
        final SensorValueModel[] values = data.getData();

        // slowly take each individual JSON value apart and put the field contents in a separate
        // list
        final Map<String, ArrayList<SensorValueModel>> sortedValues = sortJsonFields(values);

        // convert the ArrayLists into TaggedDataModel types
        final Map<String, TaggedDataModel> sortedData = new HashMap<String, TaggedDataModel>();
        for (final Map.Entry<String, ArrayList<SensorValueModel>> fieldData : sortedValues
                .entrySet()) {
            final SensorValueModel[] fieldValues = fieldData.getValue().toArray(
                    new SensorValueModel[0]);
            sortedData.put(fieldData.getKey(), new TaggedDataModel(tag, fieldValues));
        }

        return sortedData;
    }

    private Map<String, ArrayList<SensorValueModel>> sortJsonFields(SensorValueModel[] unsorted) {
        final Map<String, ArrayList<SensorValueModel>> sortedValues = new HashMap<String, ArrayList<SensorValueModel>>();
        for (final SensorValueModel genericValue : unsorted) {
            final JsonValueModel value = (JsonValueModel) genericValue;
            final Map<String, Object> fields = value.getFields();

            for (final Map.Entry<String, Object> field : fields.entrySet()) {
                final String fieldName = field.getKey();
                final Object fieldValue = field.getValue();
                if (fieldValue instanceof Double) {
                    // simple double field!
                    ArrayList<SensorValueModel> list = sortedValues.get(fieldName);
                    final FloatValueModel floatValue = new FloatValueModel(value.getTimestamp(),
                            (Double) fieldValue);
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
                        Log.d(TAG, "field " + fieldName + " is not parsable! Value: " + fieldValue);
                    }
                } else {
                    // not a valid field
                    Log.d(TAG, "field " + field.getKey() + " is not valid! Value: " + fieldValue);
                }
            }
        }
        return sortedValues;
    }

    @Override
    public void setWaitingText(boolean visible) {
        super.setWaitingText(visible);

        if ((visible = false) && (this.nrOfCharts == 0)) {
            LayoutContainer c = new LayoutContainer(new CenterLayout());
            c.add(new Text("Waiting for data..."));
            c.setId("empty_text");
            this.add(c, new RowData(1, 1));
        }
    }
}