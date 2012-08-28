package nl.sense_os.commonsense.main.client.event;

import com.google.web.bindery.event.shared.Event;

public class ShowVisualizationEvent extends Event<ShowVisualizationHandler> {

	private static final Type<ShowVisualizationHandler> TYPE = new Type<ShowVisualizationHandler>();

	@Override
	public Type<ShowVisualizationHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(ShowVisualizationHandler handler) {
		handler.onShowVisualization(this);
	}
}
