package nl.sense_os.commonsense.client.visualization.timeline;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.client.json.overlays.DataPoint;
import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.visualization.VizPanel;
import nl.sense_os.commonsense.shared.SensorModel;

import com.chap.links.client.Timeline;
import com.chap.links.client.Timeline.Options;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.DataTable;

public class TimeLinePanel extends VizPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLinePanel";
    private Timeline timeline;
    private final Timeline.Options options;
    private final DataTable dataTable;

    /**
     * Creates new TimeLinePanel instance for the given list of sensors.
     * 
     * @param sensors
     *            List with SensorModels to display in a line chart.
     * @param start
     *            Start time of the period to display.
     * @param end
     *            End time of the period to display.
     */
    public TimeLinePanel(List<SensorModel> sensors, long start, long end, String title) {
        super();

        // set up layout
        setHeading("Time line: " + title);
        setBodyBorder(false);
        setLayout(new FitLayout());

        this.options = Options.create();
        this.options.setWidth("100%");
        this.options.setHeight("100%");
        this.options.setAnimate(false);
        this.options.setStackEvents(false);
        this.dataTable = createDataTable();

        visualize(sensors, start, end);
    }

    @Override
    public void addData(JsArray<Timeseries> data) {

        Timeseries ts;
        JsArray<DataPoint> values;
        DataPoint lastPoint = null, dataPoint = null, nextPoint = null;
        for (int i = 0; i < data.length(); i++) {
            ts = data.get(i);
            values = ts.getData();
            for (int j = 0, index = this.dataTable.getNumberOfRows(); j < values.length(); j++) {
                lastPoint = dataPoint;
                if (j == 0) {
                    dataPoint = values.get(j);
                } else {
                    dataPoint = nextPoint;
                }
                if (j < values.length() - 1) {
                    nextPoint = values.get(j + 1);
                } else {
                    nextPoint = null;
                }
                if (j > 0) {
                    if (false == (lastPoint != null && lastPoint.getRawValue().equals(
                            dataPoint.getRawValue()))) {
                        // value changed! new row...
                        this.dataTable.addRow();
                        index++;
                        this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                        this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                        this.dataTable.setValue(index, 3, ts.getLabel());
                    } else {
                        // only the end time has to be changed
                    }
                } else {
                    // insert first data point
                    this.dataTable.addRow();
                    this.dataTable.setValue(index, 0, dataPoint.getTimestamp());
                    this.dataTable.setValue(index, 2, dataPoint.getRawValue());
                    this.dataTable.setValue(index, 3, ts.getLabel());
                }

                // set end time
                if (nextPoint != null) {
                    long endDate = Math.max(dataPoint.getTimestamp().getTime(), nextPoint
                            .getTimestamp().getTime() - 1000);
                    this.dataTable.setValue(index, 1, new Date(endDate));
                } else {
                    this.dataTable.setValue(index, 1, new Date());
                }
            }
        }

        if (dataTable.getNumberOfRows() > 0) {
            if (null == this.timeline) {
                createTimeline();
            } else {
                this.timeline.redraw();
            }
        } else {
            onNoData();
        }
    }

    /**
     * @return An empty DataTable with the correct columns for Timeline visualization.
     */
    private DataTable createDataTable() {

        DataTable data = DataTable.create();
        data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
        data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
        data.addColumn(DataTable.ColumnType.STRING, "content");
        data.addColumn(DataTable.ColumnType.STRING, "group");

        return data;
    }

    private void createTimeline() {

        this.timeline = new Timeline(this.dataTable, this.options);

        // this LayoutContainer ensures that the graph is sized and resized correctly
        LayoutContainer wrapper = new LayoutContainer() {
            @Override
            protected void onResize(int width, int height) {
                super.onResize(width, height);
                redrawTimeline();
            }
        };
        wrapper.add(this.timeline);

        this.add(wrapper, new FitData(5));
        this.layout();
    }

    private void onNoData() {
        String msg = "No data to visualize! "
                + "Please make sure that you selected a proper time range.";
        MessageBox.info(null, msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                TimeLinePanel.this.hide();
            }
        });
    }

    private void redrawTimeline() {
        // only redraw if the time line is already drawn
        if (null != this.timeline && this.timeline.isAttached()) {
            this.timeline.redraw();
        }
    }
}
