package nl.sense_os.commonsense.main.client.main;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.component.FooterBar;
import nl.sense_os.commonsense.main.client.application.component.MainNavigationBar;
import nl.sense_os.commonsense.main.client.env.list.EnvEvents;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
import nl.sense_os.commonsense.main.client.groups.list.GroupEvents;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryEvents;
import nl.sense_os.commonsense.main.client.states.list.StateListEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.RootPanel;

public class MainView extends View {

	private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());
	private LayoutContainer centerContent;
	private MainNavigationBar navPanel;
	private LayoutContainer westContent;
	private LayoutContainer appWidget;

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

		} else if (type.equals(MainEvents.LoggedIn)) {
			LOGGER.finest("LoggedIn");
			onLoggedIn(event);

		} else {
			LOGGER.severe("Unexpected event: " + event);
		}
	}

	private void initCenter() {

		this.centerContent = new LayoutContainer(new FitLayout());
		this.centerContent.setId("center-content");

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(10));
		appWidget.add(this.centerContent, centerData);
	}

	@Override
	protected void initialize() {
		LOGGER.finest("Initialize...");

		appWidget = new LayoutContainer();
		appWidget.setLayout(new BorderLayout());
		appWidget.setStyleAttribute("background",
				"url('commonsense/images/bgRightTop.png') no-repeat right top;");

		initNorth();
		initWest();
		initCenter();
		initSouth();

		super.initialize();
	}

	private void initNorth() {

		navPanel = new MainNavigationBar();

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 30);
		northData.setMargins(new Margins(0));
		northData.setSplit(false);
		appWidget.add(navPanel, northData);
	}

	private void initSouth() {

		FooterBar footer = new FooterBar();

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 23);
		southData.setMargins(new Margins(0));
		southData.setSplit(false);
		appWidget.add(footer, southData);
	}

	private void initWest() {

		// real content
		this.westContent = new LayoutContainer(new FitLayout());
		this.westContent.setId("west-content");
		this.westContent.setScrollMode(Scroll.AUTOY);

		// add to viewport
		final BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 0.33f, 275, 2000);
		westData.setMargins(new Margins(10, 0, 10, 10));
		westData.setSplit(true);
		appWidget.add(this.westContent, westData);
	}

	private void onError(AppEvent event) {
		LOGGER.severe("Error: " + event.<String> getData());
	}

	private void onLoggedIn(AppEvent event) {
		final ExtUser user = event.<ExtUser> getData();
		this.navPanel.setUserLabel(user.getUsername());
	}

	private void showContent() {

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
	}

	private void onUiReady(AppEvent event) {

		// ViewPort fills browser screen and automatically resizes content
		Viewport viewport = new Viewport();
		viewport.setLayout(new FitLayout());
		viewport.setStyleAttribute("background",
				"url('commonsense/images/bgLeftBottom.png') no-repeat left bottom;");

		viewport.add(appWidget);

		RootPanel.get().add(viewport);

		showContent();
	}
}
