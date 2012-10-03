package nl.sense_os.commonsense.common.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CurrentUserChangedHandler extends EventHandler {
    void onCurrentUserChanged(CurrentUserChangedEvent event);
}
