package nl.sense_os.commonsense.common.client.event;

import com.google.web.bindery.event.shared.Event;

public class CSErrorEvent extends Event<CSErrorHandler> {

    public static final Type<CSErrorHandler> TYPE = new Type<CSErrorHandler>();
    private String message;

    public CSErrorEvent(String message) {
	this.message = message;
    }

    @Override
    protected void dispatch(CSErrorHandler handler) {
	handler.onError(this);
    }

    @Override
    public Type<CSErrorHandler> getAssociatedType() {
	return TYPE;
    }

    public String getMessage() {
	return message;
    }
}
