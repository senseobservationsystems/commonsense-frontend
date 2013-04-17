package nl.sense_os.commonsense.main.client.sensors;

import nl.sense_os.commonsense.main.client.env.list.EnvEvents;
import nl.sense_os.commonsense.main.client.groups.list.GroupEvents;
import nl.sense_os.commonsense.main.client.sensors.library.LibraryEvents;
import nl.sense_os.commonsense.main.client.states.list.StateListEvents;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class SensorsViewImpl extends Composite implements SensorsView {

    private ContentPanel container;
    private ContentPanel centerContent;
    private ContentPanel westContent;

	public SensorsViewImpl() {

        container = new ContentPanel(new RowLayout(Orientation.HORIZONTAL));
        container.setHeaderVisible(false);
        container.setBodyBorder(false);
        container.setSize("100%", "100%");
        container.setId("sensors-view");

		initWest();
		initCenter();

		initComponent(container);
	}

	private void initCenter() {

        centerContent = new ContentPanel(new FitLayout());
        centerContent.setId("center-content");
        centerContent.setHeaderVisible(false);
        centerContent.setBodyBorder(false);

        container.add(centerContent, new RowData(.7, 1, new Margins(10)));
	}

	private void initWest() {

        westContent = new ContentPanel(new FitLayout());
        westContent.setId("west-content");
        westContent.setHeaderVisible(false);
        westContent.setBodyBorder(false);
        westContent.setScrollMode(Scroll.AUTOY);

        container.add(westContent, new RowData(.3, 1, new Margins(10, 0, 10, 10)));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// not used

		showContent();
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

    @Override
    public void foo() {

        container.layout(true);

    }
}
