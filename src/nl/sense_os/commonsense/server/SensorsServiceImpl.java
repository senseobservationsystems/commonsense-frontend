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
            connection.setConnectTimeout(30000);
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

    private List<ModelData> getAllSensors(String sessionId, String urlSuffix)
            throws WrongResponseException, DbConnectionException {

        List<ModelData> sensors = new ArrayList<ModelData>();

        int perPage = 1000;
        long total = perPage + 1;
        int fetched = 0;
        int page = 0;
        while (total > fetched) {
            String url = Constants.URL_SENSORS + "?per_page=" + perPage + "&page=" + page
                    + urlSuffix;
            doRequest(url, sessionId, "GET", null);
            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors failure: " + this.responseCode + " "
                        + this.responseContent);
                throw new WrongResponseException("failed to get sensors " + this.responseCode);
            }
            total = parseSensors(this.responseContent, sensors);
            fetched = sensors.size();
            page++;
        }
        return sensors;
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

    @Override
    public List<TreeModel> getAvailableSensors(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        List<ModelData> sensors = getAllSensors(sessionId, "&owned=1");

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

                foundServices.put(key, service);
            }
        }

        return new ArrayList<TreeModel>(foundServices.values());
    }
    private List<TreeModel> getDevices(String sessionId, String owned, String alias)
            throws WrongResponseException, DbConnectionException {

        String urlSuffix = "";
        if (null != owned) {
            urlSuffix += "&owned=" + owned;
        }
        if (null != alias) {
            urlSuffix += "&alias=" + alias;
        }

        // get list of devices
        String url = Constants.URL_DEVICES + "?per_page=1000" + urlSuffix;
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /devices failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get devices " + this.responseCode);
        }

        List<ModelData> deviceModels = parseDevices(this.responseContent);

        List<TreeModel> devices = new ArrayList<TreeModel>();
        for (ModelData deviceModel : deviceModels) {
            TreeModel device = new BaseTreeModel(deviceModel.getProperties());

            // get the device's sensors
            url = Constants.URL_DEVICE_SENSORS.replaceFirst("<id>", deviceModel.<String> get("id"))
                    + "?per_page=1000" + urlSuffix;
            doRequest(url, sessionId, "GET", null);

            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /devices/<id>/sensors failure: " + this.responseCode + " "
                        + this.responseContent);
                throw new WrongResponseException("failed to get device's sensors "
                        + this.responseCode);
            }

            // parse the response to get the sensors
            List<ModelData> sensors = new ArrayList<ModelData>();
            parseSensors(this.responseContent, sensors);

            // add the sensors to the device
            for (ModelData sensorModel : sensors) {
                TreeModel sensor = new BaseTreeModel(sensorModel.getProperties());
                sensor.set("owned", owned);
                sensor.set("alias", alias);
                device.add(sensor);
            }

            devices.add(device);
        }

        return devices;
    }

    @Override
    public TreeModel getGroupSensors(String sessionId, TreeModel group)
            throws DbConnectionException, WrongResponseException {

        final String alias = group.<String> get("id");
        List<TreeModel> groupTree = getSortedSensors(sessionId, null, alias);
        group.removeAll();
        for (TreeModel treeItem : groupTree) {
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
        List<ModelData> sensors = getAllSensors(sessionId, "&owned=1");

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
            doRequest(url, sessionId, "GET", null);

            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors/<id>/sensors: " + this.responseCode + " "
                        + this.responseContent);
                throw new WrongResponseException("failed to get service source sensors "
                        + this.responseCode);
            }
            List<ModelData> serviceSources = new ArrayList<ModelData>();
            parseSensors(this.responseContent, serviceSources);

            for (ModelData sourceSensor : serviceSources) {
                service.add(new BaseTreeModel(sourceSensor.getProperties()));
            }
        }

        return services;
    }
    private List<TreeModel> getSortedSensors(String sessionId, String owned, String alias)
            throws WrongResponseException, DbConnectionException {

        // request all sensors from server
        String urlSuffix = "";
        if (null != owned) {
            urlSuffix += "&owned=" + owned;
        }
        if (null != alias) {
            urlSuffix += "&alias=" + alias;
        }
        List<ModelData> unsortedSensors = getAllSensors(sessionId, urlSuffix);

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
        List<TreeModel> devices = getDevices(sessionId, owned, alias);
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

    @SuppressWarnings("unused")
    private ModelData parseDevice(String response) throws WrongResponseException {
        try {
            JSONObject device = (JSONObject) new JSONObject(response).get("device");

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

            return new BaseModelData(properties);

        } catch (JSONException e) {
            log.severe("GET /devices/<id> JSONException: " + e.getMessage());
            log.severe("Raw response: " + response);
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
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }
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
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }
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
        List<ModelData> sensors = new ArrayList<ModelData>();
        parseSensors(this.responseContent, sensors);
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
