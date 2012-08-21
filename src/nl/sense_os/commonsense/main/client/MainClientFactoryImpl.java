package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOneView;
import nl.sense_os.commonsense.main.client.allinone.AllInOneViewImpl;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.application.component.MainApplicationViewGxt;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementView;
import nl.sense_os.commonsense.main.client.groupmanagement.component.GroupManagementViewGxt;
import nl.sense_os.commonsense.main.client.logout.LogoutView;
import nl.sense_os.commonsense.main.client.logout.component.LogoutViewImpl;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorManagementView;
import nl.sense_os.commonsense.main.client.sensormanagement.component.SensorManagementViewGxt;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class MainClientFactoryImpl implements MainClientFactory {

	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);
	private static final MainApplicationView appView = new MainApplicationViewGxt();
	private static final LogoutView logout = new LogoutViewImpl();

	@Override
	public AllInOneView getAllInOneView() {
		return new AllInOneViewImpl();
	}

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
	public SensorManagementView getSensorManagementView() {
		return new SensorManagementViewGxt();
	}

	@Override
	public GroupManagementView getGroupManagementView() {
		return new GroupManagementViewGxt();
	}
}
