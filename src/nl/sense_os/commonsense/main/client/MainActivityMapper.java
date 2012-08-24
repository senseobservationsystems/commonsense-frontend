package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOneActivity;
import nl.sense_os.commonsense.main.client.allinone.AllInOnePlace;
import nl.sense_os.commonsense.main.client.environments.EnvironmentsActivity;
import nl.sense_os.commonsense.main.client.environments.EnvironmentsPlace;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupsActivity;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupsPlace;
import nl.sense_os.commonsense.main.client.logout.LogoutActivity;
import nl.sense_os.commonsense.main.client.logout.LogoutPlace;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorsActivity;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorsPlace;
import nl.sense_os.commonsense.main.client.statemanagement.StatesActivity;
import nl.sense_os.commonsense.main.client.statemanagement.StatesPlace;

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
		if (place instanceof AllInOnePlace) {
			return new AllInOneActivity((AllInOnePlace) place, clientFactory);
		} else if (place instanceof EnvironmentsPlace) {
			return new EnvironmentsActivity((EnvironmentsPlace) place,
					clientFactory);
		} else if (place instanceof GroupsPlace) {
			return new GroupsActivity((GroupsPlace) place, clientFactory);
		} else if (place instanceof LogoutPlace) {
			return new LogoutActivity((LogoutPlace) place, clientFactory);
		} else if (place instanceof SensorsPlace) {
			return new SensorsActivity((SensorsPlace) place, clientFactory);
		} else if (place instanceof StatesPlace) {
			return new StatesActivity((StatesPlace) place, clientFactory);
		}
		return null;
	}
}
