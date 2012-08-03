package nl.sense_os.commonsense.client;

import java.util.ArrayList;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.AlertCreateController;
import nl.sense_os.commonsense.client.alerts.create.AlertCreateEvents;
import nl.sense_os.commonsense.client.auth.login.LoginController;
import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.auth.pwreset.PwResetController;
import nl.sense_os.commonsense.client.demo.DemoController;
import nl.sense_os.commonsense.client.env.create.EnvCreateController;
import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.client.env.list.EnvController;
import nl.sense_os.commonsense.client.env.view.EnvViewController;
import nl.sense_os.commonsense.client.groups.create.GroupCreateController;
import nl.sense_os.commonsense.client.groups.create.GroupCreateEvents;
import nl.sense_os.commonsense.client.groups.invite.GroupInviteController;
import nl.sense_os.commonsense.client.groups.join.GroupJoinController;
import nl.sense_os.commonsense.client.groups.leave.GroupLeaveController;
import nl.sense_os.commonsense.client.groups.list.GroupController;
import nl.sense_os.commonsense.client.main.MainController;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.main.components.NavPanel;
import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteController;
import nl.sense_os.commonsense.client.sensors.library.LibraryController;
import nl.sense_os.commonsense.client.sensors.share.SensorShareController;
import nl.sense_os.commonsense.client.sensors.unshare.UnshareController;
import nl.sense_os.commonsense.client.states.connect.StateConnectController;
import nl.sense_os.commonsense.client.states.create.StateCreateController;
import nl.sense_os.commonsense.client.states.defaults.StateDefaultsController;
import nl.sense_os.commonsense.client.states.edit.StateEditController;
import nl.sense_os.commonsense.client.states.feedback.FeedbackController;
import nl.sense_os.commonsense.client.states.list.StateListController;
import nl.sense_os.commonsense.client.viz.data.DataController;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;
import nl.sense_os.commonsense.client.viz.panels.VizPanelsController;
import nl.sense_os.commonsense.client.viz.panels.map.MapPanel;
import nl.sense_os.commonsense.client.viz.tabs.VizMainController;
import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.constant.Keys;
import nl.sense_os.commonsense.common.client.model.SensorModel;
import nl.sense_os.commonsense.common.client.model.TestData;
import nl.sense_os.commonsense.common.client.model.UserModel;

