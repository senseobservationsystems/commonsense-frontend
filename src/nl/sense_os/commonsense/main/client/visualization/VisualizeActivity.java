package nl.sense_os.commonsense.main.client.visualization;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.MainClientFactory;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;

public class VisualizeActivity extends AbstractActivity {

	private static final Logger LOG = Logger.getLogger(VisualizeActivity.class.getName());
	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private MainClientFactory clientFactory;
	private VisualizePlace place;

	public VisualizeActivity(VisualizePlace place, MainClientFactory clientFactory) {
		this.place = place;
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		LOG.info("Start 'visualize' activity");

		LayoutContainer parent = clientFactory.getMainView().getGxtActivityPanel();
		parent.removeAll();
		parent.add(new Label(new VisualizePlace.Tokenizer().getToken(place)));
		parent.layout();
	}
}
