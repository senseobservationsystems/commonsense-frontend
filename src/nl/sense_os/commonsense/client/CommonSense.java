package nl.sense_os.commonsense.client;

import java.util.Date;

import nl.sense_os.commonsense.client.ajax.AjaxController;
import nl.sense_os.commonsense.client.environments.BuildingController;
import nl.sense_os.commonsense.client.groups.GroupController;
import nl.sense_os.commonsense.client.login.LoginController;
import nl.sense_os.commonsense.client.main.MainController;
import nl.sense_os.commonsense.client.main.MainEvents;
import nl.sense_os.commonsense.client.sensors.group.GroupSensorsController;
import nl.sense_os.commonsense.client.sensors.personal.MySensorsController;
import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.services.GroupsProxy;
import nl.sense_os.commonsense.client.services.GroupsProxyAsync;
import nl.sense_os.commonsense.client.services.SensorsProxy;
import nl.sense_os.commonsense.client.services.SensorsProxyAsync;
import nl.sense_os.commonsense.client.states.FeedbackController;
import nl.sense_os.commonsense.client.states.StateController;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.VizController;
import nl.sense_os.commonsense.client.visualization.map.MapController;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class CommonSense implements EntryPoint {

    private static final String TAG = "CommonSense";
    public static final String LAST_DEPLOYED = "Mon Feb 28 15:34 CET 2011";
    /**
     * @return a pretty String to show the current time
     */
    private String now() {
        return DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(new Date());
    }

    @Override
    public void onModuleLoad() {

        Log.d(TAG, "===== Module Load (" + now() + ") =====");

        // load services and put them in Registry
        final BuildingServiceAsync buildingService = GWT.create(BuildingService.class);
        Registry.register(Constants.REG_BUILDING_SVC, buildingService);
        final GroupsProxyAsync groupsService = GWT.create(GroupsProxy.class);
        Registry.register(Constants.REG_GROUPS_SVC, groupsService);
        final SensorsProxyAsync tagsService = GWT.create(SensorsProxy.class);
        Registry.register(Constants.REG_TAGS_SVC, tagsService);

        // set up MVC stuff
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new MainController());
        dispatcher.addController(new LoginController());
        dispatcher.addController(new VizController());
        dispatcher.addController(new MySensorsController());
        dispatcher.addController(new GroupController());
        dispatcher.addController(new StateController());
        dispatcher.addController(new BuildingController());
        dispatcher.addController(new GroupSensorsController());
        dispatcher.addController(new FeedbackController());
        dispatcher.addController(new MapController());
        dispatcher.addController(new AjaxController());

        // start initializing all views
        dispatcher.dispatch(MainEvents.Init);

        // notify the main controller that all views are ready
        dispatcher.dispatch(MainEvents.UiReady);

        GXT.hideLoadingPanel("loading");
    }
}
