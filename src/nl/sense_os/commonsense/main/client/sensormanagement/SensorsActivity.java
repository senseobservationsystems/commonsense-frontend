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

import nl.sense_os.commonsense.main.client.MainClientFactory;
import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtEnvironment;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.sensormanagement.deleter.SensorDeleter;
import nl.sense_os.commonsense.main.client.sensormanagement.vischoice.VisualizationChooser;
import nl.sense_os.commonsense.main.client.shared.loader.Loader;
import nl.sense_os.commonsense.main.client.shared.loader.SensorListLoader;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
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

    private void getSensors(final AsyncCallback<ListLoadResult<GxtSensor>> callback) {

        // call loader
        SensorListLoader sensorsLoader = new SensorListLoader();
        sensorsLoader.load(new Loader.Callback() {

            @Override
            public void onFailure(int code, Throwable error) {
                onLoadFailure(callback);
            }

            @Override
            public void onSuccess(Object result) {
                if (result instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<GxtSensor> sensors = (List<GxtSensor>) result;
                    onLoadComplete(sensors, callback);

                } else {
                    onFailure(-1, new Throwable("Unexpected sensor list loader result: " + result));
                }
            }
        });
    }

    @Override
    public void loadData(AsyncCallback<ListLoadResult<GxtSensor>> callback, boolean renewCache) {
        List<GxtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);
        if (renewCache) {
            library.clear();
            Registry.<List<GxtDevice>> get(Constants.REG_DEVICE_LIST).clear();

            isLoadingList = true;
            notifyState();

            // load all sensors
            getSensors(callback);

        } else {
            onLoadComplete(library, callback);
        }
    }

    private void notifyState() {
        view.setBusy(isLoadingList);
    }

    @Override
    public void onDeleteClick(List<GxtSensor> sensors) {
        SensorDeleter deleter = new SensorDeleter();
        deleter.start(sensors);
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

    @Override
    public void onVisualizeClick(List<GxtSensor> sensors) {
        VisualizationChooser chooser = new VisualizationChooser(clientFactory);
        chooser.start(sensors);
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
}
