package nl.sense_os.commonsense.main.client.application;

import nl.sense_os.commonsense.common.client.component.FooterBar;
import nl.sense_os.commonsense.common.client.event.CurrentUserChangedEvent;
import nl.sense_os.commonsense.main.client.application.component.MainNavigationBar;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
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

		LayoutContainer north = new LayoutContainer();
		north.setLayout(new FitLayout());

		mainNavigationBar = new MainNavigationBar();
		north.add(mainNavigationBar);
		borderLayout.add(north, new BorderLayoutData(LayoutRegion.NORTH, 30.0f));
		north.setBorders(true);

		LayoutContainer south = new LayoutContainer();
		south.setLayout(new FitLayout());

		FooterBar footerBar = new FooterBar();
		south.add(footerBar);
		borderLayout.add(south, new BorderLayoutData(LayoutRegion.SOUTH, 30.0f));
		south.setBorders(true);

		LayoutContainer east = new LayoutContainer();

		simplePanel = new SimplePanel();
		east.add(simplePanel);
		borderLayout.add(east, new BorderLayoutData(LayoutRegion.EAST, 0.0f));
		east.setBorders(true);

		center = new LayoutContainer(new FitLayout());

		borderLayout.add(center, new BorderLayoutData(LayoutRegion.CENTER));
		center.setBorders(true);

		wrapper.add(borderLayout);
		borderLayout.setBorders(true);

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
