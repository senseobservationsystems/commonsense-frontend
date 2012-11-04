package nl.sense_os.commonsense.main.client;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.alerts.create.AlertCreateController;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.env.create.EnvCreateController;
import nl.sense_os.commonsense.main.client.env.view.EnvViewController;
import nl.sense_os.commonsense.main.client.groups.invite.GroupInviteController;
import nl.sense_os.commonsense.main.client.groups.join.GroupJoinController;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorsPlace;
import nl.sense_os.commonsense.main.client.shared.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.shared.event.DataRequestEvent;
import nl.sense_os.commonsense.main.client.shared.event.NewVisualizationEvent;
import nl.sense_os.commonsense.main.client.shared.loader.ApiLoader;
import nl.sense_os.commonsense.main.client.shared.loader.Loader;
import nl.sense_os.commonsense.main.client.shared.loader.SensorListLoader;
import nl.sense_os.commonsense.main.client.shared.loader.UserDetailsLoader;
import nl.sense_os.commonsense.main.client.states.connect.StateConnectController;
import nl.sense_os.commonsense.main.client.states.create.StateCreateController;
import nl.sense_os.commonsense.main.client.states.defaults.StateDefaultsController;
import nl.sense_os.commonsense.main.client.states.edit.StateEditController;
import nl.sense_os.commonsense.main.client.states.feedback.FeedbackController;
import nl.sense_os.commonsense.main.client.states.list.StateListController;
import nl.sense_os.commonsense.main.client.visualization.data.DataHandler;
import nl.sense_os.commonsense.shared.client.communication.SessionManager;
import nl.sense_os.commonsense.shared.client.model.User;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Entry point for the CommonSense web application. Initializes services, prepares the MVC
 * framework, and dispatches the first events to show the application.
 */
public class MainEntryPoint implements EntryPoint {

	public static final boolean HACK_SKIP_LIB_DETAILS = Constants.ALLOW_HACKS && false;
	private static final Logger LOG = Logger.getLogger(MainEntryPoint.class.getName());

