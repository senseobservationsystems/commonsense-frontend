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
        Map<String, Map<TagModel, SensorValueModel[]>> jsonCharts = floatsFromJsons();
        Map<TagModel, SensorValueModel[]> convertedStrings = floatsFromStrings();
        Map<TagModel, SensorValueModel[]> convertedBooleans = floatsFromBooleans();

        // determine how many charts will be drawn, to get the proper height of one chart
        double nrOfCharts = (this.floatData.size() > 0) ? 1 : 0;
        nrOfCharts += jsonCharts.size();
        nrOfCharts += (this.booleanData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.stringData.size() > 0) ? 1 : 0;
        nrOfCharts = (nrOfCharts > 3) ? 3 : nrOfCharts;

        // put charts in the layout
        this.setLayout(new RowLayout());
        this.setScrollMode(Scroll.AUTOY);
        if (nrOfCharts == 0) {
            this.setLayout(new CenterLayout());
            this.add(new Text("No numerical data to visualize..."));
        } else {
            if (this.floatData.size() > 0) {
                this.add(new TimeLineChart(this.floatData, null), new RowData(-1, 1 / nrOfCharts));
            }
            for (Map.Entry<String, Map<TagModel, SensorValueModel[]>> chart : jsonCharts.entrySet()) {
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

        Map<String, Map<TagModel, SensorValueModel[]>> sortedFields = new HashMap<String, Map<TagModel, SensorValueModel[]>>();
        for (Map.Entry<TagModel, SensorValueModel[]> entry : this.jsonData.entrySet()) {
            TagModel tag = entry.getKey();
            SensorValueModel[] values = entry.getValue();

            // get the fields for this data type
            JsonValueModel testValue = (JsonValueModel) values[0];
            Map<String, String> testFields = testValue.getFields();

            // iterate over the fields to see if there is anything visualizable
            List<String> validFields = new ArrayList<String>();
            for (Map.Entry<String, String> field : testFields.entrySet()) {
                try {
                    Double.parseDouble(field.getValue());
                    validFields.add(field.getKey());
                } catch (NumberFormatException e) {
                    // not a valid field
                    Log.d(TAG,
                            "field " + field.getKey() + " is not valid! Value: " + field.getValue());
                }
            }

            // extract the values of any float type fields and put them in sortedFields
            for (String field : validFields) {
                FloatValueModel[] extractedValues = new FloatValueModel[values.length];
                for (int i = 0; i < values.length; i++) {
                    JsonValueModel value = (JsonValueModel) values[i];

                    double val = Double.parseDouble(value.getFields().get(field));
                    extractedValues[i] = new FloatValueModel(value.getTimestamp(), field, val);
                }

                Map<TagModel, SensorValueModel[]> similarValues = sortedFields.get(field);
                if (null == similarValues) {
                    similarValues = new HashMap<TagModel, SensorValueModel[]>();
                }
                similarValues.put(tag, extractedValues);
                sortedFields.put(field, similarValues);
            }
        }

        for (Map.Entry<String, Map<TagModel, SensorValueModel[]>> chart : sortedFields.entrySet()) {
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

        for (TaggedDataModel dataEntry : data) {
            SensorValueModel[] values = dataEntry.getData();
            TagModel tag = dataEntry.getTag();

            if (values.length > 0) {

                SensorValueModel s = values[0];

                switch (s.getType()) {
                case SensorValueModel.BOOL:
                    booleanData.put(tag, values);
                    break;
                case SensorValueModel.FLOAT:
                    floatData.put(tag, values);
                    break;
                case SensorValueModel.JSON:
                    jsonData.put(tag, values);
                    break;
                case SensorValueModel.STRING:
                    stringData.put(tag, values);
                    break;
                default:
                    Log.w(TAG, "Unexpected data type");
                }
            }
        }
    }
}
