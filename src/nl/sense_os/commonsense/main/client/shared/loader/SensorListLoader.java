package nl.sense_os.commonsense.main.client.shared.loader;

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
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtService;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class SensorListLoader implements Loader {

    private static final int PER_PAGE = 1000;
    private static final Logger LOG = Logger.getLogger(SensorListLoader.class.getName());
    private Callback callback;

    /**
     * Requests a list of all available services for all sensors the user owns.
     * 
     * @param groupId
     *            Optional parameter to get the available services for sensors that are not shared
     *            directly with the user but with a group.
     */
    private void getAvailableServices(final int page, final String groupId) {

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onAvailServicesFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onAvailServicesSuccess(response.getText(), page, groupId);
                } else if (Response.SC_NO_CONTENT == statusCode) {
                    onAvailServicesSuccess(null, page, groupId);
                } else {
                    onAvailServicesFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        // send request
        CommonSenseApi.getAvailableServices(reqCallback, Integer.toString(PER_PAGE),
                Integer.toString(page), groupId);
    }

    private void getGroups(final List<GxtSensor> library) {
        LOG.fine("Get groups");

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("GET groups onError callback: " + exception.getMessage());
                onGroupsFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("GET groups response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onGroupsSuccess(response.getText(), library);
                } else if (Response.SC_NO_CONTENT == statusCode) {
                    // no content
                    onGroupsSuccess(null, library);
                } else {
                    LOG.warning("GET groups returned incorrect status: " + statusCode);
                    onGroupsFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        CommonSenseApi.getGroups(reqCallback, Integer.toString(PER_PAGE), null);
    }

    private void getGroupSensors(final List<Group> groups, final int index, final int page,
            final List<GxtSensor> library) {

        if (index < groups.size()) {

            // prepare request callback
            RequestCallback reqCallback = new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                    LOG.warning("GET group sensors onError callback: " + exception.getMessage());
                    onGroupSensorsFailure(-1, exception);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    LOG.finest("GET group sensors response received: " + response.getStatusText());
                    switch (response.getStatusCode()) {
                    case Response.SC_OK:
                        onGroupSensorsSuccess(response.getText(), groups, index, page, library);
                        break;
                    case Response.SC_NO_CONTENT:
                        // fall through
                    case Response.SC_FORBIDDEN:
                        // no content
                        onGroupSensorsSuccess(null, groups, index, page, library);
                        break;
                    default:
                        LOG.warning("GET group sensors returned incorrect status: "
                                + response.getStatusCode());
                        onGroupSensorsFailure(response.getStatusCode(),
                                new Throwable(response.getStatusText()));
                    }
                }
            };

            String groupId = groups.get(index).getId();

            CommonSenseApi.getSensors(reqCallback, Integer.toString(PER_PAGE),
                    Integer.toString(page), null, null, null, "full", groupId);

        } else {

            // notify the view that the list is complete
            onLoadComplete(library);
        }
    }

    private void getSensors(final List<GxtSensor> library, final int page, final boolean shared) {
        LOG.fine("Get sensors");

        // prepare request callback
        RequestCallback reqCallback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                onSensorsFailure(-1, exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                int statusCode = response.getStatusCode();
                if (Response.SC_OK == statusCode) {
                    onSensorsResponse(response.getText(), library, page, shared);
                } else if (Response.SC_NO_CONTENT == statusCode) {
                    onSensorsResponse(null, library, page, shared);
                } else {
                    LOG.warning("GET sensors returned incorrect status: " + statusCode);
                    onSensorsFailure(statusCode, new Throwable(response.getStatusText()));
                }
            }
        };

        CommonSenseApi.getSensors(reqCallback, Integer.toString(PER_PAGE), Integer.toString(page),
                shared ? "1" : null, null, null, "full", null);
    }

    @Override
    public void load(Callback callback) {
        this.callback = callback;
        List<GxtSensor> library = new ArrayList<GxtSensor>();
        getSensors(library, 0, false);
    }

    private void onAvailServicesFailure(int code, Throwable error) {
        LOG.warning("Failed to load available services! Code: " + code + ", error: " + error);
    }

    private void onAvailServicesSuccess(String response, int page, String groupId) {

        List<GxtSensor> library = Registry.get(Constants.REG_SENSOR_LIST);

        // parse list of services from response
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            BatchAvailServicesResponse jso = JsonUtils.unsafeEval(response);
            JsArray<AvailServicesResponseEntry> entries = jso.getEntries();
            for (int i = 0; i < entries.length(); i++) {
                String id = entries.get(i).getSensorId();
                List<Service> availServices = entries.get(i).getServices();
                List<GxtService> gxtServices = new ArrayList<GxtService>();
                for (Service service : availServices) {
                    gxtServices.add(new GxtService(service));
                }
                for (GxtSensor sensor : library) {
                    if (sensor.getId().equals(id)) {
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
    }

    private void onGroupSensorsFailure(int code, Throwable error) {
        onLoadFailure(code, error);
    }

    private void onGroupSensorsSuccess(String response, List<Group> groups, int index, int page,
            List<GxtSensor> library) {
        LOG.fine("Received group sensors");

        // parse group sensors
        JsArray<Sensor> groupSensors = JsArray.createArray().cast();
        int total = 0;
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
            groupSensors = responseJso.getRawSensors();
            total = responseJso.getTotal();
        }

        LOG.finest("Parsed " + groupSensors.length() + " group sensors");

        for (int i = 0; i < groupSensors.length(); i++) {
            GxtSensor groupSensor = new GxtSensor(groupSensors.get(i));
            if (!library.contains(groupSensor)) {
                library.add(groupSensor);
            }
        }

        int retrieved = page * PER_PAGE + groupSensors.length();
        if (total > retrieved) {
            // not all sensors from the group are retrieved yet
            page++;
            getGroupSensors(groups, index, page, library);

        } else {
            // get available services for this group
            if (page > 0 || groupSensors.length() > 0) {
                getAvailableServices(0, groups.get(index).getId());
            }

            // next group
            index++;
            getGroupSensors(groups, index, 0, library);
        }
    }

    private void onGroupsFailure(int code, Throwable error) {
        onLoadFailure(code, error);
    }

    private void onGroupsSuccess(String response, List<GxtSensor> library) {
        LOG.fine("Received groups list");

        // parse list of groups from the response
        List<Group> groups = new ArrayList<Group>();
        if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
            GetGroupsResponse jso = JsonUtils.unsafeEval(response);
            groups = jso.getGroups();
        }

        getGroupSensors(groups, 0, 0, library);
    }

    private void onLoadComplete(List<GxtSensor> sensors) {
        if (null != callback) {
            callback.onSuccess(sensors);
        }
    }

    private void onLoadFailure(int code, Throwable error) {
        if (null != callback) {
            callback.onFailure(code, error);
        }
    }

    private void onSensorsFailure(int code, Throwable error) {
        onLoadFailure(code, error);
    }

    private void onSensorsResponse(String response, List<GxtSensor> library, int page,
            boolean shared) {

        // different callbacks for shared or unshared requests
        if (shared) {
            onSharedSensorsSuccess(response, library, page);
        } else {
            onUnsharedSensorsSuccess(response, library, page);
        }
    }

    private void onSharedSensorsSuccess(String response, List<GxtSensor> library, int page) {
        LOG.fine("Received shared sensor list");

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
            getSensors(library, page, true);

        } else {
            // request available services for these sensors
            getAvailableServices(0, null);

            // continue by getting the group sensors
            getGroups(library);
        }
    }

    private void onUnsharedSensorsSuccess(String response, List<GxtSensor> library, int page) {
        LOG.fine("Received unshared sensor list");

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
            getSensors(library, page, false);

        } else {
            // continue by getting the shared sensors
            getSensors(library, page, true);
        }
    }

}
