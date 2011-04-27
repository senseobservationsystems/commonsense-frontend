package nl.sense_os.commonsense.client;

import java.util.Date;

import nl.sense_os.commonsense.client.ajax.AjaxController;
import nl.sense_os.commonsense.client.data.DataController;
import nl.sense_os.commonsense.client.environments.EnvController;
import nl.sense_os.commonsense.client.groups.GroupController;
import nl.sense_os.commonsense.client.login.LoginController;
import nl.sense_os.commonsense.client.main.MainController;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.registration.RegisterController;
import nl.sense_os.commonsense.client.sensors.group.GroupSensorsController;
import nl.sense_os.commonsense.client.sensors.personal.MySensorsController;
import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.services.GroupsProxy;
import nl.sense_os.commonsense.client.services.GroupsProxyAsync;
import nl.sense_os.commonsense.client.services.SensorsProxy;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.states.connect.StateConnectController;
import nl.sense_os.commonsense.client.states.create.StateCreateController;
import nl.sense_os.commonsense.client.states.edit.StateEditController;
import nl.sense_os.commonsense.client.states.feedback.FeedbackController;
import nl.sense_os.commonsense.client.states.list.StateController;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizController;
import nl.sense_os.commonsense.shared.Constants;

import com.chap.links.client.Timeline;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Theme;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class CommonSense implements EntryPoint {

    private static final String TAG = "CommonSense";
    public static final String LAST_DEPLOYED = "Wed Apr 27 23:58";

    /**
     * Dispatches initialization event to the Controllers, and shows the UI after initialization.
     */
    private void initControllers() {

        Dispatcher dispatcher = Dispatcher.get();

        // start initializing all views
        dispatcher.dispatch(MainEvents.Init);

        // notify the main controller that all views are ready
        dispatcher.dispatch(MainEvents.UiReady);
    }

    /**
     * @return a pretty String to show the current time
     */
    private String now() {
        return DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(new Date());
    }

    @Override
    public void onModuleLoad() {

        Log.d(TAG, "===== Module Load (" + now() + ") =====");

        GXT.setDefaultTheme(Theme.GRAY, true);

        // load services and put them in Registry
        final BuildingServiceAsync buildingService = GWT.create(BuildingService.class);
        Registry.register(Constants.REG_BUILDING_SVC, buildingService);
        final GroupsProxyAsync groupsProxy = GWT.create(GroupsProxy.class);
        Registry.register(Constants.REG_GROUPS_PROXY, groupsProxy);
        final SensorsProxyAsync sensorsProxy = GWT.create(SensorsProxy.class);
        Registry.register(Constants.REG_SENSORS_PROXY, sensorsProxy);

        // set up MVC stuff
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new MainController());
        dispatcher.addController(new LoginController());
        dispatcher.addController(new RegisterController());
        dispatcher.addController(new VizController());
        dispatcher.addController(new DataController());
        dispatcher.addController(new MySensorsController());
        dispatcher.addController(new GroupController());
        dispatcher.addController(new GroupSensorsController());
        dispatcher.addController(new StateController());
        dispatcher.addController(new StateConnectController());
        dispatcher.addController(new StateCreateController());
        dispatcher.addController(new StateEditController());
        dispatcher.addController(new FeedbackController());
        dispatcher.addController(new EnvController());
        dispatcher.addController(new AjaxController());

        // test();
        initControllers();

        GXT.hideLoadingPanel("loading");
    }

    @SuppressWarnings("unused")
    private void test() {

        // Create a callback to be called when the visualization API
        // has been loaded.
        Runnable onLoadCallback = new Runnable() {
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
                options.setLayout(Timeline.Options.LAYOUT.BOX);
                options.setEditable(true);

                // create the timeline, with data and options
                Timeline timeline = new Timeline(data, options);

                RootPanel.get("gwt").add(timeline);
            }
        };

        // Load the visualization API, passing the onLoadCallback to be called when loading is done.
        VisualizationUtils.loadVisualizationApi(onLoadCallback, Timeline.PACKAGE);
    }
}
