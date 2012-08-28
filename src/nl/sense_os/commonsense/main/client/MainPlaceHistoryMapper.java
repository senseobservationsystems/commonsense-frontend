package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.environments.EnvironmentsPlace;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupsPlace;
import nl.sense_os.commonsense.main.client.logout.LogoutPlace;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorsPlace;
import nl.sense_os.commonsense.main.client.statemanagement.StatesPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceHistoryMapperWithFactory;
import com.google.gwt.place.shared.WithTokenizers;

/**
 * PlaceHistoryMapper interface is used to attach all places which the PlaceHistoryHandler should be
 * aware of. This is done via the @WithTokenizers annotation or by extending
 * {@link PlaceHistoryMapperWithFactory} and creating a separate TokenizerFactory.
 */
@WithTokenizers({ EnvironmentsPlace.Tokenizer.class, GroupsPlace.Tokenizer.class,
		LogoutPlace.Tokenizer.class, SensorsPlace.Tokenizer.class, StatesPlace.Tokenizer.class })
public interface MainPlaceHistoryMapper extends PlaceHistoryMapper {

}
