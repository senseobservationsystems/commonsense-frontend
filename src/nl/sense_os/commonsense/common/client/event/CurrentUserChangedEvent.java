package nl.sense_os.commonsense.common.client.event;

import nl.sense_os.commonsense.common.client.model.UserJso;

import com.google.web.bindery.event.shared.Event;

public class CurrentUserChangedEvent extends Event<CurrentUserChangedHandler> {

	public static final Type<CurrentUserChangedHandler> TYPE = new Type<CurrentUserChangedHandler>();
	private UserJso user;

	public CurrentUserChangedEvent(UserJso user) {
		this.user = user;
	}

	public UserJso getUser() {
		return user;
	}

	@Override
	public Type<CurrentUserChangedHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CurrentUserChangedHandler handler) {
		handler.onCurrentUserChanged(this);
	}
}
