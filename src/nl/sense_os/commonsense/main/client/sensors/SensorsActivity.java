package nl.sense_os.commonsense.main.client.sensors;

import nl.sense_os.commonsense.main.client.MainClientFactory;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SensorsActivity extends AbstractActivity {
	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private final MainClientFactory clientFactory;
	private final SensorsPlace place;

	public SensorsActivity(SensorsPlace place, MainClientFactory clientFactory) {
		this.place = place;
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// TODO Auto-generated method stub

	}
}
