package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOneView;
import nl.sense_os.commonsense.main.client.allinone.AllInOneViewImpl;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.application.component.GxtMainApplicationView;
import nl.sense_os.commonsense.main.client.environments.EnvironmentListView;
import nl.sense_os.commonsense.main.client.environments.component.GxtEnvironmentGrid;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupListView;
import nl.sense_os.commonsense.main.client.groupmanagement.component.GxtGroupGrid;
import nl.sense_os.commonsense.main.client.logout.LogoutView;
import nl.sense_os.commonsense.main.client.logout.component.LogoutViewImpl;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorListView;
import nl.sense_os.commonsense.main.client.sensormanagement.component.GxtSensorGrid;
import nl.sense_os.commonsense.main.client.statemanagement.StateListView;
import nl.sense_os.commonsense.main.client.statemanagement.component.GxtStateGrid;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class MainClientFactoryImpl implements MainClientFactory {

	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);
	private static final MainApplicationView appView = new GxtMainApplicationView();
	private static final LogoutView logout = new LogoutViewImpl();
	private static SensorListView sensorListView;
	private static GroupListView groupListView;
	private static EnvironmentListView environmentListView;
	private static StateListView stateListView;

	@Override
	public AllInOneView getAllInOneView() {
		return new AllInOneViewImpl();
	}

	@Override
	public EnvironmentListView getEnvironmentListView() {
		if (null == environmentListView) {
			environmentListView = new GxtEnvironmentGrid();
		}
		return environmentListView;
	}

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public GroupListView getGroupListView() {
		if (null == groupListView) {
			groupListView = new GxtGroupGrid();
		}
		return groupListView;
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
	public SensorListView getSensorListView() {
		if (null == sensorListView) {
			sensorListView = new GxtSensorGrid();
		}
		return sensorListView;
	}

	@Override
	public StateListView getStateListView() {
		if (null == stateListView) {
			stateListView = new GxtStateGrid();
		}
		return stateListView;
	}
}
