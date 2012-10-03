package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.application.MainApplicationViewGxt;
import nl.sense_os.commonsense.main.client.logout.LogoutView;
import nl.sense_os.commonsense.main.client.logout.LogoutViewImpl;
import nl.sense_os.commonsense.main.client.sensors.SensorsView;
import nl.sense_os.commonsense.main.client.sensors.SensorsViewImpl;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class MainClientFactoryImpl implements MainClientFactory {

	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);
	private static final MainApplicationView appView = new MainApplicationViewGxt();
	private static final LogoutView logout = new LogoutViewImpl();
	private static SensorsView sensors;

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public LogoutView getLogoutView() {
		return logout;
	}

	@Override
	public MainApplicationView getMainView() {
		return appView;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	@Override
	public SensorsView getSensorsView() {
		if (null == sensors) {
			sensors = new SensorsViewImpl();
		}
		return sensors;
	}
}