import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class CommonSense implements EntryPoint {

    private static final Logger LOG = Logger.getLogger(CommonSense.class.getName());

    public static final boolean HACK_QUICK_LOGIN = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_SKIP_LIB_DETAILS = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_TEST_NAVBAR = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_TEST_ENVCREATOR = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_TEST_MAPVIZ = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_TEST_TIMELINE = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_TEST_GROUPCREATOR = Constants.ALLOW_HACKS && false;
    public static final boolean HACK_TEST_ALERTCREATOR = Constants.ALLOW_HACKS && false;

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
	dispatcher.addController(new LoginController());
	// dispatcher.addController(new RegisterController());
	dispatcher.addController(new PwResetController());
	dispatcher.addController(new VizMainController());
	dispatcher.addController(new VizPanelsController());
	dispatcher.addController(new DemoController());
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

	if (HACK_QUICK_LOGIN) {
	    quickLogin();
	} else if (HACK_TEST_ENVCREATOR) {
	    testEnvCreator();
	} else if (HACK_TEST_MAPVIZ) {
	    testMapViz();
	} else if (HACK_TEST_NAVBAR) {
	    testNavBar();
	} else if (HACK_TEST_TIMELINE) {
	    testTimeline();
	} else if (HACK_TEST_GROUPCREATOR) {
	    testGroupCreator();
	} else if (HACK_TEST_ALERTCREATOR) {
	    testAlertCreator();
	} else {
	    initControllers();
	}

	GXT.hideLoadingPanel("loading");
    }

    private void testGroupCreator() {

	initControllers();

	AppEvent show = new AppEvent(GroupCreateEvents.ShowCreator);
	Dispatcher.forwardEvent(show);
    }

    private void testAlertCreator() {

	Dispatcher dispatcher = Dispatcher.get();

	Runnable onLoadCallback = new Runnable() {

	    public void run() {
		Maps.loadMapsApi(Keys.MAPS_KEY, "2", false, new Runnable() {

		    @Override
		    public void run() {

			LOG.fine("API loaded");
			SensorModel sensor = new SensorModel();
			sensor.setDataType("String");
			sensor.setName("Sensor");
			sensor.setId(4);
			// sensor.setDataType("String");
			// sensor.setDataType("json");
			AppEvent show = new AppEvent(AlertCreateEvents.NewCreator);
			show.setData("sensor", sensor);
			Dispatcher.forwardEvent(show);
		    }
		});
	    }
	};
	VisualizationUtils.loadVisualizationApi(onLoadCallback, AnnotatedTimeLine.PACKAGE);

	//
	// // start initializing all views - if this one is on, map doesn show controls
	// dispatcher.dispatch(MainEvents.Init);
	//
	// // notify the main controller that all views are ready
	dispatcher.dispatch(MainEvents.UiReady);

	LOG.config("Test map visualization...");
    }

    /**
     * Logs in automatically for quicker testing.
     */
    private void quickLogin() {
	LOG.config("Quick login...");

	initControllers();

	AppEvent login = new AppEvent(LoginEvents.LoginRequest);
	login.setData("username", "steven@sense-os.nl");
	login.setData("password", "1234");
	Dispatcher.forwardEvent(login);
    }

    private void testEnvCreator() {
	LOG.config("Test environment creator...");

	initControllers();

	Maps.loadMapsApi(Keys.MAPS_KEY, "2", false, new Runnable() {

	    @Override
	    public void run() {
		Dispatcher.forwardEvent(EnvCreateEvents.ShowCreator);
	    }
	});
    }

    private void testMapViz() {
	LOG.config("Test map visualization...");

	Maps.loadMapsApi(Keys.MAPS_KEY, "2", false, new Runnable() {

	    @Override
	    public void run() {
		Window window = new Window();
		window.setLayout(new FitLayout());
		window.setHeading("Maps test");
		window.setSize("90%", "600px");

		MapPanel map = new MapPanel(new ArrayList<SensorModel>(), 0, 0, false, "title");
		window.add(map);
		window.show();
		window.center();

		JsArray<Timeseries> data = TestData.getTimeseriesPosition(10);
		JsArray<Timeseries> data1 = TestData.getTimeseriesPosition1(10);
		for (int i = 0; i < data1.length(); i++) {
		    data.push(data1.get(i));
		}
		// map.onDataReceived(data);
		// TODO fix this test

	    }
	});
    }

    private void testNavBar() {

	Viewport viewport = new Viewport();
	viewport.setLayout(new FitLayout());
	viewport.setId("sense-viewport");
	viewport.setStyleAttribute("background", "transparent;");

	LayoutContainer north = new LayoutContainer(new FitLayout());
	north.setId("sense-header");
	north.setSize("100%", NavPanel.HEIGHT + "px");
	viewport.add(north, new FitData(0, 0, 2000, 0));

	NavPanel navPanel = new NavPanel();
	north.add(navPanel, new RowData(.67, 1));

	navPanel.setUser(new UserModel());
	navPanel.setLoggedIn(true);
	navPanel.setHighlight(NavPanel.VISUALIZATION);

	RootPanel.get("gwt").add(viewport);
    }

    private void testTimeline() {
	LOG.config("Test timeline...");

	// Create a callback to be called when the visualization API has been loaded.
	Runnable onLoadCallback = new Runnable() {

	    @Override
	    public void run() {
		// create a data table
		DataTable data = DataTable.create();
		data.addColumn(DataTable.ColumnType.DATETIME, "startdate");
		data.addColumn(DataTable.ColumnType.DATETIME, "enddate");
		data.addColumn(DataTable.ColumnType.STRING, "content");
		data.addColumn(DataTable.ColumnType.STRING, "group");

		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd");

		// fill the table with some data
		data.addRow();
		data.setValue(0, 0, dtf.parse("2010-08-23"));
		data.setValue(0, 1, dtf.parse("2010-08-30"));
		data.setValue(0, 2, "Project A");
		data.setValue(0, 3, "battery sensor level");
		data.addRow();
		data.setValue(1, 0, dtf.parse("2010-08-28"));
		data.setValue(1, 1, dtf.parse("2010-09-03"));
		data.setValue(1, 2, "Meeting");
		data.setValue(1, 3, "battery sensor level");
		data.addRow();
		data.setValue(2, 0, dtf.parse("2010-08-20"));
		data.setValue(2, 1, dtf.parse("2010-08-25"));
		data.setValue(2, 2, "Phone Call");
		data.setValue(2, 3, "foo");
		data.addRow();
		data.setValue(3, 0, dtf.parse("2010-08-27"));
		data.setValue(3, 1, dtf.parse("2010-08-30"));
		data.setValue(3, 2, "Finished");
		data.setValue(3, 3, "foo");

		// create options
		Timeline.Options options = Timeline.Options.create();
		options.setWidth("100%");
		options.setHeight("200px");
		options.setEditable(true);

		// create the timeline, with data and options
		LOG.fine("Before time line instantiation");
		Timeline timeline = new Timeline(data, options);
		LOG.fine("After time line instantiation");

		RootPanel.get("gwt").add(timeline);
	    }
	};

	// Load the visualization API, passing the onLoadCallback to be called when loading is done.
	VisualizationUtils.loadVisualizationApi(onLoadCallback, new String[] {});
    }
}
