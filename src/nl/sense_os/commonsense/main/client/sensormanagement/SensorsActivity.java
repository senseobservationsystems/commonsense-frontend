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
package nl.sense_os.commonsense.main.client.sensormanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
import nl.sense_os.commonsense.common.client.communication.httpresponse.AvailServicesResponseEntry;
import nl.sense_os.commonsense.common.client.communication.httpresponse.BatchAvailServicesResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetGroupsResponse;
import nl.sense_os.commonsense.common.client.communication.httpresponse.GetSensorsResponse;
import nl.sense_os.commonsense.common.client.model.Group;
import nl.sense_os.commonsense.common.client.model.Sensor;
import nl.sense_os.commonsense.common.client.model.Service;
import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.MainEntryPoint;
import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtEnvironment;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtService;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
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
public class SensorsActivity extends AbstractActivity implements SensorListView.Presenter {
	private static final Logger LOG = Logger.getLogger(SensorsActivity.class.getName());
	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private MainClientFactory clientFactory;
	private boolean isLoadingList;
	private SensorListView view;
	private static final int PER_PAGE = 1000;
	private boolean isLoadingUsers;
	private boolean isLoadingServices;

	public SensorsActivity(SensorsPlace place, MainClientFactory clientFactory) {
		this.clientFactory = clientFactory;

		// initialize library and lists of devices and environments
		if (null == Registry.get(Constants.REG_SENSOR_LIST)) {
			Registry.register(Constants.REG_SENSOR_LIST, new ArrayList<GxtSensor>());
		}
		if (null == Registry.get(Constants.REG_DEVICE_LIST)) {
			Registry.register(Constants.REG_DEVICE_LIST, new ArrayList<GxtDevice>());
		}
	}

	private List<GxtDevice> devicesFromLibrary(List<GxtSensor> library) {
		LOG.finest("Listing devices...");
		List<GxtDevice> devices = new ArrayList<GxtDevice>();

		// gather the devices of all sensors in the library
		GxtDevice device;
		for (GxtSensor sensor : library) {
			device = sensor.getDevice();
			if (device != null && !devices.contains(device)) {
				devices.add(device);
				LOG.fine("Device: " + device);
			}
		}

		return devices;
	}

