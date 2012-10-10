package nl.sense_os.commonsense.main.client.sensors.publish;

import java.util.List;

import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

import com.extjs.gxt.ui.client.Registry;
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

	private PublishConfirmDialog confirmView;

	public PublishController() {
		registerEventTypes(PublishEvents.ShowPublisher, PublishEvents.PublishRequest);
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(PublishEvents.ShowPublisher)) {
			forwardToView(confirmView, event);
		} else if (type.equals(PublishEvents.PublishRequest)) {
			List<ExtSensor> sensors = event.getData("sensors");
			boolean anomymous = event.getData("anonymous");
			publish(sensors, anomymous);
		} else {
			// something wrong
		}
	}

	private void publish(List<ExtSensor> sensors, boolean anonymous) {

		// prepare request data
		ExtUser user = Registry.get(Constants.REG_USER);
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
		String requestData = json.toString();

		// prepare callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {
				onPublicationResponse(response);
			}

			@Override
			public void onError(Request request, Throwable exception) {
				int code = -1;
				onPublicationError(code, exception);
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
			builder.sendRequest(requestData, callback);
		} catch (Exception e) {
			callback.onError(null, e);
		}
	}

	private void onPublicationResponse(Response response) {
		if (response.getStatusCode() == Response.SC_OK) {
			JSONObject jso = new JSONObject(JsonUtils.safeEval(response.getText()));
			JSONString status = jso.get("status").isString();
			if (status.stringValue().equals("201")) {
				onPublicationSuccess(response.getText());
			} else {
				onPublicationError(-1, new Throwable("Incorrect response: '" + response.getText()
						+ "'"));
			}
		} else {
			onPublicationError(response.getStatusCode(), new Throwable(response.getStatusText()));
		}
	}

	private void onPublicationSuccess(String text) {
		if (JsonUtils.safeToEval(text)) {
			getResourceLink();
		} else {
			onPublicationError(-1, new Throwable("Could not parse response: '" + text + "'"));
		}
	}

	private void getResourceLink() {
		AppEvent event = new AppEvent(PublishEvents.PublicationComplete);
		event.setData("url", "url");
		forwardToView(confirmView, event);
	}

	private void onPublicationError(int code, Throwable error) {
		AppEvent event = new AppEvent(PublishEvents.PublicationError);
		event.setData("code", code);
		event.setData("error", error);
		forwardToView(confirmView, event);
	}

	@Override
	protected void initialize() {
		confirmView = new PublishConfirmDialog(this);
	}
}
