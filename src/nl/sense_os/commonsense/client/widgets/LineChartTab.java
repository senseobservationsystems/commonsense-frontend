package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.SensorModel;
import nl.sense_os.commonsense.dto.SensorValueModel;

public class LineChartTab extends TabItem {

    private static final String TAG = "LineChartTab";
    private SensorModel sensor;
    private AnnotatedTimeLine chart;
    private DataTable data;

    public LineChartTab(SensorModel sensor) {
        super(sensor.getName());

        this.sensor = sensor;
    }

    private void getSensorValues() {

        final MessageBox progress = MessageBox.progress("Please wait", "Getting data...", "");
        progress.getProgressBar().auto();
        progress.show();

        final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);

        AsyncCallback<List<SensorValueModel>> callback = new AsyncCallback<List<SensorValueModel>>() {
            public void onFailure(Throwable ex) {
                Log.e(TAG, "Failure in getSensorValues: " + ex.getMessage());
                progress.close();
                onSensorValuesReceived(false, null);
            }

            public void onSuccess(List<SensorValueModel> values) {
                progress.close();
                onSensorValuesReceived(true, values);
            }
        };

        Timestamp start = new Timestamp((new Date().getTime() - (365 * 24 * 60 * 60 * 1000)));
        Timestamp end = new Timestamp(new Date().getTime());
        service.getSensorValues(sensor.getPhone(), sensor.getId(), start, end, callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        Log.d(TAG, "onRender");

        getSensorValues(); 
        
        // create data table for chart
        data = DataTable.create();
        data.addColumn(ColumnType.DATETIME, "Date/Time");
        data.addColumn(ColumnType.NUMBER, "Value");

        // create options
        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setDisplayAnnotations(true);
        options.setDisplayZoomButtons(true);
        options.setScaleType(AnnotatedTimeLine.ScaleType.ALLFIXED);

        // create linechart
        this.chart = new AnnotatedTimeLine(data, options, "600px", "200px"); 
        
        // set up this TabItem
        this.setLayout(new FitLayout());
        this.add(this.chart);
    }

    private void onSensorValuesReceived(boolean success, List<SensorValueModel> values) {
        Log.d(TAG, "onSensorValuesReceived");

        // fill table if values are present
        if (true == success) {
            if (values.size() > 0) {
                
                data.addRows(values.size());
                for (int i = 0; i < values.size(); i++) {
                    SensorValueModel value = values.get(i);
                    data.setValue(i, 0, value.getTimestamp());
                    Double d = 0.0;
                    try {
                        d = Double.valueOf(value.getValue());
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "NumberFormatException putting sensor values in line chart: "
                                + value.getValue());
                    }
                    data.setValue(i, 1, d);
                    Log.d(TAG, "Sensor value: " + value.getTimestamp() + ", " + value.getValue());
                }
            } else {
                Log.w(TAG, "Zero values received!");
            }
        }

        this.chart.draw(data);
    }
}
