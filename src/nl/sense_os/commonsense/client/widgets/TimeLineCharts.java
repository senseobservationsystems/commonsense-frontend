package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

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

public class TimeLineCharts extends LayoutContainer {

    private static final String TAG = "TimeLineCharts";
    private Map<TagModel, SensorValueModel[]> booleanData;
    private Map<TagModel, SensorValueModel[]> floatData;
    private Map<TagModel, SensorValueModel[]> jsonData;
    private Map<TagModel, SensorValueModel[]> stringData;

    public TimeLineCharts(List<TaggedDataModel> data) {

        // separate the data types from the different sensors that will be charted
        separateDataTypes(data);

        // convert non-float data types to floats
        final Map<String, Map<TagModel, SensorValueModel[]>> jsonCharts = floatsFromJsons();
        final Map<TagModel, SensorValueModel[]> convertedStrings = floatsFromStrings();
        final Map<TagModel, SensorValueModel[]> convertedBooleans = floatsFromBooleans();

        // determine how many charts will be drawn, to get the proper height of one chart
        double nrOfCharts = (this.floatData.size() > 0) ? 1 : 0;
        nrOfCharts += jsonCharts.size();
        nrOfCharts += (this.booleanData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.stringData.size() > 0) ? 1 : 0;
        nrOfCharts = (nrOfCharts > 2) ? 2.5 : nrOfCharts;

        // put charts in the layout
        if (nrOfCharts == 0) {
            setLayout(new CenterLayout());
            this.add(new Text("No numerical data to visualize..."));
        } else {
            setLayout(new RowLayout());
            setScrollMode(Scroll.AUTOY);

            if (this.floatData.size() > 0) {
                this.add(new TimeLineChart(this.floatData, null), new RowData(-1, 1 / nrOfCharts));
            }
            for (final Map.Entry<String, Map<TagModel, SensorValueModel[]>> chart : jsonCharts
                    .entrySet()) {
                this.add(new TimeLineChart(chart.getValue(), chart.getKey()), new RowData(-1,
                        1 / nrOfCharts));
            }
            if (this.booleanData.size() > 0) {
                this.add(new TimeLineChart(convertedBooleans, "True/false"), new RowData(-1,
                        1 / nrOfCharts));
            }
            if (this.stringData.size() > 0) {
                this.add(new TimeLineChart(convertedStrings, "Strings"), new RowData(-1,
                        1 / nrOfCharts));
            }
        }
    }

    private Map<TagModel, SensorValueModel[]> floatsFromBooleans() {
        return null;
    }

    private Map<String, Map<TagModel, SensorValueModel[]>> floatsFromJsons() {

        final Map<String, Map<TagModel, SensorValueModel[]>> sortedFields = new HashMap<String, Map<TagModel, SensorValueModel[]>>();
        for (final Map.Entry<TagModel, SensorValueModel[]> entry : this.jsonData.entrySet()) {
            final TagModel tag = entry.getKey();
            final SensorValueModel[] values = entry.getValue();

            // get the fields for this data type
            final JsonValueModel testValue = (JsonValueModel) values[0];
            final Map<String, String> testFields = testValue.getFields();

            // iterate over the fields to see if there is anything visualizable
            final List<String> validFields = new ArrayList<String>();
            for (final Map.Entry<String, String> field : testFields.entrySet()) {
                try {
                    Double.parseDouble(field.getValue());
                    validFields.add(field.getKey());
                } catch (final NumberFormatException e) {
                    // not a valid field
                    Log.d(TAG,
                            "field " + field.getKey() + " is not valid! Value: " + field.getValue());
                }
            }

            // extract the values of any float type fields and put them in sortedFields
            for (final String field : validFields) {
                final FloatValueModel[] extractedValues = new FloatValueModel[values.length];
                for (int i = 0; i < values.length; i++) {
                    final JsonValueModel value = (JsonValueModel) values[i];

                    final double val = Double.parseDouble(value.getFields().get(field));
                    extractedValues[i] = new FloatValueModel(value.getTimestamp(), val);
                }

                Map<TagModel, SensorValueModel[]> similarValues = sortedFields.get(field);
                if (null == similarValues) {
                    similarValues = new HashMap<TagModel, SensorValueModel[]>();
                }
                similarValues.put(tag, extractedValues);
                sortedFields.put(field, similarValues);
            }
        }

        for (final Map.Entry<String, Map<TagModel, SensorValueModel[]>> chart : sortedFields
                .entrySet()) {
            Log.d(TAG, "Chart " + chart.getKey() + ": " + chart.getValue().size() + " tags");
        }

        return sortedFields;
    }

    private Map<TagModel, SensorValueModel[]> floatsFromStrings() {
        return null;
    }

    private void separateDataTypes(List<TaggedDataModel> data) {

        this.floatData = new HashMap<TagModel, SensorValueModel[]>();
        this.jsonData = new HashMap<TagModel, SensorValueModel[]>();
        this.booleanData = new HashMap<TagModel, SensorValueModel[]>();
        this.stringData = new HashMap<TagModel, SensorValueModel[]>();

        for (final TaggedDataModel dataEntry : data) {
            final SensorValueModel[] values = dataEntry.getData();
            final TagModel tag = dataEntry.getTag();

            if (values.length > 0) {

                final SensorValueModel s = values[0];

                switch (s.getType()) {
                case SensorValueModel.BOOL:
                    this.booleanData.put(tag, values);
                    break;
                case SensorValueModel.FLOAT:
                    this.floatData.put(tag, values);
                    break;
                case SensorValueModel.JSON:
                    this.jsonData.put(tag, values);
                    break;
                case SensorValueModel.STRING:
                    this.stringData.put(tag, values);
                    break;
                default:
                    Log.w(TAG, "Unexpected data type");
                }
            }
        }
    }
}