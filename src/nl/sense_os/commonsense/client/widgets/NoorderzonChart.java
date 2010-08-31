package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.FloatValueModel;
import nl.sense_os.commonsense.dto.JsonValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;

public class NoorderzonChart extends LayoutContainer {

    private static final String TAG = "NoorderzonCharts";
    private Map<TagModel, SensorValueModel[]> xData;
    private Map<TagModel, SensorValueModel[]> yData;
    private Map<TagModel, SensorValueModel[]> zData;
    private Map<TagModel, SensorValueModel[]> tempData;
    private Map<TagModel, SensorValueModel[]> telxData;
    private Map<TagModel, SensorValueModel[]> telyData;
    private Map<TagModel, SensorValueModel[]> telzData;
    private Map<TagModel, SensorValueModel[]> telNoiseData;

    public NoorderzonChart(List<TaggedDataModel> data) {

        // separate the data types from the different sensors that will be charted
        separateDataTypes(data);

        // determine how many charts will be drawn, to get the proper height of one chart
        double nrOfCharts = (this.xData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.yData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.zData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.tempData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.telxData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.telyData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.telzData.size() > 0) ? 1 : 0;
        nrOfCharts += (this.telNoiseData.size() > 0) ? 1 : 0;
        nrOfCharts = (nrOfCharts > 2) ? 2 : nrOfCharts;

        // put charts in the layout
        this.setLayout(new RowLayout());
        this.setScrollMode(Scroll.AUTOY);
        if (nrOfCharts == 0) {
            this.setLayout(new CenterLayout());
            this.add(new Text("No numerical data to visualize..."));
        } else {
            if (this.xData.size() > 0) {
                this.add(new TimeLineChart(this.xData, "Versnelling X-as"), new RowData(-1,
                        1 / nrOfCharts));
            }
            if (this.yData.size() > 0) {
                this.add(new TimeLineChart(this.yData, "Versnelling Y-as"), new RowData(-1,
                        1 / nrOfCharts));
            }
            if (this.zData.size() > 0) {
                this.add(new TimeLineChart(this.zData, "Versnelling Z-as"), new RowData(-1,
                        1 / nrOfCharts));
            }
            if (this.tempData.size() > 0) {
                this.add(new TimeLineChart(this.tempData, "Temperatuur"), new RowData(-1,
                        1 / nrOfCharts));
            }
            if (this.telxData.size() > 0) {
                this.add(new TimeLineChart(this.telxData, "Telefoon X-as versnelling"),
                        new RowData(-1, 1 / nrOfCharts));
            }
            if (this.telyData.size() > 0) {
                this.add(new TimeLineChart(this.telyData, "Telefoon Y-as versnalling"),
                        new RowData(-1, 1 / nrOfCharts));
            }
            if (this.telzData.size() > 0) {
                this.add(new TimeLineChart(this.telzData, "Telefoon Z-as versnelling"),
                        new RowData(-1, 1 / nrOfCharts));
            }
            if (this.telNoiseData.size() > 0) {
                this.add(new TimeLineChart(this.telNoiseData, "Telefoon geluidssterkte"),
                        new RowData(-1, 1 / nrOfCharts));
            }
        }
    }

