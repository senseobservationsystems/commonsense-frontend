package nl.sense_os.commonsense.client.sensors.unshare;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.SensorModel;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class UnshareDialog extends View {

    private static final Logger LOGGER = Logger.getLogger(UnshareDialog.class.getName());
    private SensorModel sensor;

    public UnshareDialog(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(UnshareEvents.ShowUnshareDialog)) {
            LOGGER.finest("ShowUnshareDialog");
            SensorModel sensor = event.getData("sensor");
            showDialog(sensor);

        } else if (type.equals(UnshareEvents.UnshareComplete)) {
            LOGGER.finest("UnshareComplete");
            onUnshareComplete();

        } else if (type.equals(UnshareEvents.UnshareFailed)) {
            LOGGER.warning("UnshareFailed");
            onUnshareFailure();

        } else {
            LOGGER.warning("Unexpected event: " + event);
        }
    }

    private void onUnshareFailure() {
        MessageBox.confirm(null, "Failed to unshare the sensor! Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            submit();
                        }
                    }
                });
    }

    private void onUnshareComplete() {
        MessageBox.info(null, "The sensor has been unshared.", null);
    }

    private void showDialog(final SensorModel sensor) {
        this.sensor = sensor;
        MessageBox.confirm(null, "Are you sure you want to stop sharing this sensor?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            submit();
                        }
                    }
                });

    }

    private void submit() {
        AppEvent unshare = new AppEvent(UnshareEvents.UnshareRequest);
        unshare.setData("sensor", this.sensor);
        unshare.setData("users", this.sensor.getUsers());
        fireEvent(unshare);
    }
}
