package nl.sense_os.commonsense.common.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface LoginHandler extends EventHandler {
    void onLogin(LoginEvent event);
}
