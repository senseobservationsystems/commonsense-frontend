package nl.sense_os.commonsense.main.client.event;

import nl.sense_os.commonsense.common.client.model.User;
import nl.sense_os.commonsense.main.client.event.CurrentUserChangedEvent.Handler;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

public class CurrentUserChangedEvent extends Event<Handler> {

	public interface Handler extends EventHandler {
		void onCurrentUserChanged(CurrentUserChangedEvent event);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();
	private User user;

	public CurrentUserChangedEvent(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onCurrentUserChanged(this);
	}
}
