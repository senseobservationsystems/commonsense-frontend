package nl.sense_os.commonsense.common.client.event;

import java.util.List;

import nl.sense_os.commonsense.common.client.model.SensorJso;

import com.google.web.bindery.event.shared.Event;

public class SensorListUpdatedEvent extends Event<SensorListUpdatedHandler> {

	public static final Type<SensorListUpdatedHandler> TYPE = new Type<SensorListUpdatedHandler>();
	private List<SensorJso> sensors;

	public SensorListUpdatedEvent(List<SensorJso> sensors) {
		this.sensors = sensors;
	}

	@Override
	public Type<SensorListUpdatedHandler> getAssociatedType() {
		return TYPE;
	}

	public List<SensorJso> getSensors() {
		return sensors;
	}

	@Override
	protected void dispatch(SensorListUpdatedHandler handler) {
		handler.onSensorListUpdated(this);
	}
}
