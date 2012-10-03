package nl.sense_os.commonsense.common.client.event;

import com.google.web.bindery.event.shared.Event;

public class LoginEvent extends Event<LoginHandler> {

    public static final Type<LoginHandler> TYPE = new Type<LoginHandler>();
    private String sessionId;

    public LoginEvent(String sessionId) {
	this.sessionId = sessionId;
    }

    @Override
    protected void dispatch(LoginHandler handler) {
	handler.onLogin(this);
    }

    @Override
    public Type<LoginHandler> getAssociatedType() {
	return TYPE;
    }

    public String getSessionId() {
	return sessionId;
    }
}
