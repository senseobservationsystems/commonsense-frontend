package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOnePlace;
import nl.sense_os.commonsense.main.client.environment.EnvironmentManagementPlace;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementPlace;
import nl.sense_os.commonsense.main.client.logout.LogoutPlace;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorManagementPlace;
import nl.sense_os.commonsense.main.client.statemanagement.StateManagementPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceHistoryMapperWithFactory;
import com.google.gwt.place.shared.WithTokenizers;

/**
 * PlaceHistoryMapper interface is used to attach all places which the PlaceHistoryHandler should be
 * aware of. This is done via the @WithTokenizers annotation or by extending
 * {@link PlaceHistoryMapperWithFactory} and creating a separate TokenizerFactory.
 */
@WithTokenizers({ AllInOnePlace.Tokenizer.class, EnvironmentManagementPlace.Tokenizer.class,
		GroupManagementPlace.Tokenizer.class, LogoutPlace.Tokenizer.class,
		SensorManagementPlace.Tokenizer.class, StateManagementPlace.Tokenizer.class })
public interface MainPlaceHistoryMapper extends PlaceHistoryMapper {

}
