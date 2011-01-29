package nl.sense_os.commonsense.server;

import nl.sense_os.commonsense.client.services.TagsService;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.InternalError;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

/**
 * Servlet to provide "tags" to the client. These can be different from the standard sensors and
 * devices structure that the CommonSense API exposes.
 */
public class TagsServiceImpl extends RemoteServiceServlet implements TagsService {

    private static final Logger log = Logger.getLogger("TagsServiceImpl");
    private static final long serialVersionUID = 1L;
    private int responseCode = 0;
    private String responseContent = "";

    @Override
    public void disconnectService(String sessionId, String sensorId, String serviceId)
            throws DbConnectionException, WrongResponseException {
        String url = Constants.URL_SENSORS + "/" + sensorId + "/services/" + serviceId;
        doRequest(url, sessionId, "DELETE", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("DELETE /sensors/<id>/services/<id> failure: " + this.responseCode + " "
                    + this.responseContent);
            throw new WrongResponseException("failed to delete service " + this.responseCode);
        }
    }

    private void doRequest(String url, String sessionId, String method, String data)
            throws WrongResponseException, DbConnectionException {

        // Get response from server
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache,max-age=10");

            log.info(method + " " + connection.getURL().getPath());

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
    public List<TreeModel> getAvailableServices(String sessionId) throws WrongResponseException,
            DbConnectionException {

        // request all sensors from server
        String url = Constants.URL_SENSORS + "?owned=1";
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /sensors failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get sensors " + this.responseCode);
        }
        List<ModelData> sensors = handleSensorsResponse(this.responseContent);

        HashMap<String, TreeModel> foundServices = new HashMap<String, TreeModel>();
        List<TreeModel> sensorsPerService = new ArrayList<TreeModel>();
        for (ModelData sensorModel : sensors) {
            // create TreeModel for this sensor
            TreeModel sensor = new BaseTreeModel(sensorModel.getProperties()); 
            
            // request all available services for this sensor
            url = Constants.URL_SENSORS + "/" + sensorModel.<String> get("id") + "/services/available";
            doRequest(url, sessionId, "GET", null);
            if (this.responseCode != HttpURLConnection.HTTP_OK) {
                log.severe("GET /sensors/<id>/services/available failure: " + this.responseCode + " " + this.responseContent);
                throw new WrongResponseException("failed to get available services " + this.responseCode);
            }
            List<ModelData> sensorServices = handleAvailableServicesResponse(this.responseContent);            
            
            for (ModelData sensorService : sensorServices) {
                TreeModel service = foundServices.get(sensorService.<String> get("name"));
                if (null != service) {
                    service.add(sensor);
                } else {
                    service = new BaseTreeModel(sensorService.getProperties());
                    service.add(sensor);
                }
                // add sensor as child of service in foundServices
                foundServices.put(sensorService.<String> get("name"), service);
            }         
        }
        
        sensorsPerService.addAll(foundServices.values());
        
        return sensorsPerService;
    }

    private void getDeviceTags(String sessionId, List<TreeModel> tags)
            throws DbConnectionException, WrongResponseException {

        // request device models from server
        List<ModelData> models = requestDevices(sessionId);

        // convert the devices ModelData into TreeModels and nest their physical sensors
        for (ModelData model : models) {

            // convert "flat" device ModelData to TreeModel
            TreeModel tag = new BaseTreeModel(model.getProperties());

            // get the device's sensors
            String deviceId = model.<String> get("id");
            List<ModelData> deviceSensors = requestDeviceSensors(sessionId, deviceId);
            for (ModelData childSensor : deviceSensors) {
                TreeModel sensorTag = new BaseTreeModel(childSensor.getProperties());
                tag.add(sensorTag);
            }

            // add device to the tags
            tags.add(tag);
        }
    }

