package nl.sense_os.commonsense.main.client.sensors;

import nl.sense_os.commonsense.main.client.env.list.EnvEvents;
import nl.sense_os.commonsense.main.client.groups.list.GroupEvents;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryEvents;
import nl.sense_os.commonsense.main.client.states.list.StateListEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class SensorsViewImpl extends Composite implements SensorsView {

	private LayoutContainer container;
	private LayoutContainer centerContent;
	private LayoutContainer westContent;

	public SensorsViewImpl() {

		container = new LayoutContainer();
		container.setLayout(new BorderLayout());

		initWest();
		initCenter();

		initComponent(container);
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

	private void initCenter() {

		this.centerContent = new LayoutContainer(new FitLayout());
		this.centerContent.setId("center-content");

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(10));
		container.add(this.centerContent, centerData);
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
		container.add(this.westContent, westData);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// not used

		showContent();
	}
}
