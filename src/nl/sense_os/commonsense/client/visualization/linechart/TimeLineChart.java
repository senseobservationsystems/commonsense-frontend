package nl.sense_os.commonsense.client.visualization.linechart;

import nl.sense_os.commonsense.client.json.overlays.Timeseries;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.core.client.JsArray;

public class TimeLineChart extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "TimeLineChart";
    private Graph linksGraph;

    private TimeLineChart() {
        setupLayout();
    }

    public TimeLineChart(JsArray<Timeseries> data) {
        this();

        // create graph
        this.linksGraph = new Graph(data, options);
        add(this.linksGraph, new FlowData(0));
    }

    /**
     * Adds data to the collection of displayed data.
     * 
     * @param data
     */
    public void addData(JsArray<Timeseries> data) {

        // draw new data table (if chart is visible)
        if (null != linksGraph) {
            this.linksGraph.draw(data, options);
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

    private Graph.Options options;

    /**
     * Sets up the initial layout with the chart panel and chart
     */
    private void setupLayout() {

        // Graph options
        options = Graph.Options.create();
        options.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
        options.setLineRadius(2);
        options.setWidth("100%");
        options.setHeight("100%");
        options.setLegendCheckboxes(true);
        options.setLegendWidth("20%");
    }
}