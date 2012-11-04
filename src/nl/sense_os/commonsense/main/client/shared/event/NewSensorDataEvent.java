package nl.sense_os.commonsense.main.client.shared.event;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.shared.client.model.Timeseries;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

public class NewSensorDataEvent extends Event<NewSensorDataEvent.Handler> {

	public interface Handler extends EventHandler {
		void onNewSensorData(NewSensorDataEvent event);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();
	private List<GxtSensor> sensors;
	private JsArray<Timeseries> sensorData;

	public NewSensorDataEvent(List<GxtSensor> sensors, JsArray<Timeseries> sensorData) {
		this.sensors = sensors;
		this.sensorData = sensorData;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onNewSensorData(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	public JsArray<Timeseries> getSensorData() {
		return sensorData;
	}

	public List<GxtSensor> getSensors() {
		return sensors;
	}
}
