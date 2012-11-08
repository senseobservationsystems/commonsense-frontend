package nl.sense_os.commonsense.main.client.application.component;

import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.environmentmanagement.EnvironmentsPlace;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupsPlace;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorsPlace;
import nl.sense_os.commonsense.main.client.shared.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.shared.event.NewVisualizationEvent;
import nl.sense_os.commonsense.main.client.statemanagement.StatesPlace;
import nl.sense_os.commonsense.main.client.visualization.VisualizePlace;
import nl.sense_os.commonsense.shared.client.component.FooterBar;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class GxtMainApplicationView extends Composite implements MainApplicationView {

	private LayoutContainer center;
	private MainNavigationBar mainNavigationBar;
	private SimplePanel hiddenPanel;

	private Hyperlink menuSensors;
	private Hyperlink menuGroups;
	private Hyperlink menuStates;
	private Hyperlink menuEnvironments;
	private Hyperlink menuHighlight;
	private Label noVisualization;
	private FlowPanel visualizationList;
	private HashMap<String, Hyperlink> visualizeItems = new HashMap<String, Hyperlink>();

	public GxtMainApplicationView() {

		LayoutContainer wrapper = new LayoutContainer(new FitLayout());
		wrapper.setStyleAttribute("background",
				"url('commonsense/images/bgLeftBottom.png') no-repeat left bottom;");

		LayoutContainer borderLayout = new LayoutContainer(new BorderLayout());
		borderLayout.setStyleAttribute("background",
				"url('commonsense/images/bgRightTop.png') no-repeat right top;");

		// north: navigation bar
		mainNavigationBar = new MainNavigationBar();
		LayoutContainer north = new LayoutContainer(new FitLayout());
		north.add(mainNavigationBar);
		borderLayout.add(north, new BorderLayoutData(LayoutRegion.NORTH, 30.0f));

		// south: footer bar
		FooterBar footerBar = new FooterBar();
		LayoutContainer south = new LayoutContainer(new FitLayout());
		south.add(footerBar);
		borderLayout.add(south, new BorderLayoutData(LayoutRegion.SOUTH, 30.0f));

		// east: hidden simple panel for place/activities API
		hiddenPanel = new SimplePanel();
		LayoutContainer east = new LayoutContainer();
		east.add(hiddenPanel);
		east.setVisible(false);
		borderLayout.add(east, new BorderLayoutData(LayoutRegion.EAST, 0.0f));

		// center
		center = new LayoutContainer(new FitLayout());
		BorderLayoutData bld_center = new BorderLayoutData(LayoutRegion.CENTER);
		bld_center.setMargins(new Margins(10, 10, 10, 5));
		borderLayout.add(center, bld_center);

		// west
		LayoutContainer west = new LayoutContainer();
		BorderLayoutData bld_west = new BorderLayoutData(LayoutRegion.WEST);
		bld_west.setMargins(new Margins(10, 5, 10, 10));
		borderLayout.add(west, bld_west);

		FlowPanel placeList = new FlowPanel();
		placeList.setStyleName("borderPanel");

		Label lblManageYourData = new Label("Manage your stuff");
		lblManageYourData.setStyleName("panelHeader");
		placeList.add(lblManageYourData);

		menuSensors = new Hyperlink("Sensors", false, "sensors:");
		menuSensors.setStyleName("menuItem");
		menuSensors.setHTML("Sensor library");
		placeList.add(menuSensors);

		menuGroups = new Hyperlink("Groups", false, "groups:");
		menuGroups.setStyleName("menuItem");
		placeList.add(menuGroups);

		menuStates = new Hyperlink("States", false, "states:");
		menuStates.setStyleName("menuItem");
		placeList.add(menuStates);

		menuEnvironments = new Hyperlink("Environments", false, "environments:");
		menuEnvironments.setStyleName("menuItem");
		placeList.add(menuEnvironments);

		west.add(placeList);

		visualizationList = new FlowPanel();
		visualizationList.setStyleName("borderPanel");
		west.add(visualizationList);

		Label lblViewYourData = new Label("View your data");
		lblViewYourData.setStyleName("panelHeader");
		visualizationList.add(lblViewYourData);

		noVisualization = new Label("You have not created any visualizations yet...");
		visualizationList.add(noVisualization);
		noVisualization.addStyleName("menu-empty");

		wrapper.add(borderLayout);

		initComponent(wrapper);

		setHighlight(null);
	}

	@Override
	public AcceptsOneWidget getActivityPanel() {
		return hiddenPanel;
	}

	@Override
	public LayoutContainer getGxtActivityPanel() {
		return center;
	}

	@Override
	public void onCurrentUserChanged(CurrentUserChangedEvent event) {
		mainNavigationBar.setUserLabel(event.getUser().getUsername());
	}

	@Override
	public void onNewVisualization(NewVisualizationEvent event) {
        addVisualizationLink(event.getSensors(), event.getType(), event.getStart(), event.getEnd(),
                event.isSubsample());
    }

    private void addVisualizationLink(List<GxtSensor> sensors, int type, long start, long end,
            boolean subsample) {

        // remove "empty" text
        if (visualizeItems.isEmpty()) {
            visualizationList.remove(noVisualization);
        }

        // Create label
        String label = "";
        for (GxtSensor sensor : sensors) {
            label += sensor.getDisplayName() + ", ";
        }
        label = label.substring(0, label.length() - 2);

        // create hyperlink
        VisualizePlace place = new VisualizePlace(sensors, type, start, end, subsample);
        String token = new VisualizePlace.Tokenizer().getToken(place);
        Hyperlink hyperlink = new Hyperlink(label, false, "visualize:" + token);
        hyperlink.setStyleName("menuItem");

        // put link in menu
        visualizationList.add(hyperlink);
        visualizeItems.put(token, hyperlink);
    }

    @Override
	public void onPlaceChange(PlaceChangeEvent event) {

        // special case for visualize places
        if (event.getNewPlace() instanceof VisualizePlace) {
            VisualizePlace place = (VisualizePlace) event.getNewPlace();
            String token = new VisualizePlace.Tokenizer().getToken(place);

            // add link if it does not exist yet
            if (null == visualizeItems.get(token)) {
                addVisualizationLink(place.getSensors(), place.getType(), place.getStart(),
                        place.getEnd(), place.isSubsample());
            }
        }

		setHighlight(event.getNewPlace());
	}

	private void setHighlight(Place newPlace) {

		// reset current highlight
		if (null != menuHighlight) {
			menuHighlight.removeStyleDependentName("highlight");
		}
		menuHighlight = null;

		// determine which menu item is active
		if (newPlace instanceof GroupsPlace) {
			menuHighlight = menuGroups;
		} else if (newPlace instanceof StatesPlace) {
			menuHighlight = menuStates;
		} else if (newPlace instanceof EnvironmentsPlace) {
			menuHighlight = menuEnvironments;
		} else if (newPlace instanceof SensorsPlace) {
			menuHighlight = menuSensors;
		} else if (newPlace instanceof VisualizePlace) {
			String token = new VisualizePlace.Tokenizer().getToken((VisualizePlace) newPlace);
			menuHighlight = visualizeItems.get(token);
		}

		if (null != menuHighlight) {
			menuHighlight.addStyleDependentName("highlight");
		}
	}
}
