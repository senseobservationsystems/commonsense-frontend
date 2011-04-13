package nl.sense_os.commonsense.client.visualization.components;

import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;

public class TimeLineChart extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineChart";
    private Graph linksGraph;
    private DataTable dataTable;

    public TimeLineChart(SensorModel sensor, SensorValueModel[] values, String title) {

        addData(sensor, values);

        setupLayout(title);
    }

    /**
     * Adds data to the collection of displayed data.
     * 
     * @param taggedData
     */
    public void addData(SensorModel sensor, SensorValueModel[] values) {

        // add data to data table
        addDataColumn(sensor, values);

        // draw new data table (if chart is visible)
        if (null != linksGraph) {
            this.linksGraph.redraw();
        }
    }

    /**
     * Adds a new column to the data table that is backing the chart.
     * 
     * @param taggedData
     */
    private void addDataColumn(SensorModel sensor, SensorValueModel[] values) {

        // create dataTable if necessary
        if (null == this.dataTable) {
            this.dataTable = DataTable.create();
            this.dataTable.addColumn(ColumnType.DATETIME, "Date/Time");
        }

        this.dataTable.addColumn(ColumnType.NUMBER, sensor.<String> get("text"));

        // fill table with values of next tag
        this.dataTable.addRows(values.length);
        final int offset = this.dataTable.getNumberOfRows() - values.length;
        final int colIndex = this.dataTable.getNumberOfColumns() - 1;

        for (int i = 0; i < values.length; i++) {
            final FloatValueModel value = (FloatValueModel) values[i];

            this.dataTable.setValue(i + offset, 0, value.getTimestamp());
            this.dataTable.setValue(i + offset, colIndex, value.getValue());
        }
    }

    public void redraw() {
        // only redraw if the graph is already drawn
        if (null != this.linksGraph && this.linksGraph.isAttached()) {
            this.linksGraph.redraw();
        }
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        redraw();
    }

    /**
     * Sets up the initial layout with the chart panel
     * 
     * @param title
     *            (optional) title of this chart
     */
    private void setupLayout(String title) {
        if (null != title) {
            setHeading(title);
            setHeaderVisible(true);
        } else {
            setHeaderVisible(false);
        }

        // Graph options
        final Graph.Options options = Graph.Options.create();
        options.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
        options.setLineRadius(2);
        options.setWidth("100%");
        options.setHeight("100%");
        options.setEnableVisibility(true);

        // create graph
        this.linksGraph = new Graph(this.dataTable, options);
        add(this.linksGraph, new FlowData(0));
    }
}