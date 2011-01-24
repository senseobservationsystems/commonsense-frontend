package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import java.util.Date;

import nl.sense_os.commonsense.client.mvc.controllers.GroupsController;
import nl.sense_os.commonsense.client.mvc.controllers.LoginController;
import nl.sense_os.commonsense.client.mvc.controllers.MainController;
import nl.sense_os.commonsense.client.mvc.controllers.TagsController;
import nl.sense_os.commonsense.client.mvc.controllers.VizController;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.services.GroupsService;
import nl.sense_os.commonsense.client.services.GroupsServiceAsync;
import nl.sense_os.commonsense.client.services.TagsService;
import nl.sense_os.commonsense.client.services.TagsServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class CommonSense implements EntryPoint {

    private static final String TAG = "CommonSense";

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
        final TagsServiceAsync tagsService = GWT.create(TagsService.class);
        Registry.register(Constants.REG_TAGS_SVC, tagsService);

        // set up MVC stuff
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(new MainController());
        dispatcher.addController(new VizController());
        dispatcher.addController(new GroupsController());
        dispatcher.addController(new TagsController());
        dispatcher.addController(new LoginController());

        // start initializing all views
        dispatcher.dispatch(MainEvents.Init);

        // notify the main controller that all views are ready
        dispatcher.dispatch(MainEvents.UiReady);
        
        GXT.hideLoadingPanel("loading");
    }
}
