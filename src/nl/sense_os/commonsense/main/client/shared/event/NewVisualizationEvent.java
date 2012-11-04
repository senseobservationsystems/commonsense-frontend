package nl.sense_os.commonsense.main.client.shared.event;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.shared.event.NewVisualizationEvent.Handler;

import com.google.web.bindery.event.shared.Event;

public class NewVisualizationEvent extends Event<Handler> {

	public interface Handler {
		void onNewVisualization(NewVisualizationEvent event);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();
	public static final int TIMELINE = 0;
	public static final int TABLE = 1;
	public static final int MAP = 2;
	public static final int NETWORK = 3;

	private int type;
	private long start;
	private long end;
	private boolean subsample;
	private List<GxtSensor> sensors;

	public NewVisualizationEvent(List<GxtSensor> sensors, int type, long start, long end,
			boolean subsample) {
		this.sensors = sensors;
		this.type = type;
		this.start = start;
		this.end = end;
		this.subsample = subsample;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onNewVisualization(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	public long getEnd() {
		return end;
	}

	public List<GxtSensor> getSensors() {
		return sensors;
	}

	public long getStart() {
		return start;
	}

	public int getType() {
		return type;
	}

	public boolean isSubsample() {
		return subsample;
	}
}
