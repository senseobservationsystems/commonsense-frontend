package nl.sense_os.commonsense.main.client.states.edit;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.ServiceMethodResponse;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtServiceMethod;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;

public class StateEditController extends Controller {
	private static final Logger LOG = Logger.getLogger(StateEditController.class.getName());
	private View editor;

	public StateEditController() {
		// LOG.setLevel(Level.ALL);
		registerEventTypes(StateEditEvents.ShowEditor);
		registerEventTypes(StateEditEvents.InvokeMethodRequested);
	}

	@Override
	public void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		/*
		 * Invoke a service method
		 */
		if (type.equals(StateEditEvents.InvokeMethodRequested)) {
			LOG.finest("InvokeMethodRequested");
			final ExtSensor stateSensor = event.<ExtSensor> getData("stateSensor");
			final ExtServiceMethod serviceMethod = event.<ExtServiceMethod> getData("method");
			final List<String> params = event.<List<String>> getData("parameters");
			invokeMethod(stateSensor, serviceMethod, params);

		} else

		/*
		 * Pass through to editor view
		 */
		{
			forwardToView(editor, event);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		editor = new StateEditor(this);
	}

	private void invokeMethod(ExtSensor stateSensor, ExtServiceMethod serviceMethod,
			List<String> params) {

		// get one of the state sensor children
		ExtSensor sensor = (ExtSensor) stateSensor.getChild(0);

		LOG.fine("State: " + stateSensor);
		LOG.fine("Sensor: " + sensor);
		LOG.fine("Method: " + serviceMethod);

		// prepare request properties
		final Method method = params.size() > 0 ? RequestBuilder.POST : RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/"
				+ stateSensor.getId() + "/" + serviceMethod.getName() + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// create request body
		String body = null;
		if (params.size() > 0) {
			body = "{\"parameters\":[";
			for (String p : params) {
				body += "\"" + p.replace("\"", "\\\"") + "\",";
			}
			body = body.substring(0, body.length() - 1);
			body += "]}";
		}

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("POST service method onError callback: " + exception.getMessage());
				onInvokeMethodFailure(0);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("POST service method response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onInvokeMethodSuccess(response.getText());
				} else {
					LOG.warning("POST service method returned incorrect status: " + statusCode);
					onInvokeMethodFailure(statusCode);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(body, reqCallback);
		} catch (Exception e) {
			LOG.warning("POST service method request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void onInvokeMethodFailure(int code) {
		forwardToView(editor, new AppEvent(StateEditEvents.InvokeMethodFailed));
	}

	private void onInvokeMethodSuccess(String response) {

		// parse result from the response
		String result = null;
		ServiceMethodResponse jso = ServiceMethodResponse.create(response).cast();
		if (null != jso) {
			result = jso.getResult();
		}

		if (result != null) {
			forwardToView(editor, new AppEvent(StateEditEvents.InvokeMethodComplete, result));
		} else {
			onInvokeMethodFailure(0);
		}
	}
}
