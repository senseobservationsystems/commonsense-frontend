package nl.sense_os.commonsense.main.client.application.component;

import nl.sense_os.commonsense.common.client.component.FooterBar;
import nl.sense_os.commonsense.common.client.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;

public class MainApplicationViewGxt extends Composite implements MainApplicationView {

	private LayoutContainer center;
	private MainNavigationBar mainNavigationBar;
	private SimplePanel simplePanel;

	public MainApplicationViewGxt() {

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
		borderLayout.add(center, new BorderLayoutData(LayoutRegion.CENTER));

		LayoutContainer layoutContainer = new LayoutContainer();

		Hyperlink hprlnkSensors = new Hyperlink("Manage sensors", false, "sensors:");
		hprlnkSensors.setHTML("Sensors");
		layoutContainer.add(hprlnkSensors);

		Hyperlink hprlnkGroups = new Hyperlink("Manage groups", false, "groups:");
		hprlnkGroups.setHTML("Groups");
		layoutContainer.add(hprlnkGroups);

		Hyperlink hprlnkStates = new Hyperlink("States", false, "states:");
		layoutContainer.add(hprlnkStates);

		Hyperlink hprlnkEnvironments = new Hyperlink("Environments", false, "environments:");
		layoutContainer.add(hprlnkEnvironments);
		borderLayout.add(layoutContainer, new BorderLayoutData(LayoutRegion.WEST));
		layoutContainer.setBorders(true);

		wrapper.add(borderLayout);

		initComponent(wrapper);
	}

	@Override
	public AcceptsOneWidget getActivityPanel() {
		return simplePanel;
	}

	@Override
	public LayoutContainer getActivityPanelGxt() {
		return center;
	}

	@Override
	public void onCurrentUserChanged(CurrentUserChangedEvent event) {
		mainNavigationBar.setUserLabel(event.getUser().getUsername());
	}
}
