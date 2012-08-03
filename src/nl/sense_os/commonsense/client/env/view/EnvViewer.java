package nl.sense_os.commonsense.client.env.view;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.env.components.EnvMap;
import nl.sense_os.commonsense.common.client.model.EnvironmentModel;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class EnvViewer extends View {

    private static final Logger LOG = Logger.getLogger(EnvViewer.class.getName());

    public EnvViewer(Controller c) {
        super(c);
        // LOG.setLevel(Level.ALL);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();
        if (type.equals(EnvViewEvents.Show)) {
            LOG.finest("Show");
            final EnvironmentModel environment = event.getData("environment");
            showPanel(environment);
        }
    }

    @Override
    protected void initialize() {
        LOG.finest("Initialize...");
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
