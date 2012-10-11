package nl.sense_os.commonsense.main.client.sensors.publish;

import java.util.List;

import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class PublishController extends Controller {

	private PublishConfirmDialog view;

	public PublishController() {
		registerEventTypes(PublishEvents.ShowPublisher, PublishEvents.PublishRequest,
				PublishEvents.DatasetUrlRequest);
	}

	/**
	 * Requests URL of the published data on the RODS website.
	 */
	private void getDatasetUrl(ExtUser user, boolean anonymous) {

		// prepare request data
		JSONObject json = new JSONObject();
		json.put("username", new JSONString(user.getUsername()));
		json.put("anonymous", JSONBoolean.getInstance(anonymous));
		String data = json.toString();

		// prepare callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				int code = -1;
				onGetDatasetUrlError(code, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				onGetDatasetUrlReponse(response);
			}
		};

		// prepare request details
		Method method = RequestBuilder.POST;
		String url = "/rod/cs.php";

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("Content-Type", "application/json");
			builder.setHeader("Accept", "application/json");
			builder.sendRequest(data, callback);
		} catch (Exception e) {
			callback.onError(null, e);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(PublishEvents.ShowPublisher)) {
			forwardToView(view, event);
		} else if (type.equals(PublishEvents.PublishRequest)) {
			ExtUser user = event.getData("user");
			List<ExtSensor> sensors = event.getData("sensors");
			boolean anonymous = event.getData("anonymous");
			publish(user, sensors, anonymous);
		} else if (type.equals(PublishEvents.DatasetUrlRequest)) {
			ExtUser user = event.getData("user");
			boolean anonymous = event.getData("anonymous");
			getDatasetUrl(user, anonymous);
		} else {
			// something wrong
		}
	}

	@Override
	protected void initialize() {
		view = new PublishConfirmDialog(this);
	}

	private void onGetDatasetUrlError(int code, Throwable error) {
		AppEvent event = new AppEvent(PublishEvents.DatasetUrlError);
		event.setData("code", code);
		event.setData("error", error);
		forwardToView(view, event);
	}

	private void onGetDatasetUrlReponse(Response response) {
		if (response.getStatusCode() == Response.SC_OK) {
			if (JsonUtils.safeToEval(response.getText())) {
				JSONObject jso = new JSONObject(JsonUtils.safeEval(response.getText()));
				JSONString url = jso.get("url").isString();
				JSONString title = jso.get("title").isString();
				if (null != url && null != title) {
					onGetDatasetUrlSuccess(url.stringValue(), title.stringValue());
				} else {
					onGetDatasetUrlError(-1,
							new Throwable("Unexpected response: " + response.getText()));
				}
			} else {
				onGetDatasetUrlError(-1,
						new Throwable("Unable to parse response: " + response.getText()));
			}
		} else {
			onGetDatasetUrlError(response.getStatusCode(), new Throwable(response.getStatusText()));
		}
	}

	private void onGetDatasetUrlSuccess(String url, String title) {
		AppEvent event = new AppEvent(PublishEvents.DatasetUrlSuccess);
		event.setData("url", url);
		forwardToView(view, event);
	}

	private void onPublicationError(int code, Throwable error) {
		AppEvent event = new AppEvent(PublishEvents.PublicationError);
		event.setData("code", code);
		event.setData("error", error);
		forwardToView(view, event);
	}

	private void onPublicationResponse(Response response) {
		if (response.getStatusCode() == Response.SC_OK) {
			if (JsonUtils.safeToEval(response.getText())) {
				JSONObject jso = new JSONObject(JsonUtils.safeEval(response.getText()));
				JSONString status = jso.get("status").isString();
				if (status != null && status.stringValue().equals("200")) {
					String url = jso.get("url").isString().stringValue();
					String title = jso.get("url").isString().stringValue();
					String name = jso.get("name").isString().stringValue();
					JSONArray sensorArray = jso.get("sensors").isArray();
					int[] sensorIds = new int[sensorArray.size()];
					for (int i = 0; i < sensorArray.size(); i++) {
						String s = sensorArray.get(i).isString().stringValue();
						sensorIds[i] = Integer.parseInt(s);
					}
					onPublicationSuccess(url, title, name, sensorIds);
				} else {
					onPublicationError(-1,
							new Throwable("Unexpected response: '" + response.getText() + "'"));
				}
			} else {
				onPublicationError(-1,
						new Throwable("Unable to parse response: '" + response.getText() + "'"));
			}
		} else {
			onPublicationError(response.getStatusCode(), new Throwable(response.getStatusText()));
		}
	}

	private void onPublicationSuccess(String url, String title, String name, int[] sensorIds) {
		AppEvent event = new AppEvent(PublishEvents.PublicationSuccess);
		event.setData("url", url);
		event.setData("title", title);
		event.setData("name", name);
		event.setData("sensorIds", sensorIds);
		forwardToView(view, event);
	}

	/**
	 * Sends request to publish list of sensors to the Rotterdam Open Data Store.
	 * 
	 * @param sensors
	 *            Sensors to publish
	 * @param anonymous
	 *            Boolean to select anonymous publication
	 */
	private void publish(ExtUser user, List<ExtSensor> sensors, boolean anonymous) {

		// prepare request data
		String uuid = user.getUuid();
		JSONObject json = new JSONObject();
		json.put("username", new JSONString(user.getUsername()));
		json.put("uuid", new JSONString(uuid));
		json.put("anonymous", JSONBoolean.getInstance(anonymous));
		JSONArray array = new JSONArray();
		for (int i = 0; i < sensors.size(); i++) {
			ExtSensor sensor = sensors.get(i);
			JSONObject sensorJson = new JSONObject();
			sensorJson.put("id", new JSONNumber(sensor.getId()));
			sensorJson.put("name", new JSONString(sensor.getName()));
			sensorJson.put("description", new JSONString(sensor.getDescription()));
			array.set(i, sensorJson);
		}
		json.put("sensors", array);
		String data = json.toString();

		// prepare callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				int code = -1;
				onPublicationError(code, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				onPublicationResponse(response);
			}
		};

		// prepare request details
		Method method = RequestBuilder.POST;
		String url = "/rod/cs.php";

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("Content-Type", "application/json");
			builder.setHeader("Accept", "application/json");
			builder.sendRequest(data, callback);
		} catch (Exception e) {
			callback.onError(null, e);
		}
	}
}
