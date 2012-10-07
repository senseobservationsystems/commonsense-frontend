package nl.sense_os.commonsense.main.client.visualization.component;

import java.util.List;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.VisualizationView;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.core.client.JsArray;

public class MapVisualization extends Composite implements VisualizationView {

	public MapVisualization(List<GxtSensor> sensors, long start, long end, boolean subsample) {

		initComponent(new Label("Map for sensors: " + sensors + ", start: " + start + ", end: "
				+ end + ", subsample: " + subsample));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visualize(JsArray<Timeseries> data) {
		// TODO Auto-generated method stub

	}
}
