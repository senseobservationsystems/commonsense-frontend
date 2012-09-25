package nl.sense_os.commonsense.main.client.event;

import nl.sense_os.commonsense.main.client.event.NewVisualizationEvent.Handler;

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

	public NewVisualizationEvent(int type, long start, long end, boolean subsample) {
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
