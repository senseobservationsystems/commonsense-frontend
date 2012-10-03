package nl.sense_os.commonsense.common.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CSErrorHandler extends EventHandler {
    void onError(CSErrorEvent event);
}
