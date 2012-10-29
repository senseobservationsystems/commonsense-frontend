package nl.sense_os.commonsense.main.client.alerts.create.components;

import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.client.model.DataPoint;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.chap.links.client.Graph;
import com.chap.links.client.Graph.Options.LINESTYLE;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
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

	private static final int INDEX_BELOW_THRESH = 1;
	private static final int INDEX_ABOVE_THRESH = 2;
	private static final int INDEX_IN_RANGE_MIN = 3;
	private static final int INDEX_IN_RANGE_MAX = 4;
	private static final int INDEX_OUT_RANGE_MIN = 5;
	private static final int INDEX_OUT_RANGE_MAX = 6;

	private NumTriggerForm numTriggerForm;
	private LayoutContainer graphContainer;
	private Graph graph;

	private JsArray<Timeseries> graphData;
	private Graph.Options graphOptions = Graph.Options.create();

	public NumTriggerPanel() {
		setLayout(new RowLayout(Orientation.VERTICAL));

		graphContainer = new LayoutContainer(new FitLayout());
		add(graphContainer, new RowData(Style.DEFAULT, 190.0, new Margins()));

		numTriggerForm = new NumTriggerForm();
		add(numTriggerForm, new RowData(Style.DEFAULT, 250.0, new Margins()));

		addFormListeners();
	}

	public void addData(JsArray<Timeseries> data) {
		graphData = data;
		for (int i = graphData.length(); i < INDEX_OUT_RANGE_MAX; i++) {
			clearLine(i);
		}
		drawGraph();
	}

	private void addFormListeners() {
		numTriggerForm.getRadios().addListener(Events.Change, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				Radio selected = (Radio) be.getField().getValue();
				onAboveThreshEnabled(selected == numTriggerForm.getRdAboveThresh());
				onBelowThreshEnabled(selected == numTriggerForm.getRdBelowThresh());
				onInRangeEnabled(selected == numTriggerForm.getRdInsideRange());
				onOutRangeEnabled(selected == numTriggerForm.getRdOutsideRange());
			}
		});
		numTriggerForm.getAboveThreshField().addListener(Events.Valid, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				onAboveThreshValid(true);
			}
		});
		numTriggerForm.getBelowThreshField().addListener(Events.Valid, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				onBelowThreshValid(true);
			}
		});
		numTriggerForm.getInRangeMax().addListener(Events.Valid, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				onInRangeMaxValid(true);
			}
		});
		numTriggerForm.getInRangeMin().addListener(Events.Valid, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				onInRangeMinValid(true);
			}
		});
		numTriggerForm.getOutRangeMax().addListener(Events.Valid, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				onOutRangeMaxValid(true);
			}
		});
		numTriggerForm.getOutRangeMin().addListener(Events.Valid, new Listener<FieldEvent>() {

			@Override
			public void handleEvent(FieldEvent be) {
				onOutRangeMinValid(true);
			}
		});
	}

	private void clearLine(int index) {
		DataPoint dataPoint = graphData.get(0).getData().get(0);
		String emptyTimeseries = "{\"label\":\"trigger\",\"data\":[{\"value\":"
                + dataPoint.getRawValue() + ",\"date\":" + dataPoint.getTimestamp() + "}]}";
		graphData.set(index, JsonUtils.<Timeseries> unsafeEval(emptyTimeseries));
		graphOptions.setLineVisibe(false, index);
	}

	private void drawGraph() {
		if (graphData.length() > 0) {
			graphOptions.setAutoDataStep(true);
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

	/**
	 * Draws a horizontal line on the graph at a specified value
	 * 
	 * @param value
	 * @param line
	 */
	private void insertLine(int index, double value) {

		// get start and end time of the data
		Timeseries timeseries = graphData.get(0);
		long lineStart = timeseries.getStart();
		long lineEnd = timeseries.getEnd();
		long lineLength = lineEnd - lineStart;

		// create line of points between start and end
		String lineJson = "{\"label\":\"trigger\",\"data\":[";
		for (long date = lineStart - (lineLength >> 1); date <= lineEnd + (lineLength >> 1); date += lineLength / 100) {
			lineJson += "{\"date\":" + date + ",\"value\":" + value + "},";
		}
		lineJson = lineJson.substring(0, lineJson.length() - 1);
		lineJson += "]}";

		LOG.finest("insert line: '" + lineJson + "'");
		Timeseries lineData = JsonUtils.safeEval(lineJson);
		graphData.set(index, lineData);

		graphOptions.setLineStyle(LINESTYLE.LINE, index);
		graphOptions.setLineVisibe(true, index);
		graphOptions.setLineColor("Blue", index);
	}

	private void onAboveThreshEnabled(boolean enabled) {
		onAboveThreshValid(enabled && numTriggerForm.getAboveThreshField().isValid(true));
	}

	private void onAboveThreshValid(boolean valid) {
		LOG.finest("Above threshold field " + (valid ? "valid" : "invalid"));
		if (valid) {
			double value = numTriggerForm.getAboveThreshField().getValue().doubleValue();
			insertLine(INDEX_ABOVE_THRESH, value);
		} else {
			clearLine(INDEX_ABOVE_THRESH);
		}
		graph.draw(graphData, graphOptions);
	}

	private void onBelowThreshEnabled(boolean enabled) {
		onBelowThreshValid(enabled && numTriggerForm.getBelowThreshField().isValid(true));
	}

	private void onBelowThreshValid(boolean valid) {
		LOG.finest("Below threshold field " + (valid ? "valid" : "invalid"));
		if (valid) {
			double value = numTriggerForm.getBelowThreshField().getValue().doubleValue();
			insertLine(INDEX_BELOW_THRESH, value);
		} else {
			clearLine(INDEX_BELOW_THRESH);
		}
		graph.draw(graphData, graphOptions);
	}

	private void onInRangeEnabled(boolean enabled) {
		onInRangeMaxValid(enabled && numTriggerForm.getInRangeMax().isValid(true));
		onInRangeMinValid(enabled && numTriggerForm.getInRangeMin().isValid(true));
	}

	private void onInRangeMaxValid(boolean valid) {
		LOG.finest("In range max field " + (valid ? "valid" : "invalid"));
		if (valid) {
			double value = numTriggerForm.getInRangeMax().getValue().doubleValue();
			insertLine(INDEX_IN_RANGE_MAX, value);
		} else {
			clearLine(INDEX_IN_RANGE_MAX);
		}
		graph.draw(graphData, graphOptions);
	}

	private void onInRangeMinValid(boolean valid) {
		LOG.finest("In range min field " + (valid ? "valid" : "invalid"));
		if (valid) {
			double value = numTriggerForm.getInRangeMin().getValue().doubleValue();
			insertLine(INDEX_IN_RANGE_MIN, value);
		} else {
			clearLine(INDEX_IN_RANGE_MIN);
		}
		graph.draw(graphData, graphOptions);
	}

	private void onOutRangeEnabled(boolean enabled) {
		onOutRangeMaxValid(enabled && numTriggerForm.getOutRangeMax().isValid(true));
		onOutRangeMinValid(enabled && numTriggerForm.getOutRangeMin().isValid(true));
	}

	private void onOutRangeMaxValid(boolean valid) {
		LOG.finest("Out range max field " + (valid ? "valid" : "invalid"));
		if (valid) {
			double value = numTriggerForm.getOutRangeMax().getValue().doubleValue();
			insertLine(INDEX_OUT_RANGE_MAX, value);
		} else {
			clearLine(INDEX_OUT_RANGE_MAX);
		}
		graph.draw(graphData, graphOptions);
	}

	private void onOutRangeMinValid(boolean valid) {
		LOG.finest("Out range min field " + (valid ? "valid" : "invalid"));
		if (valid) {
			double value = numTriggerForm.getOutRangeMin().getValue().doubleValue();
			insertLine(INDEX_OUT_RANGE_MIN, value);
		} else {
			clearLine(INDEX_OUT_RANGE_MIN);
		}
		graph.draw(graphData, graphOptions);
	}
}