	/**
	 * Requests a list of all available services for all sensors the user owns.
	 * 
	 * @param groupId
	 *            Optional parameter to get the available services for sensors that are not shared
	 *            directly with the user but with a group.
	 */
	private void getAvailableServices(final int page, final String groupId) {

		isLoadingServices = true;
		notifyState();

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET available services error callback: " + exception.getMessage());
				onAvailServicesFailure();
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET available services response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onAvailServicesSuccess(response.getText(), page, groupId);
				} else if (Response.SC_NO_CONTENT == statusCode) {
					onAvailServicesSuccess(null, page, groupId);
				} else {
					LOG.warning("GET available services returned incorrect status: " + statusCode);
					onAvailServicesFailure();
				}
			}
		};

		// send request
		CommonSenseApi.getAvailableServices(reqCallback, Integer.toString(PER_PAGE),
				Integer.toString(page), groupId);
	}

	private void getGroups(final List<GxtSensor> library,
			final AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				LOG.warning("GET groups onError callback: " + exception.getMessage());
				onGroupsFailure(callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				LOG.finest("GET groups response received: " + response.getStatusText());
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onGroupsSuccess(response.getText(), library, callback);
				} else if (Response.SC_NO_CONTENT == statusCode) {
					// no content
					onGroupsSuccess(null, library, callback);
				} else {
					LOG.warning("GET groups returned incorrect status: " + statusCode);
					onGroupsFailure(callback);
				}
			}
		};

		CommonSenseApi.getGroups(reqCallback, Integer.toString(PER_PAGE), null);
	}

	private void getGroupSensors(final List<Group> groups, final int index, final int page,
			final List<GxtSensor> library, final AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		if (index < groups.size()) {

			// prepare request callback
			RequestCallback reqCallback = new RequestCallback() {

				@Override
				public void onError(Request request, Throwable exception) {
					LOG.warning("GET group sensors onError callback: " + exception.getMessage());
					onGroupSensorsFailure(callback);
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					LOG.finest("GET group sensors response received: " + response.getStatusText());
					switch (response.getStatusCode()) {
					case Response.SC_OK:
						onGroupSensorsSuccess(response.getText(), groups, index, page, library,
								callback);
						break;
					case Response.SC_NO_CONTENT:
						// fall through
					case Response.SC_FORBIDDEN:
						// no content
						onGroupSensorsSuccess(null, groups, index, page, library, callback);
						break;
					default:
						LOG.warning("GET group sensors returned incorrect status: "
								+ response.getStatusCode());
						onGroupSensorsFailure(callback);
					}
				}
			};

			int groupId = groups.get(index).getId();

			CommonSenseApi.getSensors(reqCallback, Integer.toString(PER_PAGE),
					Integer.toString(page), null, null, null, "full", Integer.toString(groupId));

		} else {

			// notify the view that the list is complete
			onLoadComplete(library, callback);
		}
	}

	private void getSensors(final List<GxtSensor> library, final int page, final boolean shared,
			final AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		// prepare request callback
		RequestCallback reqCallback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				onSensorsFailure(-1, exception, callback);
			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				int statusCode = response.getStatusCode();
				if (Response.SC_OK == statusCode) {
					onSensorsResponse(response.getText(), library, page, shared, callback);
				} else if (Response.SC_NO_CONTENT == statusCode) {
					onSensorsResponse(null, library, page, shared, callback);
				} else {
					LOG.warning("GET sensors returned incorrect status: " + statusCode);
					onSensorsFailure(statusCode, new Throwable(response.getStatusText()), callback);
				}
			}
		};

		CommonSenseApi.getSensors(reqCallback, Integer.toString(PER_PAGE), Integer.toString(page),
				shared ? "1" : null, null, null, "full", null);
	}

	@Override
	public void loadData(AsyncCallback<ListLoadResult<GxtSensor>> callback, boolean renewCache) {
		List<GxtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);
		if (renewCache) {
			library.clear();
			Registry.<List<GxtDevice>> get(Constants.REG_DEVICE_LIST).clear();

			isLoadingList = true;
			notifyState();

			getSensors(library, 0, false, callback);
		} else {
			onLoadComplete(library, callback);
		}
	}

	private void notifyState() {
		view.setBusy(isLoadingList || isLoadingUsers || isLoadingServices);
	}

	private void onAvailServicesFailure() {
		isLoadingServices = false;
		notifyState();
	}

	private void onAvailServicesSuccess(String response, int page, String groupId) {

		List<GxtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);

		// parse list of services from response
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			BatchAvailServicesResponse jso = JsonUtils.unsafeEval(response);
			JsArray<AvailServicesResponseEntry> entries = jso.getEntries();
			for (int i = 0; i < entries.length(); i++) {
				int id = entries.get(i).getSensorId();
				List<Service> availServices = entries.get(i).getServices();
				List<GxtService> gxtServices = new ArrayList<GxtService>();
				for (Service service : availServices) {
					gxtServices.add(new GxtService(service));
				}
				for (GxtSensor sensor : library) {
					if (sensor.getId() == id) {
						sensor.setAvailServices(gxtServices);
					}
				}
			}

			if (entries.length() < jso.getTotal()) {
				page++;
				getAvailableServices(page, groupId);
				return;
			}
		}

		isLoadingServices = false;
		notifyState();
	}

	private void onGroupSensorsFailure(AsyncCallback<ListLoadResult<GxtSensor>> callback) {
		onLoadFailure(callback);
	}

	private void onGroupSensorsSuccess(String response, List<Group> groups, int index, int page,
			List<GxtSensor> library, AsyncCallback<ListLoadResult<GxtSensor>> callback) {
		LOG.fine("Received group sensors response...");

		// parse group sensors
		JsArray<Sensor> groupSensors = JsArray.createArray().cast();
		int total = 0;
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			groupSensors = responseJso.getRawSensors();
			total = responseJso.getTotal();
		}

		LOG.finest("Parsed group sensors...");

		Group group = groups.get(index);
		for (int i = 0; i < groupSensors.length(); i++) {
			GxtSensor groupSensor = new GxtSensor(groupSensors.get(i));
			if (!library.contains(groupSensor)) {
				// set SensorModel.ALIAS property
				groupSensor.setAlias(group.getId());
				library.add(groupSensor);
			}
		}

		int retrieved = page * PER_PAGE + groupSensors.length();
		if (total > retrieved) {
			// not all sensors from the group are retrieved yet
			page++;
			getGroupSensors(groups, index, page, library, callback);

		} else {
			if (!MainEntryPoint.HACK_SKIP_LIB_DETAILS && groupSensors.length() > 0) {
				// get available services from the group sensors
				getAvailableServices(0, "" + group.getId());
			}

			// next group
			index++;
			getGroupSensors(groups, index, 0, library, callback);
		}
	}

	private void onGroupsFailure(AsyncCallback<ListLoadResult<GxtSensor>> callback) {
		onLoadFailure(callback);
	}

	private void onGroupsSuccess(String response, List<GxtSensor> library,
			AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		// parse list of groups from the response
		List<Group> groups = new ArrayList<Group>();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetGroupsResponse jso = JsonUtils.unsafeEval(response);
			groups = jso.getGroups();
		}

		getGroupSensors(groups, 0, 0, library, callback);
	}

	private void onLoadComplete(List<GxtSensor> library,
			AsyncCallback<ListLoadResult<GxtSensor>> callback) {
		LOG.fine("Load complete...");

		// update list of devices
		Registry.<List<GxtDevice>> get(Constants.REG_DEVICE_LIST).clear();
		Registry.<List<GxtDevice>> get(Constants.REG_DEVICE_LIST).addAll(
				devicesFromLibrary(library));

		isLoadingList = false;
		notifyState();

		if (null != callback) {
			LOG.finest("Create load result...");
			ListLoadResult<GxtSensor> result = new BaseListLoadResult<GxtSensor>(library);

			LOG.finest("Call back with load result...");
			callback.onSuccess(result);
		}
	}

	private void onLoadFailure(AsyncCallback<ListLoadResult<GxtSensor>> callback) {
		Registry.<List<GxtSensor>> get(Constants.REG_SENSOR_LIST).clear();
		Registry.<List<GxtDevice>> get(Constants.REG_DEVICE_LIST).clear();
		Registry.<List<GxtEnvironment>> get(Constants.REG_ENVIRONMENT_LIST).clear();

		if (null != callback) {
			callback.onFailure(null);
		}
	}

	private void onSensorsFailure(int code, Throwable error,
			AsyncCallback<ListLoadResult<GxtSensor>> callback) {
		onLoadFailure(callback);
	}

	private void onSensorsResponse(String response, List<GxtSensor> library, int page,
			boolean shared, AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		// different callbacks for shared or unshared requests
		if (shared) {
			onSharedSensorsSuccess(response, library, page, callback);
		} else {
			onUnsharedSensorsSuccess(response, library, page, callback);
		}
	}

	private void onSharedSensorsSuccess(String response, List<GxtSensor> library, int page,
			AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		// parse response
		int total = library.size();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {

			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			total = responseJso.getTotal();

			GxtUser user = Registry.<GxtUser> get(Constants.REG_USER);
			JsArray<Sensor> sharedSensors = responseJso.getRawSensors();
			for (int i = 0; i < sharedSensors.length(); i++) {
				GxtSensor sharedSensor = new GxtSensor(sharedSensors.get(i));
				sharedSensor.getUsers().add(user);
				library.remove(sharedSensor);
				library.add(sharedSensor);
			}
		}

		LOG.fine("total: " + total + ", library size: " + library.size());

		if (total > library.size()) {
			// get the next page with sensors
			page++;
			getSensors(library, page, true, callback);

		} else {
			// request full details for my own sensors
			if (!MainEntryPoint.HACK_SKIP_LIB_DETAILS) {
				getAvailableServices(0, null);
			}

			// continue by getting the group sensors
			getGroups(library, callback);
		}
	}

	private void onUnsharedSensorsSuccess(String response, List<GxtSensor> library, int page,
			AsyncCallback<ListLoadResult<GxtSensor>> callback) {

		// parse response
		int total = library.size();
		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
			total = responseJso.getTotal();
			JsArray<Sensor> sensors = responseJso.getRawSensors();
			for (int i = 0; i < sensors.length(); i++) {
				GxtSensor sensor = new GxtSensor(sensors.get(i));
				library.add(sensor);
			}
		}

		LOG.fine("total: " + total + ", library size: " + library.size());

		if (total > library.size()) {
			// get the next page with sensors
			page++;
			getSensors(library, page, false, callback);

		} else {
			// continue by getting the shared sensors
			getSensors(library, page, true, callback);
		}
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
		LOG.info("Start 'sensors' activity");

		view = clientFactory.getSensorListView();
		view.setPresenter(this);

		LayoutContainer parent = clientFactory.getMainView().getGxtActivityPanel();
		parent.removeAll();
		parent.add(view.asWidget());
		parent.layout();

		view.refreshLoader(false);
	}

	@Override
	public void onVisualizeClick(List<GxtSensor> sensors) {

		VisualizationChooserView visualizationChooser = clientFactory.getVisualizationChooserView();
		visualizationChooser.showWindow(sensors);
	}
}