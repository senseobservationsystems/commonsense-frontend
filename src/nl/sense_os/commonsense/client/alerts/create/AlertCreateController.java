package nl.sense_os.commonsense.client.alerts.create;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class AlertCreateController extends Controller {
	
	private Logger LOG = Logger.getLogger(AlertCreateController.class.getName());
	
	private AlertCreator creator;
	
	public AlertCreateController() {
		registerEventTypes(AlertCreateEvents.CreateAlertRequest, AlertCreateEvents.ShowCreator);
		LOG.setLevel(Level.ALL);
	}
	
	@Override
	public void handleEvent(AppEvent event) {
		LOG.fine("AlertController event received");
		forwardToView(creator, event);
		
	}
	
	@Override
	protected void initialize() {
		creator = new AlertCreator(this);
		super.initialize();
	}
}
