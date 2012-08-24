package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOneView;
import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementView;
import nl.sense_os.commonsense.main.client.logout.LogoutView;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorManagementView;
import nl.sense_os.commonsense.main.client.statemanagement.StateManagementView;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

/**
 * ClientFactory helpful to use a factory or dependency injection framework like GIN to obtain
 * references to objects needed throughout your application like the {@link EventBus},
 * {@link PlaceController} and views.
 */
public interface MainClientFactory {

	public AllInOneView getAllInOneView();

	public EventBus getEventBus();

	public GroupManagementView getGroupManagementView();

	public LogoutView getLogoutView();

	public MainApplicationView getMainView();

	public PlaceController getPlaceController();

	public SensorManagementView getSensorManagementView();

	public StateManagementView getStateManagementView();
}
