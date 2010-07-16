package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
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

public class LineChartTab extends LayoutContainer {

    private static final String TAG = "LineChartTab";
    private SensorModel sensor;
    private AnnotatedTimeLine chart;
    private DataTable data;

    public LineChartTab(SensorModel sensor) {
        this.sensor = sensor;
    }

    /**
     * Requests the sensor values from the service. <code>onSensorValuesReceived</code> is invoked
     * by the request's callback.
     */
    private void getSensorValues() {

        final MessageBox progress = MessageBox.progress("Please wait", "Requesting data...", "");
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

        service.getSensorValues(this.sensor.getPhoneId(), this.sensor.getId(), start, end, callback);
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        Log.d(TAG, "onRender");

        // request data from service
        getSensorValues();

        // create data table for chart
        this.data = DataTable.create();
        this.data.addColumn(ColumnType.DATETIME, "Date/Time");
        this.data.addColumn(ColumnType.NUMBER, "Value");

        // create options
        AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
        options.setDisplayAnnotations(true);
        options.setDisplayZoomButtons(true);
        options.setScaleType(AnnotatedTimeLine.ScaleType.ALLFIXED);

        // create linechart
        this.chart = new AnnotatedTimeLine(this.data, options, "600px", "200px");

        // set up this TabItem
        this.setLayout(new FitLayout());
        this.add(this.chart);
    }

    /**
     * Puts the newly received sensor values in a DataTable and draws the chart.
     * 
     * @param success
     *            boolean indicating if the values were successfully received.
     * @param values
     *            the values.
     */
    private void onSensorValuesReceived(boolean success, List<SensorValueModel> values) {
        Log.d(TAG, "onSensorValuesReceived");

        // fill table if values are present
        if ((true == success) && (values.size() > 0)) {

            this.data.addRows(values.size());
            for (int i = 0; i < values.size(); i++) {
                SensorValueModel value = values.get(i);
                this.data.setValue(i, 0, value.getTimestamp());
                Float f = 0.0f;
                try {
                    f = value.getFloatValue();
                } catch (NumberFormatException e) {
                    Log.e(TAG, "NumberFormatException putting sensor values in line chart: "
                            + value.getFloatValue());
                }
                this.data.setValue(i, 1, f);
                Log.d(TAG, "Sensor value: " + value.getTimestamp() + ", " + value.getFloatValue());
            }
        } else {
            Log.w(TAG, "Zero values received!");
        }

        this.chart.draw(this.data);
    }
}
