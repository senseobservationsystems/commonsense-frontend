package nl.sense_os.commonsense.main.client.event;

import java.util.List;

import nl.sense_os.commonsense.main.client.event.DataRequestEvent.Handler;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

public class DataRequestEvent extends Event<Handler> {

	public interface Handler extends EventHandler {
		void onDataRequest(DataRequestEvent event);
	}

	private long start, end;
	private List<GxtSensor> sensors;
	private boolean showProgress, subsample;

	public static final Type<Handler> TYPE = new Type<DataRequestEvent.Handler>();

	public DataRequestEvent(long start, long end, List<GxtSensor> sensors, boolean subsample,
			boolean showProgress) {
		setStart(start);
		setEnd(end);
		setSensors(sensors);
		setShowProgress(showProgress);
		setSubsample(subsample);
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onDataRequest(this);
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

	public boolean isShowProgress() {
		return showProgress;
	}

	public boolean isSubsample() {
		return subsample;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setSensors(List<GxtSensor> sensors) {
		this.sensors = sensors;
	}

	public void setShowProgress(boolean showProgress) {
		this.showProgress = showProgress;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public void setSubsample(boolean subsample) {
		this.subsample = subsample;
	}
}
