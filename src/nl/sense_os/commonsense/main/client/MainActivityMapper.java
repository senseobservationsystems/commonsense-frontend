package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.sensors.SensorsActivity;
import nl.sense_os.commonsense.main.client.sensors.SensorsPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class MainActivityMapper implements ActivityMapper {

	private final MainClientFactory clientFactory;

	public MainActivityMapper(MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof SensorsPlace) {
			return new SensorsActivity((SensorsPlace) place, clientFactory);
		}
		return null;
	}
}
