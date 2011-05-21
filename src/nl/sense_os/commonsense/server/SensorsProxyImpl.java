package nl.sense_os.commonsense.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.rpc.SensorsProxy;
import nl.sense_os.commonsense.server.utility.SensorConverter;
import nl.sense_os.commonsense.shared.constants.Urls;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;
import nl.sense_os.commonsense.shared.models.DeviceModel;
import nl.sense_os.commonsense.shared.models.GroupModel;
import nl.sense_os.commonsense.shared.models.SensorModel;
import nl.sense_os.commonsense.shared.models.ServiceModel;
import nl.sense_os.commonsense.shared.models.TagModel;
import nl.sense_os.commonsense.shared.models.UserModel;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Servlet to provide sensor details to the client. These can be different from the standard sensors
 * and devices structure that the CommonSense API exposes, initially this servlet is more like a
 * proxy, not changing any details.
 */
public class SensorsProxyImpl extends RemoteServiceServlet implements SensorsProxy {

    private static final Logger log = Logger.getLogger("SensorsProxyImpl");
    private static final long serialVersionUID = 1L;

    private List<SensorModel> getAllSensors(String sessionId, String params)
            throws WrongResponseException, DbConnectionException {

        List<SensorModel> sensors = new ArrayList<SensorModel>();

        params = params.length() > 0 ? "&" + params : "";
        int perPage = 1000;
        int fetched = 0;
        long total = fetched + 1;
        int page = 0;
        while (total > fetched) {
            String url = Urls.SENSORS + "?per_page=" + perPage + "&page=" + page + params;
            String response = Requester.request(url, sessionId, "GET", null);

            total = SensorConverter.parseSensors(response, sensors);
            fetched = sensors.size();
            page++;
        }
        return sensors;
    }

    @Override
    public List<TreeModel> getAvailableSensors(String sessionId, String serviceName)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        List<SensorModel> sensors = getAllSensors(sessionId, "owned=1");

        // remove sensors that are not available for the selected service
        List<SensorModel> availableSensors = new ArrayList<SensorModel>();
        for (SensorModel sensor : sensors) {
            String sensorId = sensor.<String> get(SensorModel.ID);
            String sensorType = sensor.<String> get(SensorModel.TYPE);
            if (false == sensorType.equals("1")) {
                if (isSensorAvailable(sessionId, sensorId, serviceName)) {
                    availableSensors.add(sensor);
                }
            }
        }

        // sort the available sensors
        List<SensorModel> feeds = new ArrayList<SensorModel>();
        List<SensorModel> devices = new ArrayList<SensorModel>();
        List<SensorModel> states = new ArrayList<SensorModel>();
        List<SensorModel> environments = new ArrayList<SensorModel>();
        List<SensorModel> apps = new ArrayList<SensorModel>();
        List<TreeModel> sorted = sortSensors(availableSensors, devices, environments, apps, feeds,
                states);

        // special handling of device sensors
        List<DeviceModel> sortedDevices = getDevices(sessionId, serviceName);
        List<DeviceModel> availableDevices = new ArrayList<DeviceModel>();
        for (DeviceModel device : sortedDevices) {
            List<ModelData> deviceSensors = device.getChildren();
            List<ModelData> availableDeviceSensors = new ArrayList<ModelData>();
            for (ModelData sensor : deviceSensors) {
                String sensorId = sensor.<String> get(SensorModel.ID);
                if (isSensorAvailable(sessionId, sensorId, serviceName)) {
                    availableDeviceSensors.add(sensor);
                }
            }

            if (availableDeviceSensors.size() > 0) {
                DeviceModel availableDevice = new DeviceModel(device.getProperties());
                for (ModelData availableSensor : availableDeviceSensors) {
                    availableDevice.add(availableSensor);
                }
                availableDevices.add(availableDevice);
            }
        }

        // add device category to sorted sensors
        TreeModel deviceCategory = new BaseTreeModel();
        deviceCategory.set("text", "Devices");
        deviceCategory.set("tagType", TagModel.TYPE_CATEGORY);
        for (DeviceModel device : availableDevices) {
            deviceCategory.add(device);
        }
        sorted.add(deviceCategory);

