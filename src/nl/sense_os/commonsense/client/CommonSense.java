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
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.user.client.Timer;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class CommonSense implements EntryPoint {

    private static final String TAG = "CommonSense";
    public static final String LAST_DEPLOYED = "Mon Feb 28 15:34 CET 2011";
    private boolean isMapsApiLoaded;

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

        initControllers();

        loadMapsApi();
    }

    /**
     * Loads the Google Maps API when the controller is initialized. If loading fails, a popup
     * window is shown.
     */
    private void loadMapsApi() {

        // Asynchronously load the Maps API.
        this.isMapsApiLoaded = false;
        Maps.loadMapsApi(Constants.MAPS_API_KEY, "2.x", true, new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Google Maps API loaded...");
                isMapsApiLoaded = true;
                // initControllers();
            }
        });

        // double check that the API has been loaded within 10 seconds
        new Timer() {

            @Override
            public void run() {
                if (false == isMapsApiLoaded) {
                    MessageBox.confirm(null, "Google Maps API not loaded, retry?",
                            new Listener<MessageBoxEvent>() {

                                @Override
                                public void handleEvent(MessageBoxEvent be) {
                                    final Button b = be.getButtonClicked();
                                    if ("ok".equalsIgnoreCase(b.getText())) {
                                        loadMapsApi();
                                    }
                                }
                            });
                }
            }
        }.schedule(1000 * 10);
    }

    protected void initControllers() {

        Dispatcher dispatcher = Dispatcher.get();

        // start initializing all views
        dispatcher.dispatch(MainEvents.Init);

        // notify the main controller that all views are ready
        dispatcher.dispatch(MainEvents.UiReady);

        GXT.hideLoadingPanel("loading");
    }
}
