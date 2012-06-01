package nl.sense_os.commonsense.client.main;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.LastDeployed;
import nl.sense_os.commonsense.client.auth.SessionManager;
import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.auth.pwreset.PwResetEvents;
import nl.sense_os.commonsense.client.auth.registration.RegisterEvents;
import nl.sense_os.commonsense.client.common.models.UserModel;
import nl.sense_os.commonsense.client.demo.DemoEvents;
import nl.sense_os.commonsense.client.env.list.EnvEvents;
import nl.sense_os.commonsense.client.groups.list.GroupEvents;
import nl.sense_os.commonsense.client.main.components.HelpScreen;
import nl.sense_os.commonsense.client.main.components.HomeScreen;
import nl.sense_os.commonsense.client.main.components.NavPanel;
import nl.sense_os.commonsense.client.sensors.library.LibraryEvents;
import nl.sense_os.commonsense.client.states.list.StateListEvents;
import nl.sense_os.commonsense.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
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
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class MainView extends View {

    private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());
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
	    LOGGER.severe("Error");
	    onError(event);

	} else if (type.equals(MainEvents.Init)) {
	    LOGGER.finest("Init");
	    // do nothing: actual initialization is done in initialize()

	} else if (type.equals(MainEvents.UiReady)) {
	    LOGGER.finest("UiReady");
	    onUiReady(event);

	} else if (type.equals(MainEvents.Navigate)) {
	    LOGGER.finest("Navigate: \'" + event.<String> getData() + "\'");
	    onNavigate(event);

	} else if (type.equals(LoginEvents.LoginSuccess)) {
	    LOGGER.finest("LoginSuccess");
	    onLoggedIn(event);

	} else if (type.equals(LoginEvents.LoggedOut)) {
	    LOGGER.finest("LoggedOut");
	    onLoggedOut(event);

	} else {
	    LOGGER.severe("Unexpected event: " + event);
	}
    }

    private void initCenter() {
	// LayoutContainer center = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	// center.setScrollMode(Scroll.AUTOY);
	// center.setBorders(false);
	//
	// // banner
	// final Text bannerText = new Text("CommonSense");
	// bannerText.setId("banner-text");
	// final LayoutContainer bannerContainer = new LayoutContainer(new CenterLayout());
	// bannerContainer.setId("banner-container");
	// bannerContainer.setSize(728, 90);
	// bannerContainer.add(bannerText);
	// final LayoutContainer banner = new LayoutContainer(new CenterLayout());
	// banner.setId("banner");
	// banner.add(bannerContainer);
	// banner.setHeight(90);
	// center.add(banner, new RowData(1, -1, new Margins(0)));

	this.centerContent = new LayoutContainer(new FitLayout());
	this.centerContent.setId("center-content");
	// center.add(this.centerContent, new RowData(1, 1, new Margins(10, 0, 0, 0)));

	BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
	centerData.setMargins(new Margins(10));
	// this.viewport.add(center, centerData);
	this.viewport.add(this.centerContent, centerData);
    }

    @Override
    protected void initialize() {
	LOGGER.finest("Initialize...");

	// ViewPort fills browser screen and automatically resizes content
	this.viewport = new Viewport();
	this.viewport.setId("sense-viewport");
	this.viewport.setLayout(new BorderLayout());
	this.viewport.setStyleAttribute("background", "transparent;");

	initNorth();
	initWest();
	initCenter();
	initSouth();

	super.initialize();
    }

    private void initNorth() {

	navPanel = new NavPanel();

	BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, NavPanel.HEIGHT);
	northData.setMargins(new Margins(0));
	northData.setSplit(false);
	this.viewport.add(navPanel, northData);
    }

    private void initSouth() {
	LayoutContainer footer = new LayoutContainer(new CenterLayout());
	String copyright = "&copy;2011 Sense";
	String bullet = "&nbsp;&nbsp;&#8226;&nbsp;&nbsp;";
	Anchor website = new Anchor("Sense Home", "http://www.sense-os.nl", "_blank");
	String update = "Last update: " + LastDeployed.getPrettyString();
	HTML footerLink = new HTML(copyright + bullet + website.toString() + bullet + update);
	footer.add(footerLink);
	footer.setId("footer-bar");

	BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 23);
	southData.setMargins(new Margins(0));
	southData.setSplit(false);
	this.viewport.add(footer, southData);
    }

    private void initWest() {

	// final LayoutContainer west = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	// west.setBorders(false);
	//
	// // Sense logo
	// final Image logo = new Image("/img/logo_sense-162x90.png");
	// logo.setPixelSize(162, 90);
	// logo.addMouseDownHandler(new MouseDownHandler() {
	// @Override
	// public void onMouseDown(MouseDownEvent event) {
	// LOGGER.fine("relative x: " + event.getRelativeX(logo.getElement()));
	// LOGGER.fine("relative y: " + event.getRelativeY(logo.getElement()));
	// }
	// });
	// final LayoutContainer logoContainer = new LayoutContainer(new CenterLayout());
	// logoContainer.setId("logo-container");
	// logoContainer.setHeight(90);
	// logoContainer.add(logo);
	// west.add(logoContainer, new RowData(1, -1, new Margins(0)));

	// real content
	this.westContent = new LayoutContainer(new FitLayout());
	this.westContent.setId("west-content");
	this.westContent.setScrollMode(Scroll.AUTOY);
	// west.add(this.westContent, new RowData(1, 1, new Margins(10, 0, 0, 0)));

	// add to viewport
	final BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 0.33f, 275, 2000);
	westData.setMargins(new Margins(10, 0, 10, 10));
	westData.setSplit(true);
	// this.viewport.add(west, westData);
	this.viewport.add(this.westContent, westData);
    }

    private void onError(AppEvent event) {
	LOGGER.severe("Error: " + event.<String> getData());
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

		// set up west panel layout
		westContent.removeAll();
		AccordionLayout layout = new AccordionLayout();
		layout.setFill(false);
		westContent.setLayout(layout);
		this.westContent.show();

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

		// set up west panel layout
		this.westContent.removeAll();
		this.westContent.setLayout(new AccordionLayout());
		this.westContent.show();

		// sensor library panel
		AppEvent displaySensorGrid = new AppEvent(LibraryEvents.ShowLibrary);
		displaySensorGrid.setData("parent", this.westContent);
		Dispatcher.forwardEvent(displaySensorGrid);

		// groups panel
		AppEvent displayGroups = new AppEvent(GroupEvents.ShowGrid);
		displayGroups.setData("parent", this.westContent);
		Dispatcher.forwardEvent(displayGroups);

		// states panel
		AppEvent displayStates = new AppEvent(StateListEvents.ShowGrid);
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

	    } else if (location.equals(NavPanel.DEMO)) {

		this.centerContent.removeAll();

		// set up west panel layout
		this.westContent.removeAll();
		this.westContent.hide();

		// demo panel
		AppEvent displayDemos = new AppEvent(DemoEvents.Show);
		displayDemos.setData("parent", this.centerContent);
		Dispatcher.forwardEvent(displayDemos);

	    } else if (location.equals(NavPanel.HELP)) {
		if (null == this.helpComponent) {
		    this.helpComponent = new HelpScreen();
		}
		newContent = this.helpComponent;

		// set up west panel layout
		this.westContent.removeAll();
		this.westContent.hide();

	    } else if (location.equals(NavPanel.RESET_PASSWORD)) {

		this.centerContent.removeAll();

		// set up west panel layout
		this.westContent.removeAll();
		this.westContent.hide();

		// demo panel
		AppEvent displayForm = new AppEvent(PwResetEvents.ShowNewPasswordForm);
		displayForm.setData("parent", this.centerContent);
		Dispatcher.forwardEvent(displayForm);

	    } else if (location.equals(NavPanel.ACCOUNT)) {
		String sessionId = SessionManager.getSessionId();
		final UrlBuilder urlBuilder = new UrlBuilder().setHost("accounts.sense-os.nl");
		urlBuilder.setParameter("session_id", sessionId);

		Location.replace(urlBuilder.buildString());

	    } else if (location.equals(NavPanel.REGISTER)) {
		final UrlBuilder urlBuilder = new UrlBuilder().setHost("accounts.sense-os.nl");

		Location.replace(urlBuilder.buildString());

	    } else if (location.equals(NavPanel.SIGN_OUT)) {
		newContent = null;
		Dispatcher.forwardEvent(LoginEvents.RequestLogout);

	    } else {

		// set up west panel layout
		this.westContent.removeAll();
		this.westContent.hide();

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
