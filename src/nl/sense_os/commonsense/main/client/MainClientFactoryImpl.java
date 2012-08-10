package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOneView;
import nl.sense_os.commonsense.main.client.allinone.AllInOneViewImpl;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.application.MainApplicationViewGxt;
import nl.sense_os.commonsense.main.client.logout.LogoutView;
import nl.sense_os.commonsense.main.client.logout.LogoutViewImpl;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class MainClientFactoryImpl implements MainClientFactory {

	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);
	private static final MainApplicationView appView = new MainApplicationViewGxt();
	private static final LogoutView logout = new LogoutViewImpl();
	private static final AllInOneView sensors = new AllInOneViewImpl();

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
	public AllInOneView getSensorsView() {
		return sensors;
	}
}
