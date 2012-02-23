package nl.sense_os.commonsense.client.alerts.create;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class AlertCreateController extends Controller {

    private Logger LOG = Logger.getLogger(AlertCreateController.class.getName());

    public AlertCreateController() {
        LOG.setLevel(Level.ALL);
        registerEventTypes(AlertCreateEvents.CreateAlertRequest, AlertCreateEvents.ShowCreator);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(AlertCreateEvents.ShowCreator)) {
            LOG.finest("Create alert creator view");
            AlertCreatorView creator = new AlertCreatorView(this);
            forwardToView(creator, event);

        } else if (type.equals(AlertCreateEvents.CreateAlertRequest)) {
            LOG.finest("CreateAlertRequest");
            // View source = (View) event.getSource();
            // TODO

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }
}
