package nl.sense_os.commonsense.client.env.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

public class EnvViewController extends Controller {

    private static final Logger LOGGER = Logger.getLogger(EnvViewController.class.getName());
    private View viewer;

    public EnvViewController() {
        registerEventTypes(EnvViewEvents.Show);
        LOGGER.setLevel(Level.ALL);
    }

    @Override
    public void handleEvent(AppEvent event) {
        forwardToView(this.viewer, event);
    }

    @Override
    protected void initialize() {
        LOGGER.finest("Initialize...");
        this.viewer = new EnvViewer(this);
        super.initialize();
    }

}
