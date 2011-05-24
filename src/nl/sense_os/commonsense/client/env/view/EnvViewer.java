package nl.sense_os.commonsense.client.env.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.EnvironmentModel;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.maps.client.overlay.Polygon;

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

    private void showPanel(EnvironmentModel e) {
        LOGGER.fine("Show environment! ID: " + e.getId() + ", name: " + e.getName() + ", floors: "
                + e.getFloors() + ", position: " + e.getPosition().toUrlValue());
        Polygon outline = e.getOutline();
        String outString = "outline: ";
        for (int i = 0; i < outline.getVertexCount(); i++) {
            outString += outline.getVertex(i).toUrlValue() + "; ";
        }
        LOGGER.fine(outString);
    }

    @Override
    protected void initialize() {
        LOGGER.finest("Initialize...");
        super.initialize();
    }

}
