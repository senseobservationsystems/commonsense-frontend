package nl.sense_os.commonsense.client.sensors.library;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.models.SensorJso;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay for list of sensors sent from CommonSense back end using JSON.
 */
public class GetSensorsResponseJso extends JavaScriptObject {

    protected GetSensorsResponseJso() {
        // empty protected constructor
    }

    public final native int getTotal() /*-{
		if (undefined != this.total) {
			return this.total;
		} else {
			return this.sensors.length;
		}
    }-*/;

    public final native JsArray<SensorJso> getRawSensors() /*-{
		return this.sensors;
    }-*/;

    public final List<SensorModel> getSensors() {
        List<SensorModel> list = new ArrayList<SensorModel>();

        JsArray<SensorJso> sensors = getRawSensors();
        if (null != sensors) {
            for (int i = 0; i < sensors.length(); i++) {
                list.add(new SensorModel(sensors.get(i)));
            }
        }

        return list;
    }
}
