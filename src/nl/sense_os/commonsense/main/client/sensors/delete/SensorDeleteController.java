package nl.sense_os.commonsense.main.client.sensors.delete;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.model.ExtSensor;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class SensorDeleteController extends Controller {

	private final static Logger LOG = Logger.getLogger(SensorDeleteController.class.getName());
	private View deleteDialog;

	public SensorDeleteController() {
		registerEventTypes(SensorDeleteEvents.ShowDeleteDialog, SensorDeleteEvents.DeleteRequest,
				SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
	}

	/**
	 * Deletes a list of sensors, using Ajax requests to CommonSense.
	 * 
	 * @param sensors
	 *            The list of sensors that have to be deleted.
	 * @param index
	 *            List index of the current sensor to be deleted.
	 * @param retryCount
	 *            Counter for failed requests that were retried.
	 */
	private void delete(final List<ExtSensor> sensors, final int index, final int retryCount) {

		if (index < sensors.size()) {
			ExtSensor sensor = sensors.get(index);

			// prepare request properties
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + ".json");
			final String url = urlBuilder.buildString();
			final Method method = RequestBuilder.DELETE;
			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("DELETE sensor onError callback: " + exception.getMessage());
					onDeleteFailure(sensors, index, retryCount);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("DELETE sensor response received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onDeleteSuccess(sensors, index);
					} else {
						LOG.warning("DELETE sensor returned incorrect status: " + statusCode);
						onDeleteFailure(sensors, index, retryCount);
					}
				}
			};

			// send request
			try {
				RequestBuilder builder = new RequestBuilder(method, url);
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("DELETE sensor request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			// done!
			onDeleteComplete();
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		if (type.equals(SensorDeleteEvents.DeleteRequest)) {
			LOG.fine("DeleteRequest");
			final List<ExtSensor> sensors = event.<List<ExtSensor>> getData("sensors");
			delete(sensors, 0, 0);

		} else

		/*
		 * Pass through to View
		 */
		{
			forwardToView(this.deleteDialog, event);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		this.deleteDialog = new SensorDeleteDialog(this);
	}

	/**
	 * Handles a failed delete request. Retries the request up to three times, after this it gives
	 * up and dispatches {@link SensorsEvents#DeleteFailure}.
	 * 
	 * @param sensors
	 *            List of sensors that have to be deleted.
	 * @param retryCount
	 *            Number of times this request was attempted.
	 */
	private void onDeleteFailure(List<ExtSensor> sensors, int index, int retryCount) {

		if (retryCount < 3) {
			// retry
			retryCount++;
			delete(sensors, index, retryCount);
		} else {
			// give up
			Dispatcher.forwardEvent(SensorDeleteEvents.DeleteFailure);
		}
	}

	/**
	 * Handles a successful delete request. Removes the deleted sensor from the list, and calls back
	 * to {@link #delete(List, int)}.
	 * 
	 * @param sensors
	 *            List of sensors that have to be deleted.
	 */
	private void onDeleteSuccess(List<ExtSensor> sensors, int index) {

		// remove the sensor from the cached library
		boolean removed = Registry.<List<ExtSensor>> get(Constants.REG_SENSOR_LIST).remove(
				sensors.get(index));
		if (!removed) {
			LOG.warning("Failed to remove the sensor from the library!");
		}

		// continue with the rest of the list
		index++;
		delete(sensors, index, 0);
	}

	private void onDeleteComplete() {
		Dispatcher.forwardEvent(SensorDeleteEvents.DeleteSuccess);
	}
}
