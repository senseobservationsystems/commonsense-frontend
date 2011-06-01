package nl.sense_os.commonsense.client.env.view;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.env.components.EnvMap;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvViewController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(EnvViewController.class.getName());
    private View viewer;

    public EnvViewController() {
        LOGGER.setLevel(Level.ALL);

        registerEventTypes(EnvViewEvents.Show);
        registerEventTypes(EnvViewEvents.RequestSensors);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvViewEvents.RequestSensors)) {
            LOGGER.finest("RequestSensors");
            final EnvironmentModel environment = event.getData("environment");
            final EnvMap panel = event.getData("panel");
            onSensorsRequest(environment, panel);

        } else

        {
            forwardToView(this.viewer, event);
        }
    }

    @Override
    protected void initialize() {
        LOGGER.finest("Initialize...");
        this.viewer = new EnvViewer(this);
        super.initialize();
    }

    private void onSensorsRequest(EnvironmentModel environment, EnvMap panel) {

        // get the position sensors for the devices
        List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
        List<SensorModel> envSensors = new ArrayList<SensorModel>();
        for (SensorModel sensor : library) {
            if (environment.equals(sensor.getEnvironment())) {
                envSensors.add(sensor);
            }
        }

        panel.setSensors(envSensors);
    }

}