	/**
	 * Redirects the user to the main page
	 */
	public static void goToLoginPage() {

		// clear any session ID to prevent from bouncing back immediately
		SessionManager.removeSessionId();

		UrlBuilder builder = new UrlBuilder();
		builder.setProtocol(Location.getProtocol());
		builder.setHost(Location.getHost());
		String path = Location.getPath().contains("index.html") ? Location.getPath().replace(
				"index.html", "login.html") : Location.getPath() + "login.html";
		builder.setPath(path);
		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
			if ("session_id".equals(entry.getKey()) || "error".equals(entry.getKey())) {
				// do not copy the session id parameter
			} else {
				builder.setParameter(entry.getKey(), entry.getValue().toArray(new String[0]));
			}
		}
		Location.replace(builder.buildString().replace("127.0.0.1%3A", "127.0.0.1:"));
	}

	private MainClientFactory clientFactory;
	private PlaceHistoryHandler historyHandler;

	/**
	 * @return The value of the 'token' URL parameter, or null
	 */
	private String getNewPasswordToken() {
		String token = Location.getParameter("token");
		return token != null && token.length() > 0 ? token : null;
	}

	/**
	 * Initializes the views and starts the default activity.
	 */
	private void init() {

		// Create ClientFactory using deferred binding
		clientFactory = GWT.create(MainClientFactory.class);
		EventBus eventBus = clientFactory.getEventBus();
		PlaceController placeController = clientFactory.getPlaceController();

		// prepare UI
		MainApplicationView main = clientFactory.getMainView();
		eventBus.addHandler(CurrentUserChangedEvent.TYPE, main);
		eventBus.addHandler(NewVisualizationEvent.TYPE, main);
		eventBus.addHandler(PlaceChangeEvent.TYPE, main);
		AcceptsOneWidget appWidget = main.getActivityPanel();

		// Start ActivityManager for the main widget with our ActivityMapper
		ActivityMapper activityMapper = new MainActivityMapper(clientFactory);
		ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
		activityManager.setDisplay(appWidget);

		// Start PlaceHistoryHandler with our PlaceHistoryMapper
		PlaceHistoryMapper historyMapper = GWT.create(MainPlaceHistoryMapper.class);
		historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(placeController, eventBus, new SensorsPlace());

		Viewport viewport = new Viewport();
		viewport.setLayout(new FitLayout());
		viewport.add(main.asWidget());
		RootPanel.get().add(viewport);

		// initialize data handler
		DataHandler dataHandler = new DataHandler(clientFactory);
		clientFactory.getEventBus().addHandler(DataRequestEvent.TYPE, dataHandler);

        // initialize GXT MVC
		initGxt();
	}

	/**
	 * Initializes the event dispatcher by adding the application's controllers to it.
	 */
	private void initGxt() {

		Dispatcher dispatcher = Dispatcher.get();

		// group controllers
		dispatcher.addController(new GroupJoinController());
		dispatcher.addController(new GroupInviteController());

		// state controllers
		dispatcher.addController(new StateListController());
		dispatcher.addController(new StateConnectController());
		dispatcher.addController(new StateCreateController());
		dispatcher.addController(new StateDefaultsController());
		dispatcher.addController(new StateEditController());
		dispatcher.addController(new FeedbackController());

		// environment controllers
		dispatcher.addController(new EnvCreateController());
		dispatcher.addController(new EnvViewController());

		dispatcher.addController(new AlertCreateController());
	}

	@Override
	public void onModuleLoad() {

		String sessionId = SessionManager.getSessionId();
		String newPasswordToken = getNewPasswordToken();
		if (null == sessionId || null != newPasswordToken) {
			goToLoginPage();

        } else {
            // initialize application
            init();

            startPreloaders();
        }
    }

    /**
     * Start the MVP application
     */
    private void startApplication() {

        GXT.hideLoadingPanel("loading");

        // Goes to place represented on URL or default place
        historyHandler.handleCurrentHistory();
    }

    /**
     * Load some information before starting the application
     */
    private void startPreloaders() {
        // get user info
        UserDetailsLoader userLoader = new UserDetailsLoader();
        userLoader.load(new Loader.Callback() {

            @Override
            public void onFailure(int code, Throwable error) {
                LOG.severe("Failed to get user info! Code: " + code + " " + error);
                SessionManager.removeSessionId();
                MainEntryPoint.goToLoginPage();
            }

            @Override
            public void onSuccess(Object result) {
                if (result instanceof User) {
                    User user = (User) result;

                    // store in registry
                    GxtUser gxtUser = new GxtUser(user);
                    Registry.register(Constants.REG_USER, gxtUser);

                    // fire event
                    clientFactory.getEventBus().fireEvent(new CurrentUserChangedEvent(user));

                } else {
                    onFailure(-1, new Throwable("Unexpected user loader result: " + result));
                }
            }
        });

        // get sensor list
        SensorListLoader sensorLoader = new SensorListLoader();
        sensorLoader.load(new Loader.Callback() {

            @Override
            public void onFailure(int code, Throwable error) {
                LOG.severe("Failed to get sensor list! Code: " + code + " " + error);
                SessionManager.removeSessionId();
                MainEntryPoint.goToLoginPage();
            }

            @Override
            public void onSuccess(Object result) {
                if (result instanceof List<?>) {

                    // store the result
                    Registry.register(Constants.REG_SENSOR_LIST, result);

                    startApplication();

                } else {
                    onFailure(-1, new Throwable("Unexpected sensors loader result: " + result));
                }
            }
        });

        // get API libraries
        ApiLoader apiLoader = new ApiLoader();
        apiLoader.load(new Loader.Callback() {

            @Override
            public void onFailure(int code, Throwable error) {
                LOG.severe("Failed to load APIs!");
            }

            @Override
            public void onSuccess(Object loadResult) {
                // nothing to do
            }
        });
	}
}