    private void separateDataTypes(List<TaggedDataModel> data) {

        this.xData = new HashMap<TagModel, SensorValueModel[]>();
        this.yData = new HashMap<TagModel, SensorValueModel[]>();
        this.zData = new HashMap<TagModel, SensorValueModel[]>();
        this.tempData = new HashMap<TagModel, SensorValueModel[]>();
        this.telNoiseData = new HashMap<TagModel, SensorValueModel[]>();
        this.telxData = new HashMap<TagModel, SensorValueModel[]>();
        this.telyData = new HashMap<TagModel, SensorValueModel[]>();
        this.telzData = new HashMap<TagModel, SensorValueModel[]>();

        for (TaggedDataModel dataEntry : data) {
            SensorValueModel[] values = dataEntry.getData();

            if (values.length > 0) {
                TagModel tag = dataEntry.getTag();

                if (values[0] instanceof FloatValueModel) {
                    this.telNoiseData.put(tag, values);
                } else {
                    SensorValueModel[] xVals = new SensorValueModel[values.length];
                    SensorValueModel[] yVals = new SensorValueModel[values.length];
                    SensorValueModel[] zVals = new SensorValueModel[values.length];
                    SensorValueModel[] tempVals = new SensorValueModel[values.length];
                    SensorValueModel[] telxVals = new SensorValueModel[values.length];
                    SensorValueModel[] telyVals = new SensorValueModel[values.length];
                    SensorValueModel[] telzVals = new SensorValueModel[values.length];
                    int xCount = 0;
                    int yCount = 0;
                    int zCount = 0;
                    int tempCount = 0;
                    int telMotionCount = 0;

                    for (SensorValueModel mdl : values) {
                        JsonValueModel s = (JsonValueModel) mdl;
                        Map<String, String> fields = s.getFields();

                        if (fields.containsKey("x")) {
                            String myriaMsg = fields.get("x");
                            JSONObject obj = JSONParser.parse(myriaMsg).isObject();
                            double val = Double.parseDouble(obj.get("value").isString()
                                    .stringValue());

                            xVals[xCount] = new FloatValueModel(s.getTimestamp(), val);
                            xCount++;

                        } else if (fields.containsKey("y")) {
                            String myriaMsg = fields.get("y");
                            JSONObject obj = JSONParser.parse(myriaMsg).isObject();
                            double val = Double.parseDouble(obj.get("value").isString()
                                    .stringValue());

                            yVals[yCount] = new FloatValueModel(s.getTimestamp(), val);
                            yCount++;

                        } else if (fields.containsKey("z")) {
                            String myriaMsg = fields.get("z");
                            JSONObject obj = JSONParser.parse(myriaMsg).isObject();
                            double val = Double.parseDouble(obj.get("value").isString()
                                    .stringValue());

                            zVals[zCount] = new FloatValueModel(s.getTimestamp(), val);
                            zCount++;

                        } else if (fields.containsKey("value")) {
                            double val = Double.parseDouble(fields.get("value"));

                            tempVals[tempCount] = new FloatValueModel(s.getTimestamp(), val / 100);
                            tempCount++;
                            
                        } else if (fields.containsKey("x-axis")) {
                            double xval = Double.parseDouble(fields.get("x-axis"));
                            double yval = Double.parseDouble(fields.get("y-axis"));
                            double zval = Double.parseDouble(fields.get("z-axis"));
                            
                            telxVals[telMotionCount] = new FloatValueModel(s.getTimestamp(), xval);
                            telyVals[telMotionCount] = new FloatValueModel(s.getTimestamp(), yval);
                            telzVals[telMotionCount] = new FloatValueModel(s.getTimestamp(), zval);
                            telMotionCount++;
                        } else {
                            Log.d(TAG, "Unexpected data");
                        }
                    }

                    if (xCount > 0) {
                        SensorValueModel[] xValsFinal = new SensorValueModel[xCount];
                        System.arraycopy(xVals, 0, xValsFinal, 0, xCount);
                        this.xData.put(tag, xValsFinal);
                    }
                    if (yCount > 0) {
                        SensorValueModel[] yValsFinal = new SensorValueModel[yCount];
                        System.arraycopy(yVals, 0, yValsFinal, 0, yCount);
                        this.yData.put(tag, yValsFinal);
                    }
                    if (zCount > 0) {
                        SensorValueModel[] zValsFinal = new SensorValueModel[zCount];
                        System.arraycopy(zVals, 0, zValsFinal, 0, zCount);
                        this.zData.put(tag, zValsFinal);
                    }
                    if (tempCount > 0) {
                        SensorValueModel[] tempValsFinal = new SensorValueModel[tempCount];
                        System.arraycopy(tempVals, 0, tempValsFinal, 0, tempCount);
                        this.tempData.put(tag, tempValsFinal);
                    }
                    if (telMotionCount > 0) {
                        this.telxData.put(tag, telxVals);
                        this.telyData.put(tag, telyVals);
                        this.telzData.put(tag, telzVals);
                    }
                }
            }
        }
    }
}