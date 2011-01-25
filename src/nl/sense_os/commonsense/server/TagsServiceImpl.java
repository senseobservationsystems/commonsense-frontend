package nl.sense_os.commonsense.server;

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

import nl.sense_os.commonsense.client.services.TagsService;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

/**
 * Servlet to provide "tags" to the client. These can be different from the standard sensors and
 * devices structure that the CommonSense API exposes.
 */
public class TagsServiceImpl extends RemoteServiceServlet implements TagsService {

    private static final Logger log = Logger.getLogger("TagsServiceImpl");
    private static final long serialVersionUID = 1L;
    private int responseCode = 0;
    private String responseContent;

    private void doRequest(String url, String sessionId, String method, String data)
            throws WrongResponseException, DbConnectionException {

        // Get response from server
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");

            // perform method at URL
            if (null != data) {
                connection.setDoOutput(true);
                OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());
                w.write(data);
                w.close();
            }
            connection.connect();
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
    public List<TreeModel> getGroupSensors(String sessionId) {
        return null;
    }

    private void getSensorTags(String sessionId, List<TreeModel> feeds, List<TreeModel> device,
            List<TreeModel> state, List<TreeModel> environment, List<TreeModel> app)
            throws DbConnectionException, WrongResponseException {

        // request all sensors from server
        List<ModelData> models = requestSensors(sessionId);

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

    @Override
    public List<TreeModel> getTags(String sessionId) throws DbConnectionException,
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

    private List<ModelData> requestDevices(String sessionId) throws DbConnectionException,
            WrongResponseException {

        String url = Constants.URL_DEVICES;     
        doRequest(url, sessionId, "GET", null);        

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET DEVICES failure: " + this.responseCode + " " + this.responseContent);
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
            log.severe("GET DEVICES JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> requestDeviceSensors(String sessionId, String deviceId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_DEVICE_SENSORS.replace("<id>", deviceId);
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET DEVICES failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get devices " + this.responseCode);
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

    private List<ModelData> requestSensors(String sessionId) throws DbConnectionException,
            WrongResponseException {

        String url = Constants.URL_SENSORS;
        doRequest(url, sessionId, "GET", null);

        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET DEVICES failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get devices " + this.responseCode);
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
                properties.put("data_type", sensor.getString("data_type"));
                properties.put("device_type", sensor.getString("device_type"));
                properties.put("name", sensor.getString("name"));
                properties.put("type", sensor.getString("type"));
                properties.put("data_structure", sensor.getString("data_structure"));
                properties.put("tagType", TagModel.TYPE_SENSOR);

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET SENSORS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }
}
