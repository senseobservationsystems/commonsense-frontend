package nl.sense_os.commonsense.main.client.alerts.create;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtNotification;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtTrigger;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class CreateAlertEvent extends AppEvent {

    private List<GxtSensor> sensors;
    private GxtTrigger trigger;
    private GxtNotification notification;

    public CreateAlertEvent(List<GxtSensor> sensors, GxtTrigger trigger,
            GxtNotification notification) {
        super(AlertCreateEvents.CreateAlertRequest);
        setSensors(sensors);
        setTrigger(trigger);
        setNotification(notification);
    }

    public GxtNotification getNotification() {
        return notification;
    }

    public List<GxtSensor> getSensors() {
        return sensors;
    }

    public GxtTrigger getTrigger() {
        return trigger;
    }

    public void setNotification(GxtNotification notification) {
        this.notification = notification;
    }

    private void setSensors(List<GxtSensor> sensors) {
        this.sensors = sensors;
    }

    public void setTrigger(GxtTrigger trigger) {
        this.trigger = trigger;
    }
}
