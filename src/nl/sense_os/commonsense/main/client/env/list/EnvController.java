package nl.sense_os.commonsense.main.client.env.list;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetEnvironmentsResponse;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.model.Environment;
import nl.sense_os.commonsense.main.client.env.create.EnvCreateEvents;
import nl.sense_os.commonsense.main.client.ext.model.ExtEnvironment;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnvController extends Controller {

	private static final Logger LOG = Logger.getLogger(EnvController.class.getName());
	private View grid;

	public EnvController() {

		// LOG.setLevel(Level.ALL);

		// events to update the list of groups
		registerEventTypes(VizEvents.Show);
		registerEventTypes(EnvEvents.ShowGrid);

		registerEventTypes(EnvEvents.ListRequested, EnvEvents.ListUpdated);
		registerEventTypes(EnvEvents.DeleteRequest, EnvEvents.DeleteSuccess);
		registerEventTypes(EnvCreateEvents.CreateSuccess);
	}

	private void delete(final ExtEnvironment environment) {

		// prepare request properties
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_ENV + "/" + environment.getId() + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("DELETE environment onError callback: " + exception.getMessage());
				onDeleteFailure();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("DELETE environment response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onDeleteSuccess(environment);
				} else {
					LOG.warning("DELETE environment returned incorrect status: " + statusCode);
					onDeleteFailure();
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, callback);
		} catch (Exception e) {
			LOG.warning("DELETE environment request threw exception: " + e.getMessage());
			callback.onError(null, e);
		}
	}

	@Override
	public void handleEvent(AppEvent event) {
		EventType type = event.getType();

		if (type.equals(EnvEvents.ListRequested)) {
			LOG.fine("LoadRequest");
			final AsyncCallback<List<ExtEnvironment>> callback = event
					.<AsyncCallback<List<ExtEnvironment>>> getData();
			requestList(callback);

		} else

		/*
		 * Delete request
		 */
		if (type.equals(EnvEvents.DeleteRequest)) {
			LOG.fine("DeleteRequest");
			final ExtEnvironment environment = event.getData("environment");
			delete(environment);

		} else

		{
			forwardToView(grid, event);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		grid = new EnvGrid(this);
		Registry.register(
				nl.sense_os.commonsense.common.client.util.Constants.REG_ENVIRONMENT_LIST,
				new ArrayList<ExtEnvironment>());
	}

	private void onDeleteFailure() {
		forwardToView(grid, new AppEvent(EnvEvents.DeleteFailure));
	}

	private void onDeleteSuccess(ExtEnvironment environment) {

		// update sensor library
		List<ExtSensor> library = Registry
				.<List<ExtSensor>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
		for (ExtSensor sensor : library) {
			if (sensor.getEnvironment() != null && sensor.getEnvironment().equals(environment)) {
				sensor.setEnvironment(null);
			}
		}

		// update global environment list
		Registry.<List<ExtEnvironment>> get(
				nl.sense_os.commonsense.common.client.util.Constants.REG_ENVIRONMENT_LIST).remove(
				environment);

		Dispatcher.forwardEvent(EnvEvents.DeleteSuccess);
	}

	private void onListFailure(AsyncCallback<List<ExtEnvironment>> callback) {
		forwardToView(grid, new AppEvent(EnvEvents.Done));
		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onListSuccess(String response, AsyncCallback<List<ExtEnvironment>> callback) {

		// parse the list of environments from the response
		List<Environment> environments = new ArrayList<Environment>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetEnvironmentsResponse jso = JsonUtils.unsafeEval(response);
			environments = jso.getEnvironments();
		}

		List<ExtEnvironment> extEnvironments = new ArrayList<ExtEnvironment>();
		for (Environment e : environments) {
			extEnvironments.add(new ExtEnvironment(e));
		}

		Registry.<List<ExtEnvironment>> get(
				nl.sense_os.commonsense.common.client.util.Constants.REG_ENVIRONMENT_LIST).addAll(
				extEnvironments);

		forwardToView(grid, new AppEvent(EnvEvents.Done));
		Dispatcher.forwardEvent(EnvEvents.ListUpdated);
		if (null != callback) {
			callback.onSuccess(extEnvironments);
		}
	}

	private void requestList(final AsyncCallback<List<ExtEnvironment>> callback) {

		forwardToView(grid, new AppEvent(EnvEvents.Working));
		Registry.<List<ExtEnvironment>> get(
				nl.sense_os.commonsense.common.client.util.Constants.REG_ENVIRONMENT_LIST).clear();

		// prepare request properties
		final String url = new UrlBuilder().setHost(Urls.HOST).setPath(Urls.PATH_ENV + ".json")
				.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET environments onError callback: " + exception.getMessage());
				onListFailure(callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET environments response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onListSuccess(response.getText(), callback);
				} else if (Response.SC_NO_CONTENT == statusCode) {
					onListSuccess(null, callback);
				} else {
					LOG.warning("GET environments returned incorrect status: " + statusCode);
					onListFailure(callback);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET environments request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	// private void xhrDelete(EnvironmentModel environment) {
	//
	// // prepare request properties
	// final String method = "DELETE";
	// final String url = Urls.ENVIRONMENTS + "/" + environment.getId() + ".json";
	// final String sessionId = SessionManager.getSessionId();
	// final AppEvent onSuccess = new AppEvent(EnvEvents.DeleteAjaxSuccess);
	// onSuccess.setData("environment", environment);
	// final AppEvent onFailure = new AppEvent(EnvEvents.DeleteAjaxSuccess);
	//
	// // send request to AjaxController
	// final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
	// ajaxRequest.setData("method", method);
	// ajaxRequest.setData("url", url);
	// ajaxRequest.setData("session_id", sessionId);
	// ajaxRequest.setData("onSuccess", onSuccess);
	// ajaxRequest.setData("onFailure", onFailure);
	//
	// Dispatcher.forwardEvent(ajaxRequest);
	// }

	// private void xhrRequestList(AsyncCallback<List<EnvironmentModel>> callback) {
	//
	// forwardToView(grid, new AppEvent(EnvEvents.Working));
	// Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).clear();
	//
	// // prepare request properties
	// final String method = "GET";
	// final String url = Urls.ENVIRONMENTS + ".json";
	// final String sessionId = SessionManager.getSessionId();
	// final AppEvent onSuccess = new AppEvent(EnvEvents.ListAjaxSuccess);
	// onSuccess.setData("callback", callback);
	// final AppEvent onFailure = new AppEvent(EnvEvents.ListAjaxFailure);
	// onFailure.setData("callback", callback);
	//
	// // send request to AjaxController
	// final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
	// ajaxRequest.setData("method", method);
	// ajaxRequest.setData("url", url);
	// ajaxRequest.setData("session_id", sessionId);
	// ajaxRequest.setData("onSuccess", onSuccess);
	// ajaxRequest.setData("onFailure", onFailure);
	//
	// Dispatcher.forwardEvent(ajaxRequest);
	// }
}
