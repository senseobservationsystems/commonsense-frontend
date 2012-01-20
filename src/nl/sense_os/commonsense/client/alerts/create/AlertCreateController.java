package nl.sense_os.commonsense.client.alerts.create;

import java.util.logging.Logger;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

public class AlertCreateController extends Controller {

    private Logger LOG = Logger.getLogger(AlertCreateController.class.getName());

    private View creator;

    public AlertCreateController() {
        registerEventTypes(AlertCreateEvents.CreateAlertRequest, AlertCreateEvents.ShowCreator);
        // LOG.setLevel(Level.ALL);
    }

    @Override
    public void handleEvent(AppEvent event) {
        LOG.finest("forward to alert creator view");
        forwardToView(creator, event);
    }

    @Override
    protected void initialize() {
        creator = new AlertCreator(this);
        super.initialize();
    }
}