        return sorted;
    }

    @Override
    public List<ServiceModel> getAvailableServices(String sessionId) throws WrongResponseException,
            DbConnectionException {

        // request all sensors from server
        List<SensorModel> sensors = getAllSensors(sessionId, "");

        HashMap<String, ServiceModel> foundServices = new HashMap<String, ServiceModel>();
        for (SensorModel sensor : sensors) {

            // request all available services for this sensor
            String url = Urls.SENSORS + "/" + sensor.<String> get("id") + "/services/available";
            String response = Requester.request(url, sessionId, "GET", null);
            List<ServiceModel> sensorServices = parseAvailableServices(response);

            // process the available services for this sensor
            for (ServiceModel service : sensorServices) {

                // find service in list of services
                String key = service.<String> get(ServiceModel.NAME);
                ServiceModel foundService = foundServices.get(key);
                if (null == foundService) {
                    foundService = new ServiceModel(service.getProperties());
                }

                // add this sensor as possible source for this service
                SensorModel sourceSensor = new SensorModel(sensor.getProperties());
                sourceSensor.set(ServiceModel.DATA_FIELDS,
                        service.<List<String>> get(ServiceModel.DATA_FIELDS));
                foundService.add(sourceSensor);

                foundServices.put(key, foundService);
            }
        }

        return new ArrayList<ServiceModel>(foundServices.values());
    }

    private List<DeviceModel> getDevices(String sessionId, String params)
            throws WrongResponseException, DbConnectionException {

        // get list of physical sensors
        params = "?per_page=1000&physical=1&" + params;
        String url = Urls.SENSORS + params;
        String response = Requester.request(url, sessionId, "GET", null);
        List<SensorModel> sensors = new ArrayList<SensorModel>();
        SensorConverter.parseSensors(response, sensors);

        // sort sensors per device
        Map<String, DeviceModel> devices = new HashMap<String, DeviceModel>();
        for (SensorModel sensor : sensors) {

            // get the device TreeModel, or create a new one
            DeviceModel device = sensor.getDevice();
            String deviceKey = device.getId() + device.getUuid();

            // add the sensor to the device
            device.add(new SensorModel(sensor.getProperties()));
            devices.put(deviceKey, device);
        }

        return new ArrayList<DeviceModel>(devices.values());
    }

    private TreeModel getGroupSensors(String sessionId, GroupModel group)
            throws DbConnectionException, WrongResponseException {

        // get all sensors that the group can see
        String groupId = group.<String> get("id");
        final String params = "alias=" + groupId;
        List<SensorModel> allSensors = getAllSensors(sessionId, params);

        // the sensors will be sorted for the group members
        Map<String, UserModel> members = new HashMap<String, UserModel>();
        for (ModelData groupMember : group.getChildren()) {
            members.put(groupMember.<String> get("id"), (UserModel) groupMember);
        }

        // delete anything that is in the group
        group.removeAll();

        // find out which group member is owner of the sensor
        for (SensorModel sensor : allSensors) {

            // save the alias that has to be used to get the data
            sensor.set(SensorModel.ALIAS, groupId);

            // set the sensor as a child of the correct user(s)
            String sensorId = sensor.<String> get("id");
            List<ModelData> sensorUsers = getSensorUsers(sensorId, sessionId, params);
            for (ModelData sensorUser : sensorUsers) {
                String userId = sensorUser.<String> get("id");

                UserModel user = members.get(userId);
                if (null != user) {
                    user.add(new SensorModel(sensor.getProperties()));
                    members.put(userId, user);
                } else if (userId.equals(groupId) && sensorUsers.size() == 1) {
                    // no 'real' users own this sensor: add the sensor to the root of the group
                    group.add(new SensorModel(sensor.getProperties()));
                }
            }
        }

        // add the sensors tree to the group root
        for (TreeModel treeItem : members.values()) {
            group.add(treeItem);
        }
        return group;
    }

    @Override
    public List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // request all sensors from server
        String params = "owned=1";
        List<SensorModel> unsortedSensors = getAllSensors(sessionId, params);

        // store alias and owner information for future use
        for (SensorModel sensor : unsortedSensors) {
            sensor.set("owned", "1");
        }

        List<SensorModel> feedsSensors = new ArrayList<SensorModel>();
        List<SensorModel> deviceSensors = new ArrayList<SensorModel>();
        List<SensorModel> stateSensors = new ArrayList<SensorModel>();
        List<SensorModel> environmentSensors = new ArrayList<SensorModel>();
        List<SensorModel> apps = new ArrayList<SensorModel>();
        List<TreeModel> sorted = sortSensors(unsortedSensors, deviceSensors, environmentSensors,
                apps, feedsSensors, stateSensors);

        // handle nested devices
        // remove the Device category if it is already present
        for (TreeModel cat : sorted) {
            if (cat.<String> get("text").equalsIgnoreCase("devices")) {
                sorted.remove(cat);
                break;
            }
        }
        // add a special category of devices
        List<DeviceModel> devices = getDevices(sessionId, params);
        if (devices.size() > 0) {
            TreeModel deviceCat = new BaseTreeModel();
            deviceCat.set("text", "Devices");
            deviceCat.set("tagType", TagModel.TYPE_CATEGORY);
            for (DeviceModel child : devices) {
                deviceCat.add(child);
            }
            sorted.add(deviceCat);
        }

        return sorted;
    }

    private List<ModelData> getSensorUsers(String id, String sessionId, String params)
            throws WrongResponseException, DbConnectionException {

        String path = "/" + id + "/users";
        String url = Urls.SENSORS + path + "?" + params;
        String response = Requester.request(url, sessionId, "GET", null);
        return parseUsers(response);
    }

    public List<TreeModel> getSharedSensors(String sessionId, List<TreeModel> groups)
            throws WrongResponseException, DbConnectionException {

        List<TreeModel> result = new ArrayList<TreeModel>();

        // get all sensors that are directly shared with me
        String params = "owned=0";
        List<SensorModel> unsortedSensors = getAllSensors(sessionId, params);

        // sort the sensors over the owners
        Map<String, UserModel> owners = new HashMap<String, UserModel>();
        for (ModelData sensor : unsortedSensors) {
            // get the TreeModel of the owner
            String sensorId = sensor.<String> get("id");
            String userId = sensor.<String> get("owner_id");
            UserModel owner = owners.get(userId);
            if (null == owner) {
                List<ModelData> sensorUsers = getSensorUsers(sensorId, sessionId, params);
                for (ModelData user : sensorUsers) {
                    if (user.<String> get("id").equals(userId)) {
                        owner = new UserModel(user.getProperties());
                        break;
                    }
                }
            }

            // add the sensor to the owner
            owner.add(new SensorModel(sensor.getProperties()));
            owners.put(userId, owner);
        }
        result.addAll(owners.values());

        // get the sensors for the groups that I am member of
        for (TreeModel group : groups) {
            if (group instanceof GroupModel) {
                group = getGroupSensors(sessionId, (GroupModel) group);
                result.add(group);
            } else {
                log.severe("Group is not instance of GroupModel: " + group);
            }
        }

        return result;
    }

    @Override
    public List<TreeModel> getStateSensors(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // request all sensors from server
        List<SensorModel> sensors = getAllSensors(sessionId, "owned=1");

        // gather the services that are connected to the sensors
        List<TreeModel> services = new ArrayList<TreeModel>();
        for (SensorModel sensor : sensors) {
            int type = Integer.parseInt(sensor.<String> get("type"));
            if (type == 2) {
                services.add(new SensorModel(sensor.getProperties()));
            }
        }

        // request the sensor name that is associated with each service
        for (TreeModel service : services) {
            String url = Urls.SENSORS + "/" + service.<String> get("id") + "/sensors";
            String response = Requester.request(url, sessionId, "GET", null);

            List<SensorModel> serviceSources = new ArrayList<SensorModel>();
            SensorConverter.parseSensors(response, serviceSources);

            for (TreeModel sourceSensor : serviceSources) {
                service.add(new SensorModel(sourceSensor.getProperties()));
            }
        }

        return services;
    }

    private boolean isSensorAvailable(String sessionId, String sensorId, String serviceName)
            throws WrongResponseException, DbConnectionException {

        // request all available services for this sensor
        String url = Urls.SENSORS + "/" + sensorId + "/services/available";
        String response = Requester.request(url, sessionId, "GET", null);
        List<ServiceModel> availableServices = parseAvailableServices(response);

        // add sensor to availableSensors list if it the requested service is available
        for (ModelData availableService : availableServices) {
            String availableName = availableService.get(ServiceModel.NAME);
            if (null != availableName && availableName.equals(serviceName)) {
                return true;
            }
        }

        return false;
    }

    private List<ServiceModel> parseAvailableServices(String response)
            throws WrongResponseException {
        // log.info("GET /sensors/<id>/services/available response: \'" + response + "\'");

        // Convert JSON response to list of tags
        try {
            List<ServiceModel> result = new ArrayList<ServiceModel>();
            JSONObject objecy = new JSONObject(response);
            JSONArray services = objecy.optJSONArray("available_services");
            if (null == services) {
                services = objecy.optJSONArray("services");
            }
            for (int i = 0; i < services.length(); i++) {
                JSONObject sensor = services.getJSONObject(i);

                String serviceId = sensor.optString(ServiceModel.ID);
                String serviceName = sensor.getString(ServiceModel.NAME);
                JSONArray dataFieldsArray = sensor.getJSONArray(ServiceModel.DATA_FIELDS);
                List<String> dataFields = new ArrayList<String>();
                for (int j = 0; j < dataFieldsArray.length(); j++) {
                    dataFields.add((String) dataFieldsArray.get(j));
                }

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(ServiceModel.ID, serviceId);
                properties.put(ServiceModel.NAME, serviceName);
                properties.put(ServiceModel.DATA_FIELDS, dataFields);

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_SERVICE);
                properties.put("text", serviceName);

                ServiceModel model = new ServiceModel(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET /sensors/<id>/services JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> parseUsers(String response) throws WrongResponseException {
        List<ModelData> result = new ArrayList<ModelData>();
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONArray users = responseObj.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", user.getString("id"));
                properties.put("email", user.optString("email"));
                properties.put("name", user.optString("name"));
                properties.put("surname", user.optString("surname"));
                properties.put("username", user.optString("username"));
                properties.put("mobile", user.optString("mobile"));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_USER);
                String text = user.optString("name", "") + " " + user.optString("surname", "");
                if (text.length() < 3) {
                    text = "User #" + properties.get("id");
                }
                properties.put("text", text);

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

        } catch (JSONException e) {
            log.severe("GET /users JSONException: " + e.getMessage());
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }

        // return list of users
        return result;
    }

    private List<TreeModel> sortSensors(List<SensorModel> unsorted, List<SensorModel> devices,
            List<SensorModel> environments, List<SensorModel> apps, List<SensorModel> feeds,
            List<SensorModel> states) throws DbConnectionException, WrongResponseException {

        // convert the sensor models into TreeModels
        for (SensorModel sensorModel : unsorted) {
            SensorModel sensor = new SensorModel(sensorModel.getProperties());
            int type = Integer.parseInt(sensor.<String> get("type"));
            switch (type) {
            case 0:
                feeds.add(sensor);
                break;
            case 1:
                devices.add(sensor);
                break;
            case 2:
                states.add(sensor);
                break;
            case 3:
                environments.add(sensor);
                break;
            case 4:
                apps.add(sensor);
                break;
            default:
                log.warning("Unexpected sensor type: " + type);
            }
        }

        // create main groups
        TreeModel feedCat = new BaseTreeModel();
        feedCat.set("text", "Public Feeds");
        feedCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : feeds) {
            feedCat.add(child);
        }
        TreeModel deviceCat = new BaseTreeModel();
        deviceCat.set("text", "Devices");
        deviceCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : devices) {
            deviceCat.add(child);
        }
        TreeModel stateCat = new BaseTreeModel();
        stateCat.set("text", "State Sensors");
        stateCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : states) {
            stateCat.add(child);
        }
        TreeModel environmentCat = new BaseTreeModel();
        environmentCat.set("text", "Environments");
        environmentCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : environments) {
            environmentCat.add(child);
        }
        TreeModel appCat = new BaseTreeModel();
        appCat.set("text", "Online Activity");
        appCat.set("tagType", TagModel.TYPE_CATEGORY);
        for (TreeModel child : apps) {
            appCat.add(child);
        }

        List<TreeModel> sorted = new ArrayList<TreeModel>();
        if (feedCat.getChildCount() > 0) {
            sorted.add(feedCat);
        }
        if (deviceCat.getChildCount() > 0) {
            sorted.add(deviceCat);
        }
        if (stateCat.getChildCount() > 0) {
            sorted.add(stateCat);
        }
        if (environmentCat.getChildCount() > 0) {
            sorted.add(environmentCat);
        }
        if (appCat.getChildCount() > 0) {
            sorted.add(appCat);
        }

        return sorted;
    }
}
