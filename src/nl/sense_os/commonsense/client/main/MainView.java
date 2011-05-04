package nl.sense_os.commonsense.client.main;

import nl.sense_os.commonsense.client.CommonSense;
import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.auth.registration.RegisterEvents;
import nl.sense_os.commonsense.client.environments.EnvEvents;
import nl.sense_os.commonsense.client.groups.GroupEvents;
import nl.sense_os.commonsense.client.main.components.HelpScreen;
import nl.sense_os.commonsense.client.main.components.HomeScreen;
import nl.sense_os.commonsense.client.main.components.NavPanel;
import nl.sense_os.commonsense.client.sensors.library.SensorLibraryEvents;
import nl.sense_os.commonsense.client.states.list.StateEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.visualization.tabs.VizEvents;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
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
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class MainView extends View {

    private static final String TAG = "MainView";
    private LayoutContainer centerContent;
    private Component helpComponent;
    private Component homeComponent;
    private NavPanel navPanel;
    private Viewport viewport;
    private LayoutContainer westContent;

    public MainView(Controller controller) {
        super(controller);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(MainEvents.Error)) {
            Log.e(TAG, "Error");
            onError(event);

        } else if (type.equals(MainEvents.Init)) {
            // Log.d(TAG, "Init");
            // do nothing: actual initialization is done in initialize()

        } else if (type.equals(MainEvents.UiReady)) {
            // Log.d(TAG, "UiReady");
            onUiReady(event);

        } else if (type.equals(MainEvents.Navigate)) {
            // Log.d(TAG, "Navigate: \'" + event.<String> getData() + "\'");
            onNavigate(event);

        } else if (type.equals(LoginEvents.LoginSuccess)) {
            // Log.d(TAG, "LoginSuccess");
            onLoggedIn(event);

        } else if (type.equals(LoginEvents.LoggedOut)) {
            // Log.d(TAG, "LoggedOut");
            onLoggedOut(event);

        } else {
            Log.e(TAG, "Unexpected event type: " + type);
        }
    }

    private void initCenter() {
        LayoutContainer center = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
        center.setScrollMode(Scroll.AUTOY);
        center.setBorders(false);

        // banner
        final Text bannerText = new Text("CommonSense");
        bannerText.setId("banner-text");
        final LayoutContainer bannerContainer = new LayoutContainer(new CenterLayout());
        bannerContainer.setId("banner-container");
        bannerContainer.setSize(728, 90);
        bannerContainer.add(bannerText);
        final LayoutContainer banner = new LayoutContainer(new CenterLayout());
        banner.setId("banner");
        banner.add(bannerContainer);
        banner.setHeight(90);
        center.add(banner, new RowData(1, -1, new Margins(0)));

        this.centerContent = new LayoutContainer(new FitLayout());
        this.centerContent.setId("center-content");
        center.add(this.centerContent, new RowData(1, 1, new Margins(10, 0, 0, 0)));

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setMargins(new Margins(5));
        this.viewport.add(center, centerData);
    }

    private void initFooter() {
        LayoutContainer footer = new LayoutContainer(new CenterLayout());
        String copyright = "&copy;2011 Sense";
        String bullet = "&nbsp;&nbsp;&#8226;&nbsp;&nbsp;";
        Anchor website = new Anchor("Sense Home", "http://www.sense-os.nl", "_blank");
        String update = "Last update: " + CommonSense.LAST_DEPLOYED;
        HTML footerLink = new HTML(copyright + bullet + website.toString() + bullet + update);
        footer.add(footerLink);
        footer.setId("footer-bar");

        BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 23);
        southData.setMargins(new Margins(0));
        southData.setSplit(false);
        this.viewport.add(footer, southData);
    }

    @Override
    protected void initialize() {
        super.initialize();

        // ViewPort fills browser screen and automatically resizes content
        this.viewport = new Viewport();
        this.viewport.setId("viewport");
        this.viewport.setLayout(new BorderLayout());
        this.viewport.setStyleAttribute("background", "transparent;");

        initNavigation();
        initWest();
        initCenter();
        initFooter();
    }

    private void initNavigation() {
        this.navPanel = new NavPanel();
        this.navPanel.setId("navigation-bar");

        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 23);
        northData.setMargins(new Margins(0));
        northData.setSplit(false);
        this.viewport.add(this.navPanel, northData);
    }

    private void initWest() {

        final LayoutContainer west = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
        west.setBorders(false);

        // Sense logo
        final Image logo = new Image("/img/logo_sense-162x90.png");
        logo.setPixelSize(162, 90);
        logo.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                Log.d(TAG, "relative x: " + event.getRelativeX(logo.getElement()));
                Log.d(TAG, "relative y: " + event.getRelativeY(logo.getElement()));
            }
        });
        final LayoutContainer logoContainer = new LayoutContainer(new CenterLayout());
        logoContainer.setId("logo-container");
        logoContainer.setHeight(90);
        logoContainer.add(logo);
        west.add(logoContainer, new RowData(1, -1, new Margins(0)));

        // real content
        this.westContent = new LayoutContainer(new FitLayout());
        this.westContent.setId("west-content");
        this.westContent.setScrollMode(Scroll.AUTOY);
        west.add(this.westContent, new RowData(1, 1, new Margins(10, 0, 0, 0)));

        // add to viewport
        final BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 0.33f, 275, 2000);
        westData.setMargins(new Margins(5, 0, 5, 5));
        westData.setSplit(true);
        this.viewport.add(west, westData);
    }

    private void onError(AppEvent event) {
        Log.e(TAG, "Error: " + event.<String> getData());
    }

    private void onLoggedIn(AppEvent event) {
        final UserModel user = event.<UserModel> getData();
        this.navPanel.setUser(user);
        this.navPanel.setLoggedIn(true);
    }

    private void onLoggedOut(AppEvent event) {
        this.navPanel.setLoggedIn(false);
    }

    private void onNavigate(AppEvent event) {
        String location = event.<String> getData("new");
        String oldLocation = event.<String> getData("old");
        if (location.equals(oldLocation)) {
            return;
        }

        // select the new center content
        Component newContent = null;
        if (null != location) {
            if (location.equals(NavPanel.HOME)) {
                if (null == this.homeComponent) {
                    this.homeComponent = new HomeScreen();
                }
                newContent = this.homeComponent;

                westContent.removeAll();
                westContent.layout();

                AccordionLayout layout = new AccordionLayout();
                layout.setFill(false);
                westContent.setLayout(layout);

                // login panel
                AppEvent displayLogin = new AppEvent(LoginEvents.Show);
                displayLogin.setData("parent", this.westContent);
                Dispatcher.forwardEvent(displayLogin);

                // register panel
                AppEvent displayRegister = new AppEvent(RegisterEvents.Show);
                displayRegister.setData("parent", this.westContent);
                Dispatcher.forwardEvent(displayRegister);

            } else if (location.equals(NavPanel.VISUALIZATION)) {

                this.centerContent.removeAll();
                this.westContent.removeAll();
                this.westContent.setLayout(new AccordionLayout());

                // sensor library panel
                AppEvent displaySensorGrid = new AppEvent(SensorLibraryEvents.ShowLibrary);
                displaySensorGrid.setData("parent", this.westContent);
                Dispatcher.forwardEvent(displaySensorGrid);

                // groups panel
                AppEvent displayGroups = new AppEvent(GroupEvents.ShowGrid);
                displayGroups.setData("parent", this.westContent);
                Dispatcher.forwardEvent(displayGroups);

                // states panel
                AppEvent displayStates = new AppEvent(StateEvents.ShowGrid);
                displayStates.setData("parent", this.westContent);
                Dispatcher.forwardEvent(displayStates);

                // environments panel
                AppEvent displayEnvironments = new AppEvent(EnvEvents.ShowGrid);
                displayEnvironments.setData("parent", this.westContent);
                Dispatcher.forwardEvent(displayEnvironments);

                // visualizations panel
                AppEvent displayVisualization = new AppEvent(VizEvents.Show);
                displayVisualization.setData("parent", this.centerContent);
                Dispatcher.forwardEvent(displayVisualization);

            } else if (location.equals(NavPanel.HELP)) {
                if (null == this.helpComponent) {
                    this.helpComponent = new HelpScreen();
                }
                newContent = this.helpComponent;

                westContent.removeAll();

            } else if (location.equals(NavPanel.SIGN_OUT)) {
                newContent = null;
                Dispatcher.forwardEvent(LoginEvents.RequestLogout);

            } else {
                LayoutContainer lc = new LayoutContainer(new CenterLayout());
                lc.add(new Text("Under construction..."));
                newContent = lc;
            }
        }

        // remove old center content
        if (null != newContent) {
            this.centerContent.removeAll();
            this.centerContent.add(newContent);
            this.centerContent.layout();
        }

        // update navigation panel
        this.navPanel.setHighlight(location);
    }

    private void onUiReady(AppEvent event) {
        RootPanel.get("gwt").add(this.viewport);
    }
}
