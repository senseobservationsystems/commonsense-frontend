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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.services.TagService;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

/**
 * Servlet to provide "tags" to the client. These can be different from the standard sensors and
 * devices structure that the CommonSense API exposes.
 */
public class TagServiceImpl extends RemoteServiceServlet implements TagService {

    private static final Logger log = Logger.getLogger("TagServiceImpl");
    private static final long serialVersionUID = 1L;

    private List<ModelData> getDevices(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // Get response from server
        String response = "";
        try {
            URL url = new URL(Constants.URL_DEVICES);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");

            // perform GET method at URL
            final int statusCode = connection.getResponseCode();
            final String message = connection.getResponseMessage();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // log.info("GET DEVICES " + statusCode + " " + message);

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } else {
                log.severe("GET DEVICES failure: " + statusCode + " " + message);
                throw new WrongResponseException("failed to get devices " + statusCode);
            }
        } catch (MalformedURLException e) {
            log.severe("GET DEVICES MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("GET DEVICES IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray devices = (JSONArray) new JSONObject(response).get("devices");
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

    private List<ModelData> getDeviceSensors(String sessionId, String deviceId)
            throws DbConnectionException, WrongResponseException {

        // Get response from server
        String response = "";
        try {
            URL url = new URL(Constants.URL_DEVICE_SENSORS.replace("<id>", deviceId));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");

            // perform GET method at URL
            final int statusCode = connection.getResponseCode();
            final String message = connection.getResponseMessage();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // log.info("GET DEVICE SENSORS " + statusCode + " " + message);

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } else {
                log.severe("GET DEVICE SENSORS failure: " + statusCode + " " + message);
                throw new WrongResponseException("failed to get device sensors " + statusCode);
            }
        } catch (MalformedURLException e) {
            log.severe("GET DEVICE SENSORS MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("GET DEVICE SENSORS IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray sensors = (JSONArray) new JSONObject(response).get("sensors");
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

    private List<ModelData> getSensors(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // Get response from server
        String response = "";
        try {
            URL url = new URL(Constants.URL_SENSORS);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");

            // perform GET method at URL
            final int statusCode = connection.getResponseCode();
            final String message = connection.getResponseMessage();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // log.info("GET SENSORS " + statusCode + " " + message);

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } else {
                log.severe("GET SENSORS failure: " + statusCode + " " + message);
                throw new WrongResponseException("failed to get sensors " + statusCode);
            }
        } catch (MalformedURLException e) {
            log.severe("GET SENSORS MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("GET SENSORS IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }

        // Convert JSON response to list of tags
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray sensors = (JSONArray) new JSONObject(response).get("sensors");
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

    @Override
    public List<TreeModel> getTags(String sessionId) throws DbConnectionException,
            WrongResponseException {

        List<ModelData> sensors = getSensors(sessionId);
        List<ModelData> devices = getDevices(sessionId);

        // convert the devices ModelData into TreeModels and nest them
        List<TreeModel> tags = new ArrayList<TreeModel>();
        for (ModelData device : devices) {

            // convert "flat" device ModelData to TreeModel
            TreeModel deviceTag = new BaseTreeModel(device.getProperties());

            // get the device's sensors
            List<ModelData> deviceSensors = getDeviceSensors(sessionId, device.<String> get("id"));
            for (ModelData childSensor : deviceSensors) {
                deviceTag.add(new BaseTreeModel(childSensor.getProperties()));

                // remove sensor from list of "non-physical sensors"
                List<ModelData> toRemove = new ArrayList<ModelData>();
                for (ModelData sensor : sensors) {
                    if (sensor.<String> get("id").equals(childSensor.<String> get("id"))) {
                        toRemove.add(sensor);
                    }
                }
                sensors.removeAll(toRemove);
            }

            // add device to the tags
            tags.add(deviceTag);
        }

        // iterate over the remaining sensors (those are not connected to a device)
        for (ModelData sensor : sensors) {
            tags.add(new BaseTreeModel(sensor.getProperties()));
        }

        return tags;
    }
}
