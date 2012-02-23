package nl.sense_os.commonsense.client.alerts.create.components;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;

/**
 * Panel to edit triggers for simple numerical sensors. Contains a graph that displays recent sensor
 * data and the levels of the triggers, and a form to control the triggers.
 * 
 * @author steven
 */
public class NumTriggerPanel extends LayoutContainer {

    private static final Logger LOG = Logger.getLogger(NumTriggerPanel.class.getName());

    private NumTriggerForm numTriggerForm;
    private LayoutContainer graphContainer;
    private Graph graph;

    private JsArray<Timeseries> graphData;
    private Graph.Options graphOptions = Graph.Options.create();

    public NumTriggerPanel() {
        LOG.setLevel(Level.ALL);
        setLayout(new RowLayout(Orientation.VERTICAL));

        graphContainer = new LayoutContainer(new FitLayout());
        add(graphContainer, new RowData(Style.DEFAULT, 190.0, new Margins()));

        numTriggerForm = new NumTriggerForm();
        add(numTriggerForm, new RowData(Style.DEFAULT, 250.0, new Margins()));
    }

    /**
     * Draws a horizontal line on the graph at a specified value
     * 
     * @param value
     * @param line
     */
    private void insertLine(double value) {
        String tsJson = "{\"label\":\"trigger\",\"data\":[" + "{\"date\":" + 0 + ",\"value\":"
                + value + "}," + "{\"date\":" + Integer.MAX_VALUE + ",\"value\":" + value + "}"
                + "]}";
        LOG.fine("insert line: '" + tsJson + "'");
        Timeseries lineData = JsonUtils.safeEval(tsJson);
        graphData.push(lineData);

        graphOptions.setLineWidth(3, graphData.length() - 1);
        graphOptions.setLineColor("Blue", graphData.length() - 1);

        graph.draw(graphData, graphOptions);
    }

    public void addData(JsArray<Timeseries> data) {
        graphData = data;
        drawGraph();

        insertLine(50);
    }

    private void drawGraph() {
        if (graphData.length() > 0) {
            graphOptions.setLineStyle(Graph.Options.LINESTYLE.DOTLINE);
            graphOptions.setLineRadius(2);
            graphOptions.setWidth("100%");
            graphOptions.setHeight("100%");
            graphOptions.setLegendVisibility(false);

            graph = new Graph(graphData, graphOptions);
            graph.setHeight("150px");

            graphContainer.add(graph);

            layout();

        } else {
            LOG.warning("No recent sensor data to display!");

            graphContainer.add(new Text("No recent sensor data to display!"));

            layout();
        }
    }

    public FormPanel getForm() {
        return numTriggerForm;
    }
}
