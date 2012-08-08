package nl.sense_os.commonsense.main.client.alerts.create;

import java.util.List;

import nl.sense_os.commonsense.main.client.ext.model.ExtNotification;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtTrigger;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class CreateAlertEvent extends AppEvent {

    private List<ExtSensor> sensors;
    private ExtTrigger trigger;
    private ExtNotification notification;

    public CreateAlertEvent(List<ExtSensor> sensors, ExtTrigger trigger,
            ExtNotification notification) {
        super(AlertCreateEvents.CreateAlertRequest);
        setSensors(sensors);
        setTrigger(trigger);
        setNotification(notification);
    }

    public ExtNotification getNotification() {
        return notification;
    }

    public List<ExtSensor> getSensors() {
        return sensors;
    }

    public ExtTrigger getTrigger() {
        return trigger;
    }

    public void setNotification(ExtNotification notification) {
        this.notification = notification;
    }

    private void setSensors(List<ExtSensor> sensors) {
        this.sensors = sensors;
    }

    public void setTrigger(ExtTrigger trigger) {
        this.trigger = trigger;
    }
}
