package nl.sense_os.commonsense.common.client.event;

import nl.sense_os.commonsense.common.client.model.User;

import com.google.web.bindery.event.shared.Event;

public class CurrentUserChangedEvent extends Event<CurrentUserChangedHandler> {

	public static final Type<CurrentUserChangedHandler> TYPE = new Type<CurrentUserChangedHandler>();
	private User user;

	public CurrentUserChangedEvent(User user) {
		this.user = user;
	}

	public User getUser() {
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
