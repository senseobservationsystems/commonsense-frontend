package nl.sense_os.commonsense.client.env.create;

import nl.sense_os.commonsense.client.common.models.SensorJso;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.google.gwt.core.client.JavaScriptObject;

public class CreateSensorResponseJso extends JavaScriptObject {

    protected CreateSensorResponseJso() {
        // empty protected constructor
    }

    public final SensorModel getSensor() {
        SensorJso sensor = getRawSensor();
        if (null != sensor) {
            return new SensorModel(sensor);
        } else {
            return null;
        }
    }

    public final native SensorJso getRawSensor() /*-{
		return this.sensor;
    }-*/;
}
