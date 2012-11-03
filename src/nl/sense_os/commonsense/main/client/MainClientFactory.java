package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.application.MainApplicationView;
import nl.sense_os.commonsense.main.client.environments.EnvironmentListView;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupListView;
import nl.sense_os.commonsense.main.client.logout.LogoutView;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorListView;
import nl.sense_os.commonsense.main.client.sensormanagement.vischoice.VisualizationChooserView;
import nl.sense_os.commonsense.main.client.statemanagement.StateListView;
import nl.sense_os.commonsense.main.client.visualization.data.ProgressView;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

/**
 * ClientFactory helpful to use a factory or dependency injection framework like GIN to obtain
 * references to objects needed throughout your application like the {@link EventBus},
 * {@link PlaceController} and views.
 */
public interface MainClientFactory {

	public EnvironmentListView getEnvironmentListView();

	public EventBus getEventBus();

	public GroupListView getGroupListView();

	public LogoutView getLogoutView();

	public MainApplicationView getMainView();

	public PlaceController getPlaceController();

	public ProgressView getProgressView();

	public SensorListView getSensorListView();

	public StateListView getStateListView();

	public VisualizationChooserView getVisualizationChooserView();
}
