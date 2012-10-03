package nl.sense_os.commonsense.common.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface LogoutHandler extends EventHandler {
    void onLogout(LogoutEvent event);
}
