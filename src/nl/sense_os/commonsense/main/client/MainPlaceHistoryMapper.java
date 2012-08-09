package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.sensors.SensorsPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceHistoryMapperWithFactory;
import com.google.gwt.place.shared.WithTokenizers;

/**
 * PlaceHistoryMapper interface is used to attach all places which the PlaceHistoryHandler should be
 * aware of. This is done via the @WithTokenizers annotation or by extending
 * {@link PlaceHistoryMapperWithFactory} and creating a separate TokenizerFactory.
 */
@WithTokenizers(SensorsPlace.Tokenizer.class)
public interface MainPlaceHistoryMapper extends PlaceHistoryMapper {

}
