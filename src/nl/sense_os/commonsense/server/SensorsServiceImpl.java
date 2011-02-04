package nl.sense_os.commonsense.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.services.SensorsService;
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
public class SensorsServiceImpl extends RemoteServiceServlet implements SensorsService {

    private static final Logger log = Logger.getLogger("TagsServiceImpl");
    private static final long serialVersionUID = 1L;
    private int responseCode = 0;
    private String responseContent = "";

    private void doRequest(String url, String sessionId, String method, String data)
            throws WrongResponseException, DbConnectionException {

        // Get response from server
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache,max-age=10");

            // log.info(method + " " + connection.getURL().getPath());

            // perform method at URL
            if (null != data) {
                log.info(data);
                connection.setDoOutput(true);
                OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());
                w.write(data);
                w.close();
            }
            this.responseCode = connection.getResponseCode();
            this.responseContent = "";
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                this.responseContent += line;
            }
        } catch (MalformedURLException e) {
            log.severe("MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }
    }

    @Override
    public List<TreeModel> getAvailableSensors(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        String url = Constants.URL_SENSORS + "?owned=1";
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /sensors failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get sensors " + this.responseCode);
        }
        List<ModelData> sensors = parseSensors(this.responseContent);

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
        String url = Constants.URL_SENSORS + "?owned=1";
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /sensors failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get sensors " + this.responseCode);
        }
        List<ModelData> sensors = parseSensors(this.responseContent);

        HashMap<String, TreeModel> foundServices = new HashMap<String, TreeModel>();
        List<TreeModel> sensorsPerService = new ArrayList<TreeModel>();
        for (ModelData sensorModel : sensors) {
            // create TreeModel for this sensor
            TreeModel sensor = new BaseTreeModel(sensorModel.getProperties());

            // request all available services for this sensor
            url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services/available";
            doRequest(url, sessionId, "GET", null);
            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors/<id>/services/available failure: " + this.responseCode
                        + " " + this.responseContent);
                throw new WrongResponseException("failed to get available services "
                        + this.responseCode);
            }
            List<ModelData> sensorServices = parseAvailableServices(this.responseContent);

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

                // add sensor as child of service in foundServices. NB: take care to create a new
                // TreeModel object, otherwise we release the sensor from any other parent services
                service.add(new BaseTreeModel(sensor.getProperties()));
                foundServices.put(key, service);
            }
        }

        sensorsPerService.addAll(foundServices.values());

        return sensorsPerService;
    }

    @Override
    public TreeModel getGroupSensors(String sessionId, TreeModel group)
            throws DbConnectionException, WrongResponseException {

        List<TreeModel> groupTree = getSortedSensors(sessionId,
                "?alias=" + group.<String> get("id"));
        group.removeAll();
        for (TreeModel treeItem : groupTree) {
            group.add(treeItem);
        }
        return group;
    }

    @Override
    public List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException {

        return getSortedSensors(sessionId, "?owned=1");
    }

    private List<TreeModel> getSortedSensors(String sessionId, String urlSuffix)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        String url = Constants.URL_SENSORS + urlSuffix;
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET SENSORS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get sensors " + this.responseCode);
        }

        List<ModelData> unsortedSensors = parseSensors(this.responseContent);

        List<TreeModel> feedsSensors = new ArrayList<TreeModel>();
        List<TreeModel> deviceSensors = new ArrayList<TreeModel>();
        List<TreeModel> stateSensors = new ArrayList<TreeModel>();
        List<TreeModel> environmentSensors = new ArrayList<TreeModel>();
        List<TreeModel> apps = new ArrayList<TreeModel>();
        List<TreeModel> sorted = sortSensors(unsortedSensors, deviceSensors, environmentSensors,
                apps, feedsSensors, stateSensors);

        // handle nested devices
        List<TreeModel> devices = getDevices(sessionId, urlSuffix, deviceSensors);
        for (TreeModel cat : sorted) {
            if (cat.<String> get("text").equalsIgnoreCase("devices")) {
                cat.removeAll();
                for (TreeModel device : devices) {
                    cat.add(device);
                }
                break;
            }
        }

        return sorted;
    }
    private List<TreeModel> getDevices(String sessionId, String urlSuffix, List<TreeModel> sensors)
            throws WrongResponseException, DbConnectionException {

        HashMap<String, TreeModel> devicesMap = new HashMap<String, TreeModel>();
        for (TreeModel sensor : sensors) {
            String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/device"
                    + urlSuffix;
            doRequest(url, sessionId, "GET", null);

            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors/<id>/device failure: " + this.responseCode + " "
                        + this.responseContent);
                throw new WrongResponseException("failed to get sensor's device "
                        + this.responseCode);
            }

            ModelData deviceModel = parseDevice(this.responseContent);

            TreeModel device = devicesMap.get(deviceModel.<String> get("id"));
            if (null == device) {
                device = new BaseTreeModel(deviceModel.getProperties());
            }
            device.add(sensor);
            devicesMap.put(device.<String> get("id"), device);
        }

        List<TreeModel> devices = new ArrayList<TreeModel>(devicesMap.values());

        return devices;
    }
    @Override
    public List<TreeModel> getMyServices(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // request all sensors from server
        String url = Constants.URL_SENSORS + "?owned=1";
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /sensors failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get sensors " + this.responseCode);
        }
        List<ModelData> sensors = parseSensors(this.responseContent);

        // gather the services that are connected to the sensors
        HashMap<String, TreeModel> foundServices = new HashMap<String, TreeModel>();
        for (ModelData sensor : sensors) {
            url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/services";
            doRequest(url, sessionId, "GET", null);

            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors/<id>/services failure: " + this.responseCode + " "
                        + this.responseContent);
                throw new WrongResponseException("failed to get services " + this.responseCode);
            }
            List<ModelData> sensorServices = parseServices(this.responseContent);

            // add the services to the list of know services
            for (ModelData serviceModel : sensorServices) {
                TreeModel service = foundServices.get(serviceModel.<String> get("id"));
                if (null != service) {
                    service.add(new BaseTreeModel(sensor.getProperties()));
                } else {
                    service = new BaseTreeModel(serviceModel.getProperties());
                    service.add(new BaseTreeModel(sensor.getProperties()));
                }
                foundServices.put(serviceModel.<String> get("id"), service);
            }
        }

        // create a tree from the gathered services
        List<TreeModel> services = new ArrayList<TreeModel>();
        services.addAll(foundServices.values());

        // request the sensor name that is associated with each service
        for (TreeModel service : services) {
            url = Constants.URL_SENSORS + "/" + service.<String> get("id");
            doRequest(url, sessionId, "GET", null);

            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors/<id>: " + this.responseCode + " " + this.responseContent);
                throw new WrongResponseException("failed to get service details "
                        + this.responseCode);
            }
            ModelData serviceSensor = parseSensor(this.responseContent);
            for (String property : serviceSensor.getPropertyNames()) {
                service.set(property, serviceSensor.get(property));
            }
        }

        return services;
    }

    private boolean isSensorAvailable(String sessionId, String sensorId, TreeModel service)
            throws WrongResponseException, DbConnectionException {

        // request all available services for this sensor
        String url = Constants.URL_SENSORS + "/" + sensorId + "/services/available";
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /sensors/<id>/services/available failure: " + this.responseCode + " "
                    + this.responseContent);
            throw new WrongResponseException("failed to get available services "
                    + this.responseCode);
        }
        List<ModelData> sensorServices = parseAvailableServices(this.responseContent);

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
                properties.put("text", properties.get("service_name"));

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

    private ModelData parseDevice(String response) throws WrongResponseException {
        try {
            JSONObject device = (JSONObject) new JSONObject(response).get("device");

            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("id", device.getString("id"));
            properties.put("type", device.getString("type"));
            properties.put("uuid", device.getString("uuid"));

            // front end-only properties
            properties.put("tagType", TagModel.TYPE_DEVICE);
            properties.put("text", properties.get("type"));

            return new BaseModelData(properties);

        } catch (JSONException e) {
            log.severe("GET /devices/<id> JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> parseDevices(String response) throws WrongResponseException {
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray devices = (JSONArray) new JSONObject(response).get("devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", device.getString("id"));
                properties.put("type", device.getString("type"));
                properties.put("uuid", device.getString("uuid"));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_DEVICE);
                properties.put("text", properties.get("type"));

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET /devices JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private ModelData parseSensor(String response) throws WrongResponseException {
        // log.info("GET /sensors/<id> response: \'" + this.responseContent + "\'");

        // Convert JSON response to sensor model
        try {
            JSONObject sensor = (JSONObject) new JSONObject(response).get("sensor");

            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("id", sensor.getString("id"));
            // properties.put("data_type_id", sensor.getString("data_type_id"));
            properties.put("pager_type", sensor.getString("pager_type"));
            properties.put("data_type", sensor.getString("data_type"));
            properties.put("device_type", sensor.getString("device_type"));
            properties.put("name", sensor.getString("name"));
            properties.put("type", sensor.getString("type"));
            if (sensor.has("data_structure")) {
                properties.put("data_structure", sensor.getString("data_structure"));
            }
            if (sensor.has("owner_id")) {
                properties.put("owner_id", sensor.getString("owner_id"));
            }

            // front end-only properties
            properties.put("tagType", TagModel.TYPE_SENSOR);
            String name = (String) properties.get("name");
            String deviceType = (String) properties.get("device_type");
            if (name.equals(deviceType)) {
                properties.put("text", name);
            } else {
                properties.put("text", name + " (" + deviceType + ")");
            }

            return new BaseModelData(properties);

        } catch (JSONException e) {
            log.severe("GET /sensors/<id> JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> parseSensors(String response) throws DbConnectionException,
            WrongResponseException {

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray sensors = (JSONArray) new JSONObject(response).get("sensors");
            for (int i = 0; i < sensors.length(); i++) {
                JSONObject sensor = sensors.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", sensor.getString("id"));
                // properties.put("data_type_id", sensor.getString("data_type_id"));
                properties.put("pager_type", sensor.getString("pager_type"));
                if (sensor.has("data_type")) {
                    properties.put("data_type", sensor.getString("data_type"));
                }
                if (sensor.has("data_type_id")) {
                    properties.put("data_type_id", sensor.getString("data_type_id"));
                }
                properties.put("device_type", sensor.getString("device_type"));
                properties.put("name", sensor.getString("name"));
                properties.put("type", sensor.getString("type"));
                if (sensor.has("data_structure")) {
                    properties.put("data_structure", sensor.getString("data_structure"));
                }
                if (sensor.has("owner_id")) {
                    properties.put("owner_id", sensor.getString("owner_id"));
                }

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

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET /sensors JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> parseServices(String response) throws WrongResponseException {
        // log.info("GET /sensors/<id>/services response: \'" + response + "\'");

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray services = (JSONArray) new JSONObject(response).get("services");
            for (int i = 0; i < services.length(); i++) {
                JSONObject sensor = services.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", sensor.getString("id"));
                properties.put("service_name", sensor.getString("name")); // !! different field name
                properties.put("data_fields", sensor.getString("data_fields"));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_SERVICE);
                properties.put("text", properties.get("service_name"));

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

    private List<TreeModel> getAvailableDevices(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException {
        String url = Constants.URL_DEVICES;
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /devices failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get devices " + this.responseCode);
        }

        // Convert JSON response to list of tags
        List<ModelData> models = parseDevices(this.responseContent);

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

    /**
     * Creates a list of devices with nested sensors by getting a list of devices and then getting
     * each device's sensors. This is more efficient than the approach used in
     * {@link #getDevices(String, String, List)}, but does not work for shared sensors because their
     * devices are usually not shared.
     * 
     * @param sessionId
     *            for authentication at CommonSense
     * @param owned
     *            optional 'owned' url parameter, unused if null
     * @param alias
     *            optional 'alias' url parameter, unused if null
     * @return list of devices with nested sensors
     * @throws DbConnectionException
     * @throws WrongResponseException
     */
    @SuppressWarnings("unused")
    private List<TreeModel> requestDevices(String sessionId, String owned, String alias)
            throws DbConnectionException, WrongResponseException {

        String urlSuffix = "";
        if (null != owned) {
            urlSuffix += "?" + owned;
            if (null != alias) {
                urlSuffix += "&" + alias;
            }
        } else if (null != alias) {
            urlSuffix += "?" + alias;
        }

        String url = Constants.URL_DEVICES + urlSuffix;
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /devices failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get devices " + this.responseCode);
        }

        // Convert JSON response to list of tags
        List<ModelData> models = parseDevices(this.responseContent);

        // add child sensors for each device
        List<TreeModel> devices = new ArrayList<TreeModel>();
        for (ModelData model : models) {

            // convert "flat" device ModelData to TreeModel
            TreeModel device = new BaseTreeModel(model.getProperties());

            // get the device's sensors
            String deviceId = model.<String> get("id");
            List<ModelData> deviceSensors = requestDeviceSensors(sessionId, deviceId);
            for (ModelData childSensor : deviceSensors) {
                TreeModel sensor = new BaseTreeModel(childSensor.getProperties());
                device.add(sensor);
            }

            // add device to the tags
            devices.add(device);
        }
        return devices;
    }

    private List<ModelData> requestDeviceSensors(String sessionId, String deviceId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_DEVICE_SENSORS.replace("<id>", deviceId);
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /devices/" + deviceId + "/sensors failure: " + this.responseCode + " "
                    + this.responseContent);
            throw new WrongResponseException("failed to get device sensors " + this.responseCode);
        }

        // Convert JSON response to list of tags
        return parseSensors(this.responseContent);
    }

    private List<TreeModel> sortSensors(List<ModelData> unsorted, List<TreeModel> devices,
            List<TreeModel> environments, List<TreeModel> apps, List<TreeModel> feeds,
            List<TreeModel> states) throws DbConnectionException, WrongResponseException {

        // convert the sensor models into TreeModels
        for (ModelData sensorModel : unsorted) {
            TreeModel sensor = new BaseTreeModel(sensorModel.getProperties());
            int type = Integer.parseInt(sensor.<String> get("type"));
            switch (type) {
                case 0 :
                    feeds.add(sensor);
                    break;
                case 1 :
                    devices.add(sensor);
                    break;
                case 2 :
                    states.add(sensor);
                    break;
                case 3 :
                    environments.add(sensor);
                    break;
                case 4 :
                    apps.add(sensor);
                    break;
                default :
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
        sorted.add(feedCat);
        sorted.add(deviceCat);
        sorted.add(stateCat);
        sorted.add(environmentCat);
        sorted.add(appCat);

        return sorted;
    }
}
