package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.environmentmanagement.EnvironmentsPlace;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupsPlace;
import nl.sense_os.commonsense.main.client.logout.LogoutPlace;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorsPlace;
import nl.sense_os.commonsense.main.client.statemanagement.StatesPlace;
import nl.sense_os.commonsense.main.client.visualization.VisualizePlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

/**
 * PlaceHistoryMapper interface is used to attach all places which the PlaceHistoryHandler should be
 * aware of. This is done via the @WithTokenizers annotation.
 */
@WithTokenizers({ EnvironmentsPlace.Tokenizer.class, GroupsPlace.Tokenizer.class,
		LogoutPlace.Tokenizer.class, SensorsPlace.Tokenizer.class, StatesPlace.Tokenizer.class,
		VisualizePlace.Tokenizer.class })
public interface MainPlaceHistoryMapper extends PlaceHistoryMapper {

}
