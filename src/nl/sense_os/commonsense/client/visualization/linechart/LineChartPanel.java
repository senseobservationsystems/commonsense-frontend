package nl.sense_os.commonsense.client.visualization.linechart;

import java.util.List;

import nl.sense_os.commonsense.client.json.overlays.Timeseries;
import nl.sense_os.commonsense.client.visualization.VizPanel;
import nl.sense_os.commonsense.shared.SensorModel;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FillData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.JsArray;

public class LineChartPanel extends VizPanel {

    @SuppressWarnings("unused")
    private static final String TAG = "LineChartPanel";
    private Graph graph;
    private final Graph.Options options;

    public LineChartPanel(List<SensorModel> sensors, long start, long end, String title) {
        super();

        // set up layout
        setHeading("Line chart: " + title);
        setBodyBorder(false);
        setLayout(new FillLayout());

        // Graph options
        this.options = Graph.Options.create();
        this.options.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
        this.options.setLineRadius(2);
        this.options.setWidth("100%");
        this.options.setHeight("100%");
        this.options.setLegendCheckboxes(true);
        this.options.setLegendWidth("20%");

        visualize(sensors, start, end);
    }

    @Override
    public void addData(JsArray<Timeseries> data) {
        // Log.d(TAG, "addData...");

        JsArray<Timeseries> floatData = JsArray.createArray().cast();
        for (int i = 0; i < data.length(); i++) {
            Timeseries ts = data.get(i);
            if (ts.getType().equalsIgnoreCase("number")) {
                floatData.push(ts);
            }
        }

        if (floatData.length() > 0) {
            if (null == this.graph) {
                createGraph(floatData);
            } else {
                this.graph.draw(floatData, this.options);
            }

        } else {
            onNoData();
        }
    }

    private void createGraph(JsArray<Timeseries> data) {

        this.graph = new Graph(data, this.options);

        // this LayoutContainer ensures that the graph is sized and resized correctly
        LayoutContainer graphWrapper = new LayoutContainer() {
            @Override
            protected void onResize(int width, int height) {
                super.onResize(width, height);
                redrawGraph();
            }
        };
        graphWrapper.add(this.graph);

        this.add(graphWrapper, new FillData(5));
        this.layout();
    }

    private void onNoData() {
        String msg = "No data to visualize! "
                + "Please make sure that the sensor contains numerical data and that you selected a proper time range.";
        MessageBox.info(null, msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (null == graph) {
                    LineChartPanel.this.hide();
                }
            }
        });
    }

    private void redrawGraph() {
        // only redraw if the graph is already drawn
        if (null != this.graph && this.graph.isAttached()) {
            this.graph.redraw();
        }
    }
}