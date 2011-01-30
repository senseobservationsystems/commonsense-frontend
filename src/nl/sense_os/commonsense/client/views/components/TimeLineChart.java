package nl.sense_os.commonsense.client.views.components;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;

import java.util.List;

import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.sensorvalues.FloatValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

public class TimeLineChart extends ContentPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineChart";
    private Graph linksGraph;
    private DataTable dataTable;

    public TimeLineChart(List<TaggedDataModel> taggedDatas, String title) {

        for (final TaggedDataModel taggedData : taggedDatas) {
            addData(taggedData);
        }

        setupLayout(title);
    }

    public TimeLineChart(TaggedDataModel taggedData, String title) {

        addData(taggedData);

        setupLayout(title);
    }

    /**
     * Adds data to the collection of displayed data.
     * 
     * @param taggedData
     */
    public void addData(TaggedDataModel taggedData) {

        // add data to data table
        addDataColumn(taggedData);

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
    private void addDataColumn(TaggedDataModel taggedData) {

        final TagModel tag = taggedData.getTag();
        final SensorValueModel[] values = taggedData.getData();

        // create dataTable if necessary
        if (null == this.dataTable) {
            this.dataTable = DataTable.create();
            this.dataTable.addColumn(ColumnType.DATETIME, "Date/Time");
        }

        this.dataTable.addColumn(ColumnType.NUMBER, tag.<String> get("text"));

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

    /**
     * Recreates the annotated time line chart after the chart panel is resized. The time line
     * widget does not seem to be able to resize after being created.
     */
    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);

        // Log.d(TAG, "onResize(" + width + ", " + height + ")");

        // only resize the panel if it is rendered
        if (null != this.linksGraph && this.linksGraph.isAttached()) {
            this.linksGraph.redraw();
        }
    }

    @Override
    public boolean layout() {
        // TODO Auto-generated method stub
        return super.layout();
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