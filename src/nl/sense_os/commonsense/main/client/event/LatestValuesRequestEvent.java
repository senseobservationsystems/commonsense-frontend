package nl.sense_os.commonsense.main.client.event;

import java.util.List;

import nl.sense_os.commonsense.main.client.event.LatestValuesRequestEvent.Handler;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

public class LatestValuesRequestEvent extends Event<Handler> {

	public interface Handler extends EventHandler {
		void onLatestValuesRequest(LatestValuesRequestEvent event);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();
	private List<GxtSensor> sensors;

	public LatestValuesRequestEvent(List<GxtSensor> sensors) {
		this.sensors = sensors;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onLatestValuesRequest(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	public List<GxtSensor> getSensors() {
		return sensors;
	}

}
