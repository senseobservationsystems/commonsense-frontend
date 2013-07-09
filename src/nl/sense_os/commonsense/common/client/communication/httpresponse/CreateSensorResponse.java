package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.Sensor;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

public class CreateSensorResponse extends SenseApiResponse {

	protected CreateSensorResponse() {
		// empty protected constructor
	}

	public final native Sensor getSensor() /*-{
		if (undefined != this.sensor) {
			return this.sensor;
		} else {
			return {};
		}
	}-*/;
}
