package nl.sense_os.commonsense.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.services.SensorsProxy;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

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

    private List<ModelData> getAllSensors(String sessionId, String params)
            throws WrongResponseException, DbConnectionException {

        List<ModelData> sensors = new ArrayList<ModelData>();

        params = params.length() > 0 ? "&" + params : "";
        int perPage = 1000;
        int fetched = 0;
        long total = fetched + 1;
        int page = 0;
        while (total > fetched) {
            String url = Constants.URL_SENSORS + "?per_page=" + perPage + "&page=" + page + params;
            String response = Requester.request(url, sessionId, "GET", null);

            total = parseSensors(response, sensors);
            fetched = sensors.size();
            page++;
        }
        return sensors;
    }

    private List<TreeModel> getAvailableDevices(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException {
        String url = Constants.URL_DEVICES;
        String response = Requester.request(url, sessionId, "GET", null);

        // Convert JSON response to list of tags
        List<ModelData> models = parseDevices(response);

        // add child sensors for each device
        List<TreeModel> devices = new ArrayList<TreeModel>();
        for (ModelData model : models) {

            // convert "flat" device ModelData to TreeModel
            TreeModel device = new BaseTreeModel(model.getProperties());

            // get the device's sensors
            String deviceId = model.<String> get("id");
            List<ModelData> deviceSensors = requestDeviceSensors(sessionId, deviceId);
            for (ModelData childSensor : deviceSensors) {
                if (isSensorAvailable(sessionId, childSensor.<String> get("id"), service)) {
                    TreeModel sensor = new BaseTreeModel(childSensor.getProperties());
                    device.add(sensor);
                }
            }

            // add device to the tags
            devices.add(device);
        }
        return devices;
    }

    @Override
    public List<TreeModel> getAvailableSensors(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        List<ModelData> sensors = getAllSensors(sessionId, "owned=1");

        // check non-device sensors
        List<ModelData> availableSensors = new ArrayList<ModelData>();
        for (ModelData sensor : sensors) {
            int type = Integer.parseInt(sensor.<String> get("type"));
            if (type != 1) {
                if (isSensorAvailable(sessionId, sensor.<String> get("id"), service)) {
                    availableSensors.add(sensor);
                }
            }
        }

        // sort the available sensors
        List<TreeModel> feeds = new ArrayList<TreeModel>();
        List<TreeModel> devices = new ArrayList<TreeModel>();
        List<TreeModel> states = new ArrayList<TreeModel>();
        List<TreeModel> environments = new ArrayList<TreeModel>();
        List<TreeModel> apps = new ArrayList<TreeModel>();
        List<TreeModel> sorted = sortSensors(availableSensors, devices, environments, apps, feeds,
                states);

        devices = getAvailableDevices(sessionId, service);
        for (TreeModel category : sorted) {
            if (category.<String> get("text").equalsIgnoreCase("devices")) {
                category.removeAll();
                for (TreeModel device : devices) {
                    category.add(device);
                }
                break;
            }
        }

        return sorted;
    }

    @Override
    public List<TreeModel> getAvailableServices(String sessionId) throws WrongResponseException,
            DbConnectionException {

        // request all sensors from server
        List<ModelData> sensors = getAllSensors(sessionId, "");

        HashMap<String, TreeModel> foundServices = new HashMap<String, TreeModel>();
        for (ModelData sensorModel : sensors) {
            // create TreeModel for this sensor
            TreeModel sensor = new BaseTreeModel(sensorModel.getProperties());

            // request all available services for this sensor
            String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id")
                    + "/services/available";
            String response = Requester.request(url, sessionId, "GET", null);
            List<ModelData> sensorServices = parseAvailableServices(response);

            // process the available services for this sensor
            for (ModelData serviceModel : sensorServices) {
                String key = serviceModel.<String> get("service_name");
                TreeModel service = foundServices.get(key);
                if (null == service) {
                    service = new BaseTreeModel(serviceModel.getProperties());
                } else {
                    // add the data fields of the new service model to the fields that are already
                    // known
                    List<String> dataFields = serviceModel.<List<String>> get("data_fields");
                    dataFields.addAll(service.<List<String>> get("data_fields"));
                    service.set("data_fields", dataFields);
                }

                foundServices.put(key, service);
            }
        }

        return new ArrayList<TreeModel>(foundServices.values());
    }

    private List<TreeModel> getDevices(String sessionId) throws WrongResponseException,
            DbConnectionException {

        // get list of physical sensors
        String url = Constants.URL_SENSORS + "?per_page=1000&physical=1";
        String response = Requester.request(url, sessionId, "GET", null);
        List<ModelData> sensors = new ArrayList<ModelData>();
        parseSensors(response, sensors);

        // sort sensors per device
        Map<String, TreeModel> devices = new HashMap<String, TreeModel>();
        for (ModelData sensor : sensors) {

            // get the device TreeModel, or create a new one
            String deviceUuid = sensor.<String> get("device_device_uuid");
            TreeModel device = devices.get(deviceUuid);
            if (device == null) {
                device = new BaseTreeModel();
                device.set("uuid", deviceUuid);
                device.set("type", sensor.get("device_device_type"));

                // front end-only properties
                device.set("tagType", TagModel.TYPE_DEVICE);
                if (device.get("type").equals("myrianode")) {
                    device.set("text", device.get("type") + " " + device.get("uuid"));
                } else {
                    device.set("text", device.get("type"));
                }
            }

            // add the sensor to the device
            device.add(new BaseTreeModel(sensor.getProperties()));
            devices.put(deviceUuid, device);
        }

        return new ArrayList<TreeModel>(devices.values());
    }

    @Override
    public TreeModel getGroupSensors(String sessionId, TreeModel group)
            throws DbConnectionException, WrongResponseException {

        // get all sensors that the group can see
        String groupId = group.<String> get("id");
        final String params = "alias=" + groupId;
        List<ModelData> allSensors = getAllSensors(sessionId, params);

        // the sensors will be sorted for the group members
        Map<String, TreeModel> members = new HashMap<String, TreeModel>();
        for (ModelData groupMember : group.getChildren()) {
            members.put(groupMember.<String> get("id"), (TreeModel) groupMember);
        }

        // delete anything that is in the group
        group.removeAll();

        // find out which group member is owner of the sensor
        for (ModelData sensor : allSensors) {

            // save the alias that has to be used to get the data
            sensor.set("alias", groupId);

            // set the sensor as a child of the correct user(s)
            String sensorId = sensor.<String> get("id");
            List<ModelData> sensorUsers = getSensorUsers(sensorId, sessionId, params);
            for (ModelData sensorUser : sensorUsers) {
                String userId = sensorUser.<String> get("id");

                TreeModel user = members.get(userId);
                if (null != user) {
                    user.add(new BaseTreeModel(sensor.getProperties()));
                    members.put(userId, user);
                } else if (userId.equals(groupId) && sensorUsers.size() == 1) {
                    // no 'real' users own this sensor: add the sensor to the root of the group
                    group.add(new BaseTreeModel(sensor.getProperties()));
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

        return getSortedSensors(sessionId, "1", null);
    }

    @Override
    public List<TreeModel> getMyServices(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // request all sensors from server
        List<ModelData> sensors = getAllSensors(sessionId, "owned=1");

        // gather the services that are connected to the sensors
        List<TreeModel> services = new ArrayList<TreeModel>();
        for (ModelData sensor : sensors) {
            int type = Integer.parseInt(sensor.<String> get("type"));
            if (type == 2) {
                services.add(new BaseTreeModel(sensor.getProperties()));
            }
        }

        // request the sensor name that is associated with each service
        for (TreeModel service : services) {
            String url = Constants.URL_SENSORS + "/" + service.<String> get("id") + "/sensors";
            String response = Requester.request(url, sessionId, "GET", null);

            List<ModelData> serviceSources = new ArrayList<ModelData>();
            parseSensors(response, serviceSources);

            for (ModelData sourceSensor : serviceSources) {
                service.add(new BaseTreeModel(sourceSensor.getProperties()));
            }
        }

        return services;
    }

    private List<ModelData> getSensorUsers(String id, String sessionId, String params)
            throws WrongResponseException, DbConnectionException {

        String path = "/" + id + "/users";
        String url = Constants.URL_SENSORS + path + "?" + params;
        String response = Requester.request(url, sessionId, "GET", null);
        return parseUsers(response);
    }

    private List<TreeModel> getSortedSensors(String sessionId, String owned, String alias)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        String params = "";
        if (null != owned) {
            if (null != alias) {
                params = "owned=" + owned + "&alias=" + alias;
            } else {
                params = "owned=" + owned;
            }
        } else if (null != alias) {
            params = "alias=" + alias;
        }
        List<ModelData> unsortedSensors = getAllSensors(sessionId, params);

        // store alias and owner information for future use
        for (ModelData sensor : unsortedSensors) {
            sensor.set("owned", owned);
            sensor.set("alias", alias);
        }

        List<TreeModel> feedsSensors = new ArrayList<TreeModel>();
        List<TreeModel> deviceSensors = new ArrayList<TreeModel>();
        List<TreeModel> stateSensors = new ArrayList<TreeModel>();
        List<TreeModel> environmentSensors = new ArrayList<TreeModel>();
        List<TreeModel> apps = new ArrayList<TreeModel>();
        List<TreeModel> sorted = sortSensors(unsortedSensors, deviceSensors, environmentSensors,
                apps, feedsSensors, stateSensors);

        // handle nested devices
        for (TreeModel cat : sorted) {
            if (cat.<String> get("text").equalsIgnoreCase("devices")) {
                sorted.remove(cat);
                break;
            }
        }
        List<TreeModel> devices = getDevices(sessionId);
        TreeModel deviceCat = new BaseTreeModel();
        deviceCat.set("text", "Devices");
        deviceCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : devices) {
            deviceCat.add(child);
        }
        sorted.add(deviceCat);

        return sorted;
    }

    private boolean isSensorAvailable(String sessionId, String sensorId, TreeModel service)
            throws WrongResponseException, DbConnectionException {

        // request all available services for this sensor
        String url = Constants.URL_SENSORS + "/" + sensorId + "/services/available";
        String response = Requester.request(url, sessionId, "GET", null);
        List<ModelData> sensorServices = parseAvailableServices(response);

        // add sensor to availableSensors list if it the requested service is available
        for (ModelData sensorService : sensorServices) {
            if (sensorService.get("service_name").equals(service.get("service_name"))) {
                return true;
            }
        }
        return false;
    }

    private List<ModelData> parseAvailableServices(String response) throws WrongResponseException {
        // log.info("GET /sensors/<id>/services/available response: \'" + response + "\'");

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray services = (JSONArray) new JSONObject(response).get("available_services");
            for (int i = 0; i < services.length(); i++) {
                JSONObject sensor = services.getJSONObject(i);

                String serviceName = sensor.getString("name");
                JSONArray dataFieldsArray = sensor.getJSONArray("data_fields");
                List<String> dataFields = new ArrayList<String>();
                for (int j = 0; j < dataFieldsArray.length(); j++) {
                    dataFields.add((String) dataFieldsArray.get(j));
                }

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("service_name", serviceName);
                properties.put("data_fields", dataFields);

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_SERVICE);
                properties.put("text", serviceName);

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET /sensors/<id>/services JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> parseDevices(String response) throws WrongResponseException {

        List<ModelData> result = new ArrayList<ModelData>();
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONArray devices = responseObj.getJSONArray("devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", device.getString("id"));
                properties.put("type", device.getString("type"));
                properties.put("uuid", device.getString("uuid"));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_DEVICE);
                if (properties.get("type").equals("myrianode")) {
                    String text = properties.get("type") + " " + properties.get("uuid");
                    properties.put("text", text);
                } else {
                    properties.put("text", properties.get("type"));
                }

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

        } catch (JSONException e) {
            log.severe("GET /devices JSONException: " + e.getMessage());
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }

        // return list of tags
        return result;
    }

    private int parseSensors(String response, List<ModelData> list) throws DbConnectionException,
            WrongResponseException {

        // Convert JSON response to list of tags
        try {
            JSONObject responseObj = new JSONObject(response);
            JSONArray sensors = responseObj.getJSONArray("sensors");
            int total = responseObj.optInt("total", sensors.length());
            for (int i = 0; i < sensors.length(); i++) {
                JSONObject sensor = sensors.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", sensor.getString("id"));
                properties.put("data_type_id", sensor.optString("data_type_id"));
                properties.put("pager_type", sensor.getString("pager_type"));
                properties.put("device_type", sensor.getString("device_type"));
                properties.put("name", sensor.getString("name"));
                properties.put("type", sensor.getString("type"));
                properties.put("data_type", sensor.optString("data_type"));
                properties.put("data_type_id", sensor.optString("data_type_id"));
                properties.put("data_structure", sensor.optString("data_structure"));
                properties.put("owner_id", sensor.optString("owner_id"));
                properties.put("device_device_type", sensor.optString("device_device_type"));
                properties.put("device_device_uuid", sensor.optString("device_device_uuid"));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_SENSOR);
                String name = (String) properties.get("name");
                String deviceType = (String) properties.get("device_type");
                if (name.equals(deviceType)) {
                    properties.put("text", name);
                } else {
                    properties.put("text", name + " (" + deviceType + ")");
                }

                ModelData model = new BaseModelData(properties);

                list.add(model);
            }

            // return total count
            return total;

        } catch (JSONException e) {
            log.severe("GET /sensors JSONException: " + e.getMessage());
            log.severe("Raw response: " + response);
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

    private List<ModelData> requestDeviceSensors(String sessionId, String deviceId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_DEVICE_SENSORS.replace("<id>", deviceId);
        String response = Requester.request(url, sessionId, "GET", null);

        // Convert JSON response to list of tags
        List<ModelData> sensors = new ArrayList<ModelData>();
        parseSensors(response, sensors);
        return sensors;
    }

    private List<TreeModel> sortSensors(List<ModelData> unsorted, List<TreeModel> devices,
            List<TreeModel> environments, List<TreeModel> apps, List<TreeModel> feeds,
            List<TreeModel> states) throws DbConnectionException, WrongResponseException {

        // convert the sensor models into TreeModels
        for (ModelData sensorModel : unsorted) {
            TreeModel sensor = new BaseTreeModel(sensorModel.getProperties());
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
        feedCat.set("text", "Feeds");
        feedCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : feeds) {
            feedCat.add(child);
        }
        TreeModel deviceCat = new BaseTreeModel();
        deviceCat.set("text", "Devices");
        deviceCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : devices) {
            deviceCat.add(child);
        }
        TreeModel stateCat = new BaseTreeModel();
        stateCat.set("text", "States");
        stateCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : states) {
            stateCat.add(child);
        }
        TreeModel environmentCat = new BaseTreeModel();
        environmentCat.set("text", "Environments");
        environmentCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : environments) {
            environmentCat.add(child);
        }
        TreeModel appCat = new BaseTreeModel();
        appCat.set("text", "Applications");
        appCat.set("tagType", TagModel.TYPE_GROUP);
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
