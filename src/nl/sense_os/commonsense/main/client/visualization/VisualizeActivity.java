package nl.sense_os.commonsense.main.client.visualization;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.event.NewVisualizationEvent;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.visualization.component.VisualizeTable;
import nl.sense_os.commonsense.main.client.visualization.component.VisualizeTimeline;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class VisualizeActivity extends AbstractActivity {

	private static final Logger LOG = Logger.getLogger(VisualizeActivity.class.getName());
	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private MainClientFactory clientFactory;

	private int type;
	private long start;
	private long end;
	private boolean subsample;
	private List<GxtSensor> sensors;

	public VisualizeActivity(VisualizePlace place, MainClientFactory clientFactory) {
		type = place.getType();
		start = place.getStart();
		end = place.getEnd();
		subsample = place.isSubsample();
		sensors = place.getSensors();
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		LOG.info("Start 'visualize' activity");

		LayoutContainer parent = clientFactory.getMainView().getGxtActivityPanel();
		parent.removeAll();
		switch (type) {
		case NewVisualizationEvent.TIMELINE:
			parent.add(new VisualizeTimeline(sensors, start, end, subsample));
			break;

		case NewVisualizationEvent.TABLE:
			parent.add(new VisualizeTable(sensors, start, end, subsample));
			break;
		default:
			LOG.warning("Unsupported visualization!");
		}
		parent.layout();
	}
}
