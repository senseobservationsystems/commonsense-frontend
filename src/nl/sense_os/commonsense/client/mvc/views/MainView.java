package nl.sense_os.commonsense.client.mvc.views;

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
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

import nl.sense_os.commonsense.client.components.NavPanel;
import nl.sense_os.commonsense.client.components.Visualization;
import nl.sense_os.commonsense.client.components.building.BuildingMgmt;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.NavEvents;
import nl.sense_os.commonsense.client.utility.Log;

public class MainView extends View {

    private static final String TAG = "MainView";
    private Viewport viewport;
    private Component buildingMgmt;
    private LayoutContainer mainContents;
    private Component loginComponent;
    private Component homeComponent;
    private Component helpComponent;
    private Component navComponent;
    private Component visualization;

    public MainView(Controller controller) {
        super(controller);
    }

    private void createBuildingMgmt() {
        if (null == this.buildingMgmt) {
            this.buildingMgmt = new BuildingMgmt();
        }
    }

    private void createHomeScreen() {
        if (null == this.homeComponent) {
            LayoutContainer home = new LayoutContainer(new FitLayout());
            Frame frame = new Frame("http://welcome.sense-os.nl");
            frame.setStylePrimaryName("senseFrame");
            home.add(frame, new FitData());
            this.homeComponent = home;
        }
    }

    private void createHelpScreen() {
        if (null == this.helpComponent) {
            LayoutContainer help = new LayoutContainer(new FitLayout());
            Frame frame = new Frame("http://welcome.sense-os.nl/node/6");
            frame.setStylePrimaryName("senseFrame");
            help.add(frame, new FitData());
            this.helpComponent = help;
        }
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(MainEvents.Error)) {
            onError(event);
        } else if (eventType.equals(MainEvents.Init)) {
            onInit(event);
        } else if (eventType.equals(MainEvents.UiReady)) {
            onUiReady(event);
        } else if (eventType.equals(NavEvents.NavReady)) {
            onNavReady(event);
        } else if (eventType.equals(NavEvents.NavChanged)) {
            onNavChanged(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            onLoggedOut(event);
        } else if (eventType.equals(LoginEvents.LoginPanelReady)) {
            onLoginReady(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }

    private void onError(AppEvent event) {
        Log.e(TAG, "onError");
    }

    private void onLoggedIn(AppEvent event) {
        Log.d(TAG, "onLoggedIn");

        History.back();
    }

    private void onLoggedOut(AppEvent event) {

    }

    private void onInit(AppEvent event) {
        Log.d(TAG, "onInit");

        // main container with top and center separation
        this.mainContents = new LayoutContainer(new BorderLayout());
        this.mainContents.setId("main_content");
        this.mainContents.setStyleAttribute("background",
                "url('img/bg/right_top_pre-light.png') no-repeat top right transparent;");
        this.mainContents.setStyleAttribute("border-width", "0px");

        // ViewPort fills browser screen and automatically resizes content
        this.viewport = new Viewport();
        this.viewport.setId("viewport");
        this.viewport.setLayout(new FitLayout());
        this.viewport.setStyleAttribute("backgroundColor", "white");
        this.viewport.setStyleAttribute("background",
                "url('img/bg/left_bot_pre-light.png') no-repeat bottom left white;");
        this.viewport.add(mainContents);
    }

    private void onNavReady(AppEvent event) {
        Log.d(TAG, "onNavReady");

        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 25);
        northData.setMargins(new Margins(0));
        northData.setSplit(false);
        this.navComponent = event.getData();
        this.navComponent.setId("navigation_bar");

        this.mainContents.add(this.navComponent, northData);
    }

    private void onLoginReady(AppEvent event) {
        Log.d(TAG, "onLoginReady");

        this.loginComponent = event.getData();
    }

    private void onNavChanged(AppEvent event) {
        Log.d(TAG, "onNavChanged: \'" + event.<String> getData() + "\'");

        // select the new center content
        String location = event.<String> getData();
        Component center = new Text(location);
        if (null != location) {
            if (location.equals(NavPanel.SIGN_IN)) {
                center = this.loginComponent;
            } else if (location.equals(NavPanel.SIGN_OUT)) {
                Dispatcher.forwardEvent(LoginEvents.RequestLogout);
                center = this.loginComponent;
            } else if (location.equals(NavPanel.HOME)) {
                createHomeScreen();
                center = this.homeComponent;
            } else if (location.equals(NavPanel.HELP)) {
                createHelpScreen();
                center = this.helpComponent;
            } else if (location.equals(NavPanel.BUILDING_MGMT)) {
                createBuildingMgmt();
                center = this.buildingMgmt;
            } else if (location.equals(NavPanel.VISUALIZATION)) {
                createVisualization();
                center = this.visualization;
            } else {
                LayoutContainer lc = new LayoutContainer(new CenterLayout());
                lc.add(new Text("Under construction..."));
                center = lc;
            }
        }
        center.setId("center_content");

        // remove old center content
        Component oldCenter = this.mainContents.getItemByItemId("center_content");
        if (null != oldCenter) {
            this.mainContents.remove(oldCenter);
        }

        // add new center
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        this.mainContents.add(center, centerData);
        this.mainContents.layout();
    }

    private void createVisualization() {
        if (null == this.visualization) {
            this.visualization = new Visualization();
        }
    }

    private void onUiReady(AppEvent event) {
        Log.d(TAG, "onUiReady");
        RootPanel.get().add(this.viewport);
    }
}
