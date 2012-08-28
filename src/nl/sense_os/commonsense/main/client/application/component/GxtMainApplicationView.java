package nl.sense_os.commonsense.main.client.application.component;

import nl.sense_os.commonsense.common.client.component.FooterBar;
import nl.sense_os.commonsense.common.client.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class GxtMainApplicationView extends Composite implements MainApplicationView {

	private LayoutContainer center;
	private MainNavigationBar mainNavigationBar;
	private SimplePanel simplePanel;

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
		simplePanel = new SimplePanel();
		LayoutContainer east = new LayoutContainer();
		east.add(simplePanel);
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
		lblManageYourData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		placeList.add(lblManageYourData);

		Hyperlink hprlnkSensors = new Hyperlink("Sensors", false, "sensors:");
		hprlnkSensors.setHTML("Sensor library");
		placeList.add(hprlnkSensors);

		Hyperlink hprlnkGroups = new Hyperlink("Groups", false, "groups:");
		placeList.add(hprlnkGroups);

		Hyperlink hprlnkStates = new Hyperlink("States", false, "states:");
		placeList.add(hprlnkStates);

		Hyperlink hprlnkEnvironments = new Hyperlink("Environments", false, "environments:");
		placeList.add(hprlnkEnvironments);

		west.add(placeList);

		FlowPanel visualizationList = new FlowPanel();
		visualizationList.setStyleName("borderPanel");
		west.add(visualizationList);

		Label lblViewYourData = new Label("View your data");
		lblViewYourData.setStyleName("panelHeader");
		lblViewYourData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		visualizationList.add(lblViewYourData);

		Label lblNoVisualization = new Label("You have not created any visualizations yet...");
		visualizationList.add(lblNoVisualization);

		wrapper.add(borderLayout);

		initComponent(wrapper);
	}

	@Override
	public AcceptsOneWidget getActivityPanel() {
		return simplePanel;
	}

	@Override
	public LayoutContainer getGxtActivityPanel() {
		return center;
	}

	@Override
	public void onCurrentUserChanged(CurrentUserChangedEvent event) {
		mainNavigationBar.setUserLabel(event.getUser().getUsername());
	}
}
