package nl.sense_os.commonsense.client.widgets;

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

    public TimeLineCharts() {
        super();
        
        this.jsonCharts = new HashMap<String, TimeLineChart>();        
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

        // TODO return of the variable chart size 

        // if (this.getItemCount() > 2) {
        // all charts are already the right size
        this.add(chart, new RowData(-1, 0.4));
        // } else {
        // List<Component> oldItems = this.getItems();
        //
        // double nrOfCharts = oldItems.size() + 1;
        // nrOfCharts = (nrOfCharts > 2.5) ? 2.5 : nrOfCharts;
        // this.
        //
        // // re-add old charts with new height
        // for (Component oldItem : oldItems) {
        // this.add(oldItem, new RowData(-1, 1 / nrOfCharts));
        // }
        //
        // // add new chart
        // this.add(chart, new RowData(-1, 1 / nrOfCharts));
        // }

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
                Log.d(TAG, "New chart for JSON field " + chartName);

                chart = new TimeLineChart(taggedData, chartName);
                addChart(chart);
                this.jsonCharts.put(chartName, chart);
            } else {
                Log.d(TAG, "Reuse existing chart for JSON field " + chartName);
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

        // slowly take each individual JSON value apart and put the field contents in a separate list
        final Map<String, ArrayList<SensorValueModel>> sortedValues = sortJsonFields(values);
        
        // convert the ArrayLists into TaggedDataModel types
        final Map<String, TaggedDataModel> sortedData = new HashMap<String, TaggedDataModel>();
        for (final Map.Entry<String, ArrayList<SensorValueModel>> fieldData : sortedValues.entrySet()) {
            final SensorValueModel[] fieldValues = fieldData.getValue().toArray(new SensorValueModel[0]);
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
                    final FloatValueModel floatValue = new FloatValueModel(value.getTimestamp(), (Double) fieldValue);
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
                        final FloatValueModel floatValue = new FloatValueModel(value.getTimestamp(), parsed);
                        if (null != list) {
                            list.add(floatValue);
                        } else {
                            list = new ArrayList<SensorValueModel>();
                            list.add(floatValue);
                        }
                        sortedValues.put(fieldName, list);
                    } catch (final NumberFormatException e) {
                        // not a valid field
                        Log.d(TAG, "field " + fieldName + " is not parsable! Value: "
                                + fieldValue);
                    }
                } else {
                    // not a valid field
                    Log.d(TAG, "field " + field.getKey() + " is not valid! Value: " + fieldValue);
                }
            }
        }
        return sortedValues;
    }
}