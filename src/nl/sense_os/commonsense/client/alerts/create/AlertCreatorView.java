package nl.sense_os.commonsense.client.alerts.create;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.components.AlertCreator;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.viz.data.DataEvents;
import nl.sense_os.commonsense.client.viz.data.DataRequestEvent;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.JsArray;

public class AlertCreatorView extends View {

    class TriggerTypes {
        static final int NUMBER = 0;
        static final int STRING = 1;
        static final int POSITION = 2;
        static final int ERROR = -1;
    }

    private static final Logger LOG = Logger.getLogger(AlertCreatorView.class.getName());

    private AlertCreator creator;
    private SensorModel sensor;
    private JsArray<Timeseries> data;
    private int triggerType;

    public AlertCreatorView(Controller c) {
        super(c);
        LOG.setLevel(Level.ALL);
    }

    private void cancel() {
        creator.hide();
    }

    private void determineTriggerType(JsArray<Timeseries> data) {
        if (data.length() == 1) {
            // show different trigger creator screens for different data types
            String type = data.get(0).getType();
            if (type.equalsIgnoreCase("number")) {
                triggerType = TriggerTypes.NUMBER;

            } else if (type.equalsIgnoreCase("string")) {
                triggerType = TriggerTypes.STRING;

            } else {
                LOG.warning("Incompatible data type: " + type);
                triggerType = TriggerTypes.ERROR;
            }

        } else if (data.length() > 1) {
            // received complex JSON data

            // check if this is a location sensor
            boolean foundLat = false, foundLon = false;
            for (int i = 0; i < data.length(); i++) {
                Timeseries ts = data.get(i);
                if (ts.getLabel().contains("longitude")) {
                    foundLon = true;
                } else if (ts.getLabel().contains("latitude")) {
                    foundLat = true;
                }
            }
            // special trigger form for location sensors
            if (foundLat && foundLon) {
                triggerType = TriggerTypes.POSITION;

            } else {
                LOG.warning("Cannot create alert for JSON sensor with multiple fields");
                triggerType = TriggerTypes.ERROR;
            }

        } else {
            LOG.warning("No data received!");
            triggerType = TriggerTypes.ERROR;
        }
    }

    private void goToNext() {
        creator.showNotificationsForm();
    }

    private void goToPrev() {
        switch (triggerType) {
            case TriggerTypes.NUMBER :
                creator.showNumTriggerForm();
                break;
            case TriggerTypes.STRING :
                creator.showStringTriggerForm();
                break;
            case TriggerTypes.POSITION :
                creator.showPosTriggerForm();
                break;
            case TriggerTypes.ERROR :
                // fall through
            default :
                LOG.warning("Unexpected trigger type: " + triggerType);
                MessageBox.alert("Alert creator",
                        "Cannot create alerts for this sensor: incompatible data type.", null);
                return;
        }
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(AlertCreateEvents.ShowCreator)) {
            LOG.finest("ShowCreator");
            SensorModel sensor = event.getData("sensor");
            onShowRequest(sensor);

        } else if (type.equals(DataEvents.DataReceived)) {
            LOG.finest("DataReceived");
            JsArray<Timeseries> data = event.getData("data");
            onDataReceived(data);

        } else {
            LOG.warning("Unexpected event received: " + event);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        creator = new AlertCreator();

        creator.getNextButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                goToNext();
            }
        });
        creator.getBackButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                goToPrev();
            }
        });
        creator.getCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                cancel();
            }
        });
    }

    private void onDataReceived(JsArray<Timeseries> data) {
        this.data = data;
        determineTriggerType(data);
        showCreator();
    }

    private void onShowRequest(SensorModel sensor) {
        this.sensor = sensor;

        // send request for sensor data
        long end = System.currentTimeMillis();
        long start = System.currentTimeMillis() - 1000l * 60 * 60 * 24 * 7;
        List<SensorModel> sensors = Arrays.asList(sensor);
        boolean subsample = false;
        boolean showProgress = true;
        DataRequestEvent event = new DataRequestEvent(start, end, sensors, subsample, showProgress);
        event.setSource(this);
        Dispatcher.forwardEvent(event);
    }

    private void showCreator() {

        switch (triggerType) {
            case TriggerTypes.NUMBER :
                creator.show();
                creator.showNumTriggerForm();
                creator.onNewNumData(data);
                break;
            case TriggerTypes.STRING :
                creator.show();
                creator.showStringTriggerForm();
                creator.onNewStringData(data);
                break;
            case TriggerTypes.POSITION :
                creator.show();
                creator.showPosTriggerForm();
                creator.onNewPosData(data);
                break;
            case TriggerTypes.ERROR :
                // fall through
            default :
                LOG.warning("Unexpected trigger type: " + triggerType);
                MessageBox.alert("Alert creator",
                        "Cannot create alerts for this sensor: incompatible data type.", null);
                return;
        }
    }
}
