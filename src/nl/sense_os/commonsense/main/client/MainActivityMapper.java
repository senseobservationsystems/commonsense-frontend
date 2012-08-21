package nl.sense_os.commonsense.main.client;

import nl.sense_os.commonsense.main.client.allinone.AllInOneActivity;
import nl.sense_os.commonsense.main.client.allinone.AllInOnePlace;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementActivity;
import nl.sense_os.commonsense.main.client.groupmanagement.GroupManagementPlace;
import nl.sense_os.commonsense.main.client.logout.LogoutActivity;
import nl.sense_os.commonsense.main.client.logout.LogoutPlace;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorManagementActivity;
import nl.sense_os.commonsense.main.client.sensormanagement.SensorManagementPlace;

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
		} else if (place instanceof GroupManagementPlace) {
			return new GroupManagementActivity((GroupManagementPlace) place, clientFactory);
		} else if (place instanceof LogoutPlace) {
			return new LogoutActivity((LogoutPlace) place, clientFactory);
		} else if (place instanceof SensorManagementPlace) {
			return new SensorManagementActivity((SensorManagementPlace) place, clientFactory);
		}
		return null;
	}
}
