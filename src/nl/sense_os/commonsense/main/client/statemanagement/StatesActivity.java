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

import nl.sense_os.commonsense.lib.client.communication.CommonSenseClient;
import nl.sense_os.commonsense.lib.client.model.apiclass.Sensor;
import nl.sense_os.commonsense.lib.client.model.apiclass.ServiceMethod;
import nl.sense_os.commonsense.lib.client.model.httpresponse.GetMethodsResponse;
import nl.sense_os.commonsense.lib.client.model.httpresponse.GetSensorsResponse;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtServiceMethod;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.gxt.util.TreeCopier;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a container Widget.
 */
public class StatesActivity extends AbstractActivity implements
		StateListView.Presenter {

	private static final Logger LOG = Logger.getLogger(StatesActivity.class.getName());

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private MainClientFactory clientFactory;
	private StateListView view;

	public StatesActivity(StatesPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void disconnectService(GxtSensor sensor, GxtSensor stateSensor) {

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onDisconnectFailure(-1, exception);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onDisconnectSuccess(response.getText());
				} else {
					onDisconnectFailure(statusCode, new Throwable(response.getStatusText()));
				}
			}
		};

        CommonSenseClient.getClient().disconnectService(reqCallback, sensor.getId(),
                stateSensor.getId());
	}

	private void getConnected(final GxtSensor state, final AsyncCallback<List<GxtSensor>> callback) {

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onConnectedFailure(-1, exception, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onConnectedSuccess(response.getText(), state, callback);
				} else {
					onConnectedFailure(statusCode, new Throwable(response.getStatusText()),
							callback);
				}
			}
		};

        CommonSenseClient.getClient().getConnectedSensors(reqCallback, state.getId());
	}

	private void getMethods(final GxtSensor state, final List<GxtSensor> sensors) {

		if (sensors.size() > 0) {

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					onMethodsFailure(-1, exception);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET service methods response received: " + response.getStatusText());
					int statusCode = response.getStatusCode();
					if (Response.SC_OK == statusCode) {
						onMethodsSuccess(response.getText(), state, sensors);
					} else {
						onMethodsFailure(statusCode, new Throwable(response.getStatusText()));
					}
				}
			};

            CommonSenseClient.getClient().getServiceMethods(reqCallback, sensors.get(0).getId(),
                    state.getId());

		} else {
			LOG.warning("State \'" + state + "\' has no connected sensors!");
		}
	}

	private void getStateSensors(final AsyncCallback<List<GxtSensor>> callback) {

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onStateSensorsFailure(-1, exception, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onStateSensorsSuccess(response.getText(), callback);
				} else {
					onStateSensorsFailure(response.getStatusCode(),
							new Throwable(response.getStatusText()), callback);
				}
			}
		};

        CommonSenseClient.getClient().getSensors(reqCallback, 1000, null, null, null, null,
                "full", null);
	}

	@Override
	public void loadData(AsyncCallback<List<GxtSensor>> callback, Object loadConfig) {

		view.setBusy(true);

		if (null == loadConfig) {
			getStateSensors(callback);
		} else if (loadConfig instanceof GxtSensor && ((GxtSensor) loadConfig).getType() == 2) {
			getConnected((GxtSensor) loadConfig, callback);
		} else {
			onLoadComplete(new ArrayList<GxtSensor>(), callback);
		}
	}

	private void onConnectedFailure(int code, Throwable error,
			AsyncCallback<List<GxtSensor>> callback) {

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onConnectedSuccess(String response, GxtSensor state,
			AsyncCallback<List<GxtSensor>> callback) {

		// parse list of sensors from response
		List<GxtSensor> sensors = new ArrayList<GxtSensor>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			JsArray<Sensor> rawSensors = responseJso.getRawSensors();
			for (int i = 0; i < rawSensors.length(); i++) {
				GxtSensor sensor = new GxtSensor(rawSensors.get(i));
				sensors.add(sensor);
			}
		}

		// get details from library
		List<GxtSensor> result = new ArrayList<GxtSensor>();
		List<GxtSensor> library = Registry.<List<GxtSensor>> get(Constants.REG_SENSOR_LIST);
		for (GxtSensor sensor : sensors) {
			int index = -1;
			for (GxtSensor libSensor : library) {
				if (libSensor.getId() == sensor.getId()) {
					index = library.indexOf(libSensor);
					break;
				}
			}
			if (index != -1) {
				GxtSensor detailed = (GxtSensor) TreeCopier.copySensor(library.get(index));
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

	private void onDisconnectFailure(int code, Throwable error) {
		LOG.warning("Failed to disconnect service! Code: " + code + " " + error.getMessage());
		view.onDisconnectFailure();
	}

	private void onDisconnectSuccess(String response) {
		view.onListUpdate();
	}

	private void onLoadComplete(List<GxtSensor> result, AsyncCallback<List<GxtSensor>> callback) {

		view.setBusy(false);

		if (null != callback) {
			callback.onSuccess(result);
		}
	}

	private void onLoadFailure(AsyncCallback<List<GxtSensor>> callback) {

		view.setBusy(false);

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onMethodsFailure(int code, Throwable error) {
		LOG.warning("Failed to get service methods! Code: " + code + " " + error.getMessage());
		// TODO notify the user that something is wrong
	}

	private void onMethodsSuccess(String response, GxtSensor state, List<GxtSensor> sensors) {

		// parse list of methods from the response
		JsArray<ServiceMethod> methods = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetMethodsResponse jso = JsonUtils.unsafeEval(response);
			methods = jso.getRawMethods();
		}

		if (null != methods) {
			List<GxtServiceMethod> extMethods = new ArrayList<GxtServiceMethod>(methods.length());
			for (int i = 0; i < methods.length(); i++) {
				extMethods.add(new GxtServiceMethod(methods.get(i)));
			}
			state.set("methods", extMethods);
		}
	}

	private void onStateSensorsFailure(int code, Throwable error,
			AsyncCallback<List<GxtSensor>> callback) {
		LOG.warning("Failed to get state sensors! Code: " + code + " " + error.getMessage());
		onLoadFailure(callback);
	}

	private void onStateSensorsSuccess(String response, AsyncCallback<List<GxtSensor>> callback) {

		// parse list of sensors from response
		JsArray<Sensor> sensors = null;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			sensors = responseJso.getRawSensors();
		}

		GxtUser user = Registry.<GxtUser> get(Constants.REG_USER);
		List<GxtSensor> states = new ArrayList<GxtSensor>();
		for (int i = 0; i < sensors.length(); i++) {
			GxtSensor sensor = new GxtSensor(sensors.get(i));
			if (sensor.getType() == 2 && user.equals(sensor.getOwner())) {
				states.add(sensor);
			}
		}

		onLoadComplete(states, callback);
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
		LOG.info("Start 'statemanagement' activity");

		view = clientFactory.getStateListView();
		view.setPresenter(this);

		LayoutContainer parent = clientFactory.getMainView().getGxtActivityPanel();
		parent.removeAll();
		parent.add(view.asWidget());
		parent.layout();

		view.refreshLoader(false);
	}
}
