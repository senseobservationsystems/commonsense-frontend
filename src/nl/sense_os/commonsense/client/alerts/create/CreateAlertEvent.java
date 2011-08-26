package nl.sense_os.commonsense.client.alerts.create;

import java.util.List;

import nl.sense_os.commonsense.client.common.models.NotificationModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.TriggerModel;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class CreateAlertEvent extends AppEvent {

    private List<SensorModel> sensors;
    private TriggerModel trigger;
    private NotificationModel notification;

    public CreateAlertEvent(List<SensorModel> sensors, TriggerModel trigger,
            NotificationModel notification) {
        super(AlertCreateEvents.CreateAlertRequest);
        setSensors(sensors);
        setTrigger(trigger);
        setNotification(notification);
    }

    public NotificationModel getNotification() {
        return notification;
    }

    public List<SensorModel> getSensors() {
        return sensors;
    }

    public TriggerModel getTrigger() {
        return trigger;
    }

    public void setNotification(NotificationModel notification) {
        this.notification = notification;
    }

    private void setSensors(List<SensorModel> sensors) {
        this.sensors = sensors;
    }

    public void setTrigger(TriggerModel trigger) {
        this.trigger = trigger;
    }
}
