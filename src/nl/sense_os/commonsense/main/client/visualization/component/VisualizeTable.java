package nl.sense_os.commonsense.main.client.visualization.component;

import java.util.List;

import nl.sense_os.commonsense.main.client.event.NewSensorDataEvent;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.VisualizeView;

import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.user.client.ui.Composite;

public class VisualizeTable extends Composite implements VisualizeView {

	private long start;
	private long end;
	private boolean subsample;
	private List<GxtSensor> sensors;

	public VisualizeTable(List<GxtSensor> sensors, long start, long end, boolean subsample) {
		this.sensors = sensors;
		this.start = start;
		this.end = end;
		this.subsample = subsample;

		initWidget(new Label("Table for sensors: " + sensors + ", start: " + start + ", end: "
				+ end + ", subsample: " + subsample));
	}

	@Override
	public void onNewSensorData(NewSensorDataEvent event) {
		// TODO Auto-generated method stub

	}
}
