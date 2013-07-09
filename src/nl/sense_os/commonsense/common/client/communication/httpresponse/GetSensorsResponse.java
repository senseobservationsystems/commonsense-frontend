package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.Sensor;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

/**
 * Overlay for list of sensors sent from CommonSense back end using JSON.
 */
public class GetSensorsResponse extends SenseApiResponse {

	protected GetSensorsResponse() {
		// empty protected constructor
	}

	public native final JsArray<Sensor> getRawSensors() /*-{
		if (undefined != this.sensors) {
			return this.sensors;
		} else {
			return [];
		}
	}-*/;

	public final List<Sensor> getSensors() {
		List<Sensor> list = new ArrayList<Sensor>();

		JsArray<Sensor> sensors = getRawSensors();
		if (null != sensors) {
			for (int i = 0; i < sensors.length(); i++) {
				list.add(sensors.get(i));
			}
		}

		return list;
	}

	public native final int getTotal() /*-{
		if (undefined != this.total) {
			return this.total;
		} else {
			return this.sensors.length;
		}
	}-*/;
}
