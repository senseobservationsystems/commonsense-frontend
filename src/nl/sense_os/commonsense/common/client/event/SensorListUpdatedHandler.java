package nl.sense_os.commonsense.common.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface SensorListUpdatedHandler extends EventHandler {
    void onSensorListUpdated(SensorListUpdatedEvent event);
}
