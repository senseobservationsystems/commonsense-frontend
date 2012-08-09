package nl.sense_os.commonsense.main.client;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.alerts.create.AlertCreateController;
import nl.sense_os.commonsense.main.client.env.create.EnvCreateController;
import nl.sense_os.commonsense.main.client.env.list.EnvController;
import nl.sense_os.commonsense.main.client.env.view.EnvViewController;
import nl.sense_os.commonsense.main.client.groups.create.GroupCreateController;
import nl.sense_os.commonsense.main.client.groups.invite.GroupInviteController;
import nl.sense_os.commonsense.main.client.groups.join.GroupJoinController;
import nl.sense_os.commonsense.main.client.groups.leave.GroupLeaveController;
import nl.sense_os.commonsense.main.client.groups.list.GroupController;
import nl.sense_os.commonsense.main.client.main.MainController;
import nl.sense_os.commonsense.main.client.main.MainEvents;
import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteController;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryController;
import nl.sense_os.commonsense.main.client.sensors.share.SensorShareController;
import nl.sense_os.commonsense.main.client.sensors.unshare.UnshareController;
import nl.sense_os.commonsense.main.client.states.connect.StateConnectController;
import nl.sense_os.commonsense.main.client.states.create.StateCreateController;
import nl.sense_os.commonsense.main.client.states.defaults.StateDefaultsController;
import nl.sense_os.commonsense.main.client.states.edit.StateEditController;
import nl.sense_os.commonsense.main.client.states.feedback.FeedbackController;
import nl.sense_os.commonsense.main.client.states.list.StateListController;
import nl.sense_os.commonsense.main.client.viz.data.DataController;
import nl.sense_os.commonsense.main.client.viz.panels.VizPanelsController;
import nl.sense_os.commonsense.main.client.viz.tabs.VizMainController;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class MainEntryPoint implements EntryPoint {

	private static final Logger LOG = Logger.getLogger(MainEntryPoint.class.getName());

	public static final boolean HACK_SKIP_LIB_DETAILS = Constants.ALLOW_HACKS && false;

	/**
	 * Dispatches initialization event to the controllers, and shows the UI after initialization.
	 */
	private void initControllers() {

		Dispatcher dispatcher = Dispatcher.get();

		// start initializing all views
		dispatcher.dispatch(MainEvents.Init);

		// notify the main controller that all views are ready
		dispatcher.dispatch(MainEvents.UiReady);
	}

	/**
	 * Initializes the event dispatcher by adding the application's controllers to it.
	 */
	private void initDispatcher() {

		Dispatcher dispatcher = Dispatcher.get();

		dispatcher.addController(new MainController());
		dispatcher.addController(new VizMainController());
		dispatcher.addController(new VizPanelsController());
		dispatcher.addController(new DataController());

		// sensor library controllers
		dispatcher.addController(new LibraryController());
		dispatcher.addController(new SensorDeleteController());
		dispatcher.addController(new SensorShareController());
		dispatcher.addController(new UnshareController());

		// group controllers
		dispatcher.addController(new GroupController());
		dispatcher.addController(new GroupCreateController());
		dispatcher.addController(new GroupJoinController());
		dispatcher.addController(new GroupLeaveController());
		dispatcher.addController(new GroupInviteController());

		// state controllers
		dispatcher.addController(new StateListController());
		dispatcher.addController(new StateConnectController());
		dispatcher.addController(new StateCreateController());
		dispatcher.addController(new StateDefaultsController());
		dispatcher.addController(new StateEditController());
		dispatcher.addController(new FeedbackController());

		// environment controllers
		dispatcher.addController(new EnvController());
		dispatcher.addController(new EnvCreateController());
		dispatcher.addController(new EnvViewController());

		dispatcher.addController(new AlertCreateController());
	}

	@Override
	public void onModuleLoad() {

		/* initialize */
		initDispatcher();
		initControllers();

		GXT.hideLoadingPanel("loading");
	}
}
