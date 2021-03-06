package nl.sense_os.commonsense.main.client.sensors.unshare;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient.Urls;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

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

public class UnshareController extends Controller {

	private static final Logger LOG = Logger.getLogger(UnshareController.class.getName());
	private View dialog;

	public UnshareController() {
		// LOG.setLevel(Level.ALL);
		registerEventTypes(UnshareEvents.ShowUnshareDialog);
		registerEventTypes(UnshareEvents.UnshareRequest, UnshareEvents.UnshareComplete);
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		if (type.equals(UnshareEvents.UnshareRequest)) {
			LOG.finest("UnshareRequest");
			ExtSensor sensor = event.getData("sensor");
			List<ExtUser> users = event.getData("users");
			onUnshareRequest(sensor, users);

		} else

		/*
		 * Pass through to dialog
		 */
		{
			forwardToView(dialog, event);
		}

	}

	@Override
	protected void initialize() {
		dialog = new UnshareDialog(this);
		super.initialize();
	}

	private void onUnshareComplete(ExtSensor sensor) {

		// update library
		List<ExtSensor> library = Registry
				.get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
		int index = library.indexOf(sensor);
		if (index != -1) {
			LOG.fine("Updating sensor's users in the library");
			library.get(index).setUsers(null);
			library.get(index).setUsers(sensor.getUsers());
		} else {
			LOG.warning("Cannot find the unshared sensor in the library!");
		}

		// dispatch event
		Dispatcher.forwardEvent(UnshareEvents.UnshareComplete);
	}

	private void onUnshareFailure(int statusCode) {
		forwardToView(dialog, new AppEvent(UnshareEvents.UnshareFailed));
	}

	private void onUnshareRequest(ExtSensor sensor, List<ExtUser> users) {
		unshare(sensor, users, 0);
	}

	private void onUnshareSuccess(String response, ExtSensor sensor, List<ExtUser> users, int index) {
		// update the sensor model
		List<ExtUser> sensorUsers = sensor.getUsers();
		sensorUsers.remove(users.get(index));
		sensor.setUsers(sensorUsers);

		// continue with the next user to remove
		index++;
		unshare(sensor, users, index);
	}

	private void unshare(final ExtSensor sensor, final List<ExtUser> users, final int index) {

		if (index < users.size()) {
			ExtUser user = users.get(index);

			ExtUser currentUser = Registry
					.<ExtUser> get(nl.sense_os.commonsense.common.client.util.Constants.REG_USER);
			if (currentUser.equals(user)) {
				LOG.finest("Skipped unsharing with the current user...");
				unshare(sensor, users, index + 1);
				return;
			}
			LOG.fine("Unsharing " + sensor + " with " + user);

			// prepare request properties
			final Method method = RequestBuilder.DELETE;
			final UrlBuilder urlBuilder = new UrlBuilder().setProtocol(
					CommonSenseClient.Urls.PROTOCOL).setHost(CommonSenseClient.Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/users/" + user.getId()
					+ ".json");
			final String url = urlBuilder.buildString();
			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("DELETE sensor user onError callback: " + exception.getMessage());
					onUnshareFailure(0);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("DELETE sensor user received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onUnshareSuccess(response.getText(), sensor, users, index);
					} else {
						LOG.warning("DELETE sensor user returned incorrect status: " + statusCode);
						onUnshareFailure(statusCode);
					}
				}
			};

			// send request
			try {
				RequestBuilder builder = new RequestBuilder(method, url);
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("DELETE sensor user request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			onUnshareComplete(sensor);
		}
	}
}
