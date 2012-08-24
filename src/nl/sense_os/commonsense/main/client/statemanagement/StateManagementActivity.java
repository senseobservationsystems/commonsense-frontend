/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.sense_os.commonsense.main.client.statemanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.SessionManager;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetMethodsResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetSensorsResponse;
import nl.sense_os.commonsense.common.client.constant.Urls;
import nl.sense_os.commonsense.common.client.model.Sensor;
import nl.sense_os.commonsense.common.client.model.ServiceMethod;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtServiceMethod;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
import nl.sense_os.commonsense.main.client.ext.util.TreeCopier;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a container Widget.
 */
public class StateManagementActivity extends AbstractActivity implements
		StateManagementView.Presenter {

	private static final Logger LOG = Logger.getLogger(StateManagementActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private MainClientFactory clientFactory;
	private StateManagementView view;

	public StateManagementActivity(StateManagementPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	private void disconnectService(ExtSensor sensor, ExtSensor stateSensor) {

		// TODO this needs to be handled by separate view/controller

		// prepare request data
		final Method method = RequestBuilder.DELETE;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/"
				+ stateSensor.getId() + ".json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("DELETE service onError callback: " + exception.getMessage());
				onDisconnectFailure(0);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("DELETE service response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onDisconnectSuccess(response.getText());
				} else {
					LOG.warning("DELETE service returned incorrect status: " + statusCode);
					onDisconnectFailure(statusCode);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("DELETE service request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void getConnected(final ExtSensor state, final AsyncCallback<List<ExtSensor>> callback) {

		// prepare request properties
		final Method method = RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + state.getId() + "/sensors.json");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET service sensors onError callback: " + exception.getMessage());
				onConnectedFailure(callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET service sensors response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onConnectedSuccess(response.getText(), state, callback);
				} else {
					LOG.warning("GET service sensors returned incorrect status: " + statusCode);
					onConnectedFailure(callback);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET service sensors request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	private void getMethods(final ExtSensor state, final List<ExtSensor> sensors) {

		if (sensors.size() > 0) {
			// prepare request properties
			final Method method = RequestBuilder.GET;
			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensors.get(0).getId() + "/services/"
					+ state.getId() + "/methods.json");
			final String url = urlBuilder.buildString();
			final String sessionId = SessionManager.getSessionId();

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET service methods onError callback: " + exception.getMessage());
					onMethodsFailure(0);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET service methods response received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onMethodsSuccess(response.getText(), state, sensors);
					} else {
						LOG.warning("GET service methods returned incorrect status: " + statusCode);
						onMethodsFailure(statusCode);
					}
				}
			};

			// send request
			try {
				RequestBuilder builder = new RequestBuilder(method, url);
				builder.setHeader("X-SESSION_ID", sessionId);
				builder.sendRequest(null, reqCallback);
			} catch (Exception e) {
				LOG.warning("GET service methods request threw exception: " + e.getMessage());
				reqCallback.onError(null, e);
			}

		} else {
			LOG.warning("State \'" + state + "\' has no connected sensors!");
		}
	}

	private void getStateSensors(final AsyncCallback<List<ExtSensor>> callback) {

		// prepare request properties
		final Method method = RequestBuilder.GET;
		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
		urlBuilder.setPath(Urls.PATH_SENSORS + ".json");
		urlBuilder.setParameter("per_page", "1000");
		urlBuilder.setParameter("details", "full");
		final String url = urlBuilder.buildString();
		final String sessionId = SessionManager.getSessionId();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET sensors onError callback: " + exception.getMessage());
				onStateSensorsFailure(callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET sensors response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onStateSensorsSuccess(response.getText(), callback);
				} else {
					LOG.warning("GET sensors returned incorrect status: " + statusCode);
					onStateSensorsFailure(callback);
				}
			}
		};

		// send request
		try {
			RequestBuilder builder = new RequestBuilder(method, url);
			builder.setHeader("X-SESSION_ID", sessionId);
			builder.sendRequest(null, reqCallback);
		} catch (Exception e) {
			LOG.warning("GET sensors request threw exception: " + e.getMessage());
			reqCallback.onError(null, e);
		}
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
		LOG.info("Start 'statemanagement' activity");

		view = clientFactory.getStateManagementView();
		view.setPresenter(this);

		LayoutContainer parent = clientFactory.getMainView().getActivityPanelGxt();
		parent.removeAll();
		parent.add(view.asWidget());
		parent.layout();

		view.refreshLoader(false);
	}

	@Override
	public void loadData(AsyncCallback<List<ExtSensor>> callback, Object loadConfig) {

		view.setBusy(true);

		if (null == loadConfig) {
			getStateSensors(callback);
		} else if (loadConfig instanceof ExtSensor && ((ExtSensor) loadConfig).getType() == 2) {
			getConnected((ExtSensor) loadConfig, callback);
		} else {
			onLoadComplete(new ArrayList<ExtSensor>(), callback);
		}
	}

	private void onConnectedFailure(AsyncCallback<List<ExtSensor>> callback) {

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onConnectedSuccess(String response, ExtSensor state,
			AsyncCallback<List<ExtSensor>> callback) {

		// parse list of sensors from response
		List<ExtSensor> sensors = new ArrayList<ExtSensor>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			JsArray<Sensor> rawSensors = responseJso.getRawSensors();
			for (int i = 0; i < rawSensors.length(); i++) {
				ExtSensor sensor = new ExtSensor(rawSensors.get(i));
				sensors.add(sensor);
			}
		}

		// get details from library
		List<ExtSensor> result = new ArrayList<ExtSensor>();
		List<ExtSensor> library = Registry
				.<List<ExtSensor>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
		for (ExtSensor sensor : sensors) {
			int index = -1;
			for (ExtSensor libSensor : library) {
				if (libSensor.getId() == sensor.getId()) {
					index = library.indexOf(libSensor);
					break;
				}
			}
			if (index != -1) {
				ExtSensor detailed = (ExtSensor) TreeCopier.copySensor(library.get(index));
				state.add(detailed);
				result.add(detailed);
			} else {
				sensor.setParent(state);
				result.add(sensor);
			}
		}

		// return to view
		onLoadComplete(result, callback);

		// continue getting methods
		getMethods(state, result);
	}

	private void onDisconnectFailure(int code) {
		view.onDisconnectFailure();
	}

	private void onDisconnectSuccess(String response) {
		view.onListUpdate();
	}

	private void onLoadComplete(List<ExtSensor> result, AsyncCallback<List<ExtSensor>> callback) {

		view.setBusy(false);

		if (null != callback) {
			callback.onSuccess(result);
		}
	}

	private void onLoadFailure(AsyncCallback<List<ExtSensor>> callback) {

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onMethodsFailure(int statusCode) {
		// TODO
	}

	private void onMethodsSuccess(String response, ExtSensor state, List<ExtSensor> sensors) {

		// parse list of methods from the response
		JsArray<ServiceMethod> methods = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetMethodsResponse jso = JsonUtils.unsafeEval(response);
			methods = jso.getRawMethods();
		}

		if (null != methods) {
			List<ExtServiceMethod> extMethods = new ArrayList<ExtServiceMethod>(methods.length());
			for (int i = 0; i < methods.length(); i++) {
				extMethods.add(new ExtServiceMethod(methods.get(i)));
			}
			state.set("methods", extMethods);
		}
	}

	private void onStateSensorsFailure(AsyncCallback<List<ExtSensor>> callback) {
		onLoadFailure(callback);
	}

	private void onStateSensorsSuccess(String response, AsyncCallback<List<ExtSensor>> callback) {

		// parse list of sensors from response
		JsArray<Sensor> sensors = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			sensors = responseJso.getRawSensors();
		}

		ExtUser user = Registry
				.<ExtUser> get(nl.sense_os.commonsense.common.client.util.Constants.REG_USER);
		List<ExtSensor> states = new ArrayList<ExtSensor>();
		for (int i = 0; i < sensors.length(); i++) {
			ExtSensor sensor = new ExtSensor(sensors.get(i));
			if (sensor.getType() == 2 && user.equals(sensor.getOwner())) {
				states.add(sensor);
			}
		}

		onLoadComplete(states, callback);
	}
}
