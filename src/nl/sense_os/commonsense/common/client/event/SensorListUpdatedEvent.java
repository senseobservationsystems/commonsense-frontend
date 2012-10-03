package nl.sense_os.commonsense.common.client.event;

import java.util.List;

import nl.sense_os.commonsense.common.client.model.Sensor;

import com.google.web.bindery.event.shared.Event;

public class SensorListUpdatedEvent extends Event<SensorListUpdatedHandler> {

	public static final Type<SensorListUpdatedHandler> TYPE = new Type<SensorListUpdatedHandler>();
	private List<Sensor> sensors;

	public SensorListUpdatedEvent(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	@Override
	public Type<SensorListUpdatedHandler> getAssociatedType() {
		return TYPE;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	@Override
	protected void dispatch(SensorListUpdatedHandler handler) {
		handler.onSensorListUpdated(this);
	}
}
