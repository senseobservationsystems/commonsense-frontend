package nl.sense_os.commonsense.main.client.env.view;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.env.components.EnvMap;
import nl.sense_os.commonsense.main.client.ext.model.ExtEnvironment;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

public class EnvViewController extends Controller {

	private static final Logger LOG = Logger.getLogger(EnvViewController.class.getName());
	private View viewer;

	public EnvViewController() {
		// LOG.setLevel(Level.ALL);

		registerEventTypes(EnvViewEvents.Show);
		registerEventTypes(EnvViewEvents.RequestSensors);
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		if (type.equals(EnvViewEvents.RequestSensors)) {
			LOG.finest("RequestSensors");
			final ExtEnvironment environment = event.getData("environment");
			final EnvMap panel = event.getData("panel");
			onSensorsRequest(environment, panel);

		} else

		{
			forwardToView(this.viewer, event);
		}
	}

	@Override
	protected void initialize() {
		LOG.finest("Initialize...");
		this.viewer = new EnvViewer(this);
		super.initialize();
	}

	private void onSensorsRequest(ExtEnvironment environment, EnvMap panel) {

		// get the position sensors for the devices
		List<ExtSensor> library = Registry
				.get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
		List<ExtSensor> envSensors = new ArrayList<ExtSensor>();
		for (ExtSensor sensor : library) {
			if (environment.equals(sensor.getEnvironment())) {
				envSensors.add(sensor);
			}
		}

		panel.setSensors(envSensors);
	}

}
