package nl.sense_os.commonsense.main.client.visualization.component.table;

import java.util.List;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.VisualizationView;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.core.client.JsArray;

public class TableVisualization extends Composite implements VisualizationView {

	public TableVisualization(List<GxtSensor> sensors, long start, long end, boolean subsample) {

		initComponent(new Label("Table for sensors: " + sensors + ", start: " + start + ", end: "
				+ end + ", subsample: " + subsample));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// not used

	}

	@Override
	public void visualize(JsArray<Timeseries> data) {
		// TODO Auto-generated method stub

	}
}
