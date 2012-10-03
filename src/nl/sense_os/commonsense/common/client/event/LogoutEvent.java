package nl.sense_os.commonsense.common.client.event;

import com.google.web.bindery.event.shared.Event;

public class LogoutEvent extends Event<LogoutHandler> {

    public static final Type<LogoutHandler> TYPE = new Type<LogoutHandler>();

    @Override
    public Type<LogoutHandler> getAssociatedType() {
	return TYPE;
    }

    @Override
    protected void dispatch(LogoutHandler handler) {
	handler.onLogout(this);
    }
}
