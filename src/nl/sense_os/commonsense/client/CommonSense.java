package nl.sense_os.commonsense.client;

import java.util.Date;

import nl.sense_os.commonsense.client.controllers.BuildingController;
import nl.sense_os.commonsense.client.controllers.FeedbackController;
import nl.sense_os.commonsense.client.controllers.GroupController;
import nl.sense_os.commonsense.client.controllers.GroupSensorsController;
import nl.sense_os.commonsense.client.controllers.LoginController;
import nl.sense_os.commonsense.client.controllers.MainController;
import nl.sense_os.commonsense.client.controllers.MySensorsController;
import nl.sense_os.commonsense.client.controllers.StateController;
import nl.sense_os.commonsense.client.controllers.VizController;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.services.GroupsService;
import nl.sense_os.commonsense.client.services.GroupsServiceAsync;
import nl.sense_os.commonsense.client.services.SensorsService;
import nl.sense_os.commonsense.client.services.SensorsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
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
    public static final String LAST_DEPLOYED = "Tue Feb 8 16:21:33 CET 2011";

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
        final GroupsServiceAsync groupsService = GWT.create(GroupsService.class);
        Registry.register(Constants.REG_GROUPS_SVC, groupsService);
        final SensorsServiceAsync tagsService = GWT.create(SensorsService.class);
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

        // start initializing all views
        dispatcher.dispatch(MainEvents.Init);

        // notify the main controller that all views are ready
        dispatcher.dispatch(MainEvents.UiReady);

        GXT.hideLoadingPanel("loading");
    }
}
