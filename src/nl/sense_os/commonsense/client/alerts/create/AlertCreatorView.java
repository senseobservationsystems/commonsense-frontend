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
import com.google.gwt.core.client.JsArray;

public class AlertCreatorView extends View {

    private static final Logger LOG = Logger.getLogger(AlertCreatorView.class.getName());

    private AlertCreator creator;
    private SensorModel sensor;

    public AlertCreatorView(Controller c) {
        super(c);
        LOG.setLevel(Level.ALL);
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

    private void onDataReceived(JsArray<Timeseries> data) {
        creator.showNumTriggerForm();
        showCreator();
        creator.onNewNumData(data);
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

    private void cancel() {
        creator.hide();
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

        if (sensor.getDataType().equals("string")) {
            creator.showStringTriggerForm();

        } else if (sensor.getDataType().equals("float") || sensor.getDataType().equals("int")) {
            creator.showNumTriggerForm();

        } else if (sensor.getDataType().contains("position")) {
            creator.showPosTriggerForm();

        } else {
            LOG.warning("Unexpected data type: " + sensor.getDataType());
        }

        creator.show();
    }

    private void goToNext() {
        creator.showNotificationsForm();
    }

    private void goToPrev() {
        // TODO
        creator.showNumTriggerForm();
    }
}