    @Override
    public TreeModel getGroupSensors(String sessionId, TreeModel group)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_SENSORS + "?alias=" + group.<String> get("id") + "&owned=0";
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET GROUP SENSORS failure: " + this.responseCode + " "
                    + this.responseContent);
            throw new WrongResponseException("failed to get sensors " + this.responseCode);
        }

        List<ModelData> models = handleSensorsResponse(this.responseContent);

        group.removeAll();
        for (ModelData model : models) {
            group.add(new BaseTreeModel(model.getProperties()));
        }
        return group;
    }

    @Override
    public List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // categorized sensors
        List<TreeModel> feeds = new ArrayList<TreeModel>();
        List<TreeModel> devices = new ArrayList<TreeModel>();
        List<TreeModel> states = new ArrayList<TreeModel>();
        List<TreeModel> environments = new ArrayList<TreeModel>();
        List<TreeModel> apps = new ArrayList<TreeModel>();
        getSensorTags(sessionId, feeds, devices, states, environments, apps);

        // devices are special case
        devices = new ArrayList<TreeModel>();
        getDeviceTags(sessionId, devices);

        // create main groups
        TreeModel feedCat = new BaseTreeModel();
        feedCat.set("name", "Feeds");
        feedCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : feeds) {
            feedCat.add(child);
        }
        TreeModel deviceCat = new BaseTreeModel();
        deviceCat.set("name", "Devices");
        deviceCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : devices) {
            deviceCat.add(child);
        }
        TreeModel stateCat = new BaseTreeModel();
        stateCat.set("name", "States");
        stateCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : states) {
            stateCat.add(child);
        }
        TreeModel environmentCat = new BaseTreeModel();
        environmentCat.set("name", "Environments");
        environmentCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : environments) {
            environmentCat.add(child);
        }
        TreeModel appCat = new BaseTreeModel();
        appCat.set("name", "Applications");
        appCat.set("tagType", TagModel.TYPE_GROUP);
        for (TreeModel child : apps) {
            appCat.add(child);
        }

        List<TreeModel> tags = new ArrayList<TreeModel>();
        tags.add(feedCat);
        tags.add(deviceCat);
        tags.add(stateCat);
        tags.add(environmentCat);
        tags.add(appCat);

        return tags;
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
        List<ModelData> sensors = handleSensorsResponse(this.responseContent);

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
            List<ModelData> sensorServices = handleServicesResponse(this.responseContent);

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
            ModelData serviceSensor = handleSensorResponse(this.responseContent);
            for (String property : serviceSensor.getPropertyNames()) {
                service.set(property, serviceSensor.get(property));
            }
        }

        return services;
    }

    private void getSensorTags(String sessionId, List<TreeModel> feeds, List<TreeModel> device,
            List<TreeModel> state, List<TreeModel> environment, List<TreeModel> app)
            throws DbConnectionException, WrongResponseException {

        // request all sensors from server
        String url = Constants.URL_SENSORS + "?owned=1";
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET MY SENSORS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get my sensors " + this.responseCode);
        }

        List<ModelData> models = handleSensorsResponse(this.responseContent);

        // convert the sensor models into TreeModels
        for (ModelData model : models) {
            TreeModel tag = new BaseTreeModel(model.getProperties());
            int type = Integer.parseInt(model.<String> get("type"));
            switch (type) {
            case 0:
                feeds.add(tag);
                break;
            case 1:
                device.add(tag);
                break;
            case 2:
                state.add(tag);
                break;
            case 3:
                environment.add(tag);
                break;
            case 4:
                app.add(tag);
                break;
            default:
                log.warning("Unexpected sensor type: " + type);
            }
        }
    }

    private List<ModelData> handleAvailableServicesResponse(String response) throws WrongResponseException {
     // log.info("GET /sensors/<id>/services/available response: \'" + response + "\'");

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray services = (JSONArray) new JSONObject(response).get("available_services");
            for (int i = 0; i < services.length(); i++) {
                JSONObject sensor = services.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("name", sensor.getString("name")); // !! different field name
                properties.put("data_fields", sensor.getString("data_fields"));
                properties.put("tagType", TagModel.TYPE_SERVICE);

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

    private ModelData handleSensorResponse(String response) throws WrongResponseException {
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
            properties.put("tagType", TagModel.TYPE_SENSOR);

            return new BaseModelData(properties);

        } catch (JSONException e) {
            log.severe("GET /sensors/<id> JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> handleSensorsResponse(String response) throws DbConnectionException,
            WrongResponseException {

        // log.info("GET /sensors response: \'" + this.responseContent + "\'");

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
                properties.put("tagType", TagModel.TYPE_SENSOR);

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

    private List<ModelData> handleServicesResponse(String response) throws WrongResponseException {
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
                properties.put("tagType", TagModel.TYPE_SENSOR);

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

    private List<ModelData> requestDevices(String sessionId) throws DbConnectionException,
            WrongResponseException {

        String url = Constants.URL_DEVICES;
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET /devices failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get devices " + this.responseCode);
        }

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray devices = (JSONArray) new JSONObject(this.responseContent).get("devices");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", device.getString("id"));
                properties.put("type", device.getString("type"));
                properties.put("uuid", device.getString("uuid"));
                properties.put("tagType", TagModel.TYPE_DEVICE);

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

    private List<ModelData> requestDeviceSensors(String sessionId, String deviceId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_DEVICE_SENSORS.replace("<id>", deviceId);
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET DEVICE SENSORS failure: " + this.responseCode + " "
                    + this.responseContent);
            throw new WrongResponseException("failed to get device sensors " + this.responseCode);
        }

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray sensors = (JSONArray) new JSONObject(this.responseContent).get("sensors");
            for (int i = 0; i < sensors.length(); i++) {
                JSONObject sensor = sensors.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", sensor.getString("id"));
                properties.put("data_type_id", sensor.getString("data_type_id"));
                properties.put("pager_type", sensor.getString("pager_type"));
                properties.put("device_type", sensor.getString("device_type"));
                properties.put("name", sensor.getString("name"));
                properties.put("type", sensor.getString("type"));
                properties.put("tagType", TagModel.TYPE_SENSOR);

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET DEVICE SENSORS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    @Override
    public void shareSensors(String sessionId, List<TreeModel> sensors, TreeModel user)
            throws DbConnectionException, WrongResponseException, InternalError {

        for (TreeModel sensor : sensors) {
            String data = null;
            try {
                JSONObject dataJson = new JSONObject();
                JSONObject userJson = new JSONObject();
                userJson.put("id", user.<String> get("id"));
                dataJson.put("user", userJson);
                data = dataJson.toString();
            } catch (JSONException e) {
                log.severe("JSONException creating POST data");
                throw new InternalError("JSONException creating POST data.");
            }

            String url = Constants.URL_SENSORS + "/" + sensor.<String> get("id") + "/users.json";
            doRequest(url, sessionId, "POST", data);

            if (this.responseCode != HttpURLConnection.HTTP_CREATED) {
                log.severe("failed to share sensor: " + this.responseCode + " "
                        + this.responseContent);
                throw new WrongResponseException("failed to share sensor: " + responseCode);
            }
        }
    }
}
