package nl.sense_os.commonsense.main.client.alerts.create;

import java.util.logging.Logger;

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.model.apiclass.DataPoint;
import nl.sense_os.commonsense.lib.client.model.httpresponse.GetSensorDataResponse;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class AlertCreateController extends Controller {

	private Logger LOG = Logger.getLogger(AlertCreateController.class.getName());

	public AlertCreateController() {
		registerEventTypes(AlertCreateEvents.CreateAlertRequest, AlertCreateEvents.NewCreator);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(AlertCreateEvents.NewCreator)) {
			LOG.finest("Prepare new alert creator view");
			GxtSensor sensor = event.getData("sensor");
			prepareCreator(sensor);

		} else if (type.equals(AlertCreateEvents.CreateAlertRequest)) {
			LOG.finest("CreateAlertRequest");
			// View source = (View) event.getSource();
			// TODO

		} else {
			LOG.warning("Unexpected event: " + event);
		}
	}

	private void prepareCreator(final GxtSensor sensor) {

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET data onError callback: " + exception.getMessage());
				onLastDataPointFailure(sensor);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET data response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onLastDataPointSuccess(response.getText(), sensor);
				} else {
					LOG.warning("GET data returned incorrect status: " + statusCode);
					onLastDataPointFailure(sensor);
				}
			}
		};

        CommonSenseClient.getClient().getSensorData(reqCallback, sensor.getId(), null, null, null,
                null, null, null, null, true, null);
	}

	private void onLastDataPointSuccess(String response, GxtSensor sensor) {
		GetSensorDataResponse jso = GetSensorDataResponse.create(response);
        JsArray<DataPoint> data = jso.getData();
		if (data.length() > 0) {
            DataPoint dataPoint = data.get(0);
			long timestamp = Math.round(Double.parseDouble("" + dataPoint.getDate()) * 1000);

			AppEvent showCreator = new AppEvent(AlertCreateEvents.ShowCreator);
			showCreator.setData("sensor", sensor);
			showCreator.setData("timestamp", timestamp);
			forwardToView(new AlertCreatorView(this), showCreator);

		} else {
			onLastDataPointFailure(sensor);
		}
	}

	private void onLastDataPointFailure(GxtSensor sensor) {
		AppEvent showCreator = new AppEvent(AlertCreateEvents.ShowCreator);
		showCreator.setData("sensor", sensor);
		showCreator.setData("timestamp", -1l);
		forwardToView(new AlertCreatorView(this), showCreator);
	}
}
