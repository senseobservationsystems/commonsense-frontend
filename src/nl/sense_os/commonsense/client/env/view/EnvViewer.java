package nl.sense_os.commonsense.client.env.view;

import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.env.components.EnvMap;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvViewer extends View {

    private static final Logger LOGGER = Logger.getLogger(EnvViewer.class.getName());

    public EnvViewer(Controller c) {
        super(c);
        LOGGER.setLevel(Level.ALL);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();
        if (type.equals(EnvViewEvents.Show)) {
            LOGGER.finest("Show");
            final EnvironmentModel environment = event.getData("environment");
            showPanel(environment);
        }
    }

    @Override
    protected void initialize() {
        LOGGER.finest("Initialize...");
        super.initialize();
    }

    private void showPanel(EnvironmentModel environment) {

        Window w = new Window();
        w.setLayout(new FitLayout());
        w.setSize("75%", "600px");
        w.add(new EnvMap(environment));

        w.show();
    }
}
