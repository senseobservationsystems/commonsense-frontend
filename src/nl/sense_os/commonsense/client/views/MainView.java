package nl.sense_os.commonsense.client.views;

import nl.sense_os.commonsense.client.events.LoginEvents;
import nl.sense_os.commonsense.client.events.MainEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.views.components.HelpScreen;
import nl.sense_os.commonsense.client.views.components.HomeScreen;
import nl.sense_os.commonsense.client.views.components.NavPanel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;

public class MainView extends View {

    private static final String TAG = "MainView";
    private Viewport viewport;
    private LayoutContainer center;
    private NavPanel navPanel;
    private Component homeComponent;
    private Component helpComponent;

    public MainView(Controller controller) {
        super(controller);
    }

    private void createCenter() {
        this.center = new LayoutContainer(new FitLayout());

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        this.viewport.add(this.center, centerData);
    }

    private void createNavigation() {
        this.navPanel = new NavPanel();
        this.navPanel.setId("navigation_bar");

        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 20);
        northData.setMargins(new Margins(0));
        northData.setSplit(false);
        this.viewport.add(this.navPanel, northData);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(MainEvents.Error)) {
            Log.e(TAG, "Error");
            onError(event);
        } else if (eventType.equals(MainEvents.Init)) {
            // Log.d(TAG, "Init");
            // do nothing: actual initialization is done in initialize()
        } else if (eventType.equals(MainEvents.UiReady)) {
            // Log.d(TAG, "UiReady");
            onUiReady(event);
        } else if (eventType.equals(MainEvents.Navigate)) {
            // Log.d(TAG, "Navigate: \'" + event.<String> getData() + "\'");
            onNavigate(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            // Log.d(TAG, "LoggedIn");
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        // ViewPort fills browser screen and automatically resizes content
        this.viewport = new Viewport();
        this.viewport.setId("viewport");
        this.viewport.setLayout(new BorderLayout());
        this.viewport.setStyleAttribute("background",
                "url('img/bg/right_top_pre-light.png') no-repeat top right;");

        createNavigation();
        createCenter();

        RootPanel.get().add(viewport);
    }

    private void onError(AppEvent event) {
        Log.e(TAG, "Error: " + event.<String> getData());
    }

    private void onLoggedIn(AppEvent event) {
        final UserModel user = event.<UserModel> getData();
        this.navPanel.setUser(user);
        this.navPanel.setLoggedIn(true);

        History.back();
    }

    private void onLoggedOut(AppEvent event) {
        this.navPanel.setLoggedIn(false);
    }

    private void onNavigate(AppEvent event) {
        String location = event.<String> getData();

        // select the new center content
        Component newContent = null;
        if (null != location) {
            if (location.equals(NavPanel.SIGN_IN)) {
                Dispatcher.forwardEvent(MainEvents.ShowLogin, this.center);
            } else if (location.equals(NavPanel.SIGN_OUT)) {
                Dispatcher.forwardEvent(LoginEvents.RequestLogout);
                Dispatcher.forwardEvent(MainEvents.ShowLogin, this.center);
            } else if (location.equals(NavPanel.HOME)) {
                if (null == this.homeComponent) {
                    this.homeComponent = new HomeScreen();
                }
                newContent = this.homeComponent;
            } else if (location.equals(NavPanel.HELP)) {
                if (null == this.helpComponent) {
                    this.helpComponent = new HelpScreen();
                }
                newContent = this.helpComponent;
            } else if (location.equals(NavPanel.VISUALIZATION)) {
                Dispatcher.forwardEvent(MainEvents.ShowVisualization, this.center);
            } else {
                LayoutContainer lc = new LayoutContainer(new CenterLayout());
                lc.add(new Text("Under construction..."));
                newContent = lc;
            }
        }

        // remove old center content
        if (null != newContent) {
            newContent.setId("center_content");
            this.center.removeAll();
            this.center.add(newContent);
            this.center.layout();
        }

        // update navigation panel
        this.navPanel.setHighlight(location);
    }

    private void onUiReady(AppEvent event) {
        // RootPanel.get().add(this.viewport);
    }
}
