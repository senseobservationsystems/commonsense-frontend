package nl.sense_os.commonsense.server;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.server.data.Phone;
import nl.sense_os.commonsense.server.data.Sensor;
import nl.sense_os.commonsense.server.data.SensorValue;
import nl.sense_os.commonsense.server.data.User;
import nl.sense_os.commonsense.server.utility.PhoneConverter;
import nl.sense_os.commonsense.server.utility.SensorConverter;
import nl.sense_os.commonsense.server.utility.SensorValueConverter;
import nl.sense_os.commonsense.server.utility.TimestampConverter;
import nl.sense_os.commonsense.server.utility.UserConverter;

public class DataServiceImpl extends RemoteServiceServlet implements DataService {

    private static final Logger log = Logger.getLogger("DataServiceImpl");
    private static final long serialVersionUID = 1L;
    private static final String URL_BASE = "http://demo.almende.com/commonSense2/gae/";
    private static final String URL_GET_PHONE_DETAILS = URL_BASE + "get_phone_details.php";
    private static final String URL_GET_PHONE_SENSORS = URL_BASE + "get_phone_sensors.php";
    private static final String URL_GET_TAGS = URL_BASE + "get_tags.php";
    private static final String URL_GET_SENSOR_DATA = URL_BASE + "get_sensor_data2.php";
    private static final String URL_LOGIN = URL_BASE + "login2.php";
    private static final String USER_SESSION = "GWTAppUser";

    public UserModel checkLogin(String name, String password) {
        // try to get JSON response
        String response = "";
        try {
            final URL url = new URL(URL_LOGIN + "?email=" + name + "&password=" + password);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            response = reader.readLine();
            reader.close();
        } catch (MalformedURLException e) {
            log.warning("MalFormedUrlException in checkLogin");
            log.warning(e.getMessage());
            return null;
        } catch (IOException e) {
            log.warning("IOException in checkLogin");
            log.warning(e.getMessage());
            return null;
        }

        // read user id from response
        try {
            JSONObject json = new JSONObject(response);

            final int id = json.getInt("user_id");
            User user = new User(id, name, password);
            setUserInSession(user);
            return UserConverter.entityToModel(user);
        } catch (JSONException e) {
            log.warning("JSONException parsing login response");
            log.warning(e.getMessage());
            return null;
        }
    }

    public List<SenseTreeModel> getPhoneDetails() {

        List<SenseTreeModel> phoneList = new ArrayList<SenseTreeModel>();
        String jsonText = "";

        User user = getUserFromSession();

        // Get json object
        try {
            final URL url = new URL(URL_GET_PHONE_DETAILS + "?email=" + user.getName()
                    + "&password=" + user.getPassword());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
            log.warning("MalFormedUrlException in getPhoneDetails");
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning("IOException in getPhoneDetails");
            log.warning(e.getMessage());
        }

        // Convert to object
        JSONArray phones;
        try {
            phones = (JSONArray) new JSONObject(jsonText).get("phones");
            for (int i = 0; i < phones.length(); i++) {
                JSONObject jsonPhone = (JSONObject) phones.get(i);
                Phone phone = PhoneConverter.jsonToEntity(jsonPhone);
                // phone.setSensors(getPhoneSensors(phone));
                phoneList.add(PhoneConverter.entityToModel(phone));
            }
        } catch (JSONException e) {
            log.warning("JSONException in getPhoneDetails");
            log.warning(e.getMessage());
        }
        return phoneList;
    }

    public List<TagModel> getTags(String rootTag) {
        List<TagModel> tagsList = new ArrayList<TagModel>();

        User user = getUserFromSession();
        if (rootTag == null) {
            rootTag = "/" + user.getId() + "/";
        } else {
            try {
                rootTag = URLEncoder.encode(rootTag, "UTF8");
            } catch (UnsupportedEncodingException e) {
                log.warning("UnsupportedEncodingException encoding login url");
                return null;
            }
        }

        // Get json object
        String response = "";
        try {
            final URL url = new URL(URL_GET_TAGS + "?email=" + user.getName() + "&password="
                    + user.getPassword() + "&root=" + rootTag);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
            log.warning("MalFormedUrlException in getTags");
            log.warning(e.getMessage());
            return null;
        } catch (IOException e) {
            log.warning("IOException in getTags");
            log.warning(e.getMessage());
            return null;
        }

        // Convert to object
        JSONArray tags;
        try {
            tags = (JSONArray) new JSONObject(response).get("tags");
            for (int i = 0; i < tags.length(); i++) {
                JSONObject tag = tags.getJSONObject(i);
                String typeString = tag.getString("type");

                // recognize type of the tagged thing
                int type = -1;
                if (typeString.equals("device")) {
                    type = TagModel.TYPE_DEVICE;
                } else if (typeString.equals("group")) {
                    type = TagModel.TYPE_GROUP;
                } else if (typeString.equals("sensor_type")) {
                    type = TagModel.TYPE_SENSOR;
                } else if (typeString.equals("user")) {
                    type = TagModel.TYPE_USER;
                }

                tagsList.add(new TagModel(tag.getString("path"), type));
            }
        } catch (JSONException e) {
            log.warning("JSONException in getTags");
            log.warning(e.getMessage());
        }
        return tagsList;
    }

    public List<SenseTreeModel> getSensors(int phoneId) {
        List<SenseTreeModel> sensorList = new ArrayList<SenseTreeModel>();
        String jsonText = "";

        User user = getUserFromSession();

        // Get json object
        try {
            final URL url = new URL(URL_GET_PHONE_SENSORS + "?email=" + user.getName()
                    + "&password=" + user.getPassword() + "&device_id=" + phoneId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
            log.warning("MalFormedUrlException in getSensors");
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning("IOException in getSensors");
            log.warning(e.getMessage());
        }

        // Convert to object
        JSONArray sensors;
        try {
            sensors = (JSONArray) new JSONObject(jsonText).get("sensors");
            for (int i = 0; i < sensors.length(); i++) {
                JSONObject jsonSensor = (JSONObject) sensors.get(i);
                Sensor sensor = SensorConverter.jsonToEntity(jsonSensor, phoneId);
                sensorList.add(SensorConverter.entityToModel(sensor));
            }
        } catch (JSONException e) {
            log.warning("JSONException in getSensors");
            log.warning(e.getMessage());
        }
        return sensorList;
    }

    public List<SensorValueModel> getSensorValues(TagModel tag, Timestamp begin, Timestamp end) {
        List<SensorValueModel> sensorValueList = new ArrayList<SensorValueModel>();
        String response = "";

        User user = getUserFromSession();

        // Get JSON response from CommonSense
        try {
            String beginTime = TimestampConverter.timestampToEpochSecs(begin);
            String endTime = TimestampConverter.timestampToEpochSecs(end);

            URL url = new URL(URL_GET_SENSOR_DATA + "?email=" + user.getName()
                    + "&password=" + user.getPassword() + "&tag=" + tag.getPath() + "&ts_begin="
                    + beginTime + "&ts_end=" + endTime);
            URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();            
            HTTPResponse httpResponse = fetcher.fetch(url);
            
            response = new String(httpResponse.getContent());
        } catch (MalformedURLException e) {
            log.warning("MalformedUrlException in getSensorValues");
        } catch (IOException e) {
            log.warning("IOException in getSensorValues");
        }

        // Convert to sensor value objects
        JSONArray sensorValues;
        try {
            JSONObject json = new JSONObject(response);
            String name = json.getString("name");
            String dataType = json.getString("data_type");
            sensorValues = json.getJSONArray("data");
            for (int i = 0; i < sensorValues.length(); i++) {
                JSONObject jsonSensorValue = (JSONObject) sensorValues.get(i);
                SensorValue sensorValue = SensorValueConverter.jsonToEntity(jsonSensorValue, name,
                        dataType);
                sensorValueList.add(SensorValueConverter.entityToModel(sensorValue));
            }
        } catch (JSONException e) {
            log.warning("JSONException in getSensorValues:");
            logLongString(e.getMessage());
        }
        return sensorValueList;
    }

    private User getUserFromSession() {
        HttpSession session = getThreadLocalRequest().getSession();
        return (User) session.getAttribute(USER_SESSION);
    }

    public UserModel isSessionAlive() {
        User user = getUserFromSession();
        if ((user != null) && (user.getName().length() != 0)) {
            System.out.println("User " + user.getName() + " is already logged in");
            return UserConverter.entityToModel(user);
        }
        return null;
    }

    /**
     * Writes very long strings in chunks of 500 characters to the log. Used to overcome the log
     * overwriting itself in Eclipse.
     * 
     * @param msg
     */
    private void logLongString(String msg) {

        for (int i = 0; i <= msg.length() / 500; i++) {
            int msgend = 500 * i + 500;
            if (msgend > msg.length()) {
                msgend = msg.length();
            }
            String s = msg.substring(500 * i, msgend);
            log.warning(s);
        }
    }

    public void logout() {
        HttpSession session = getThreadLocalRequest().getSession();
        if (session != null)
            session.invalidate();
    }

    private void setUserInSession(User user) {
        HttpSession session = getThreadLocalRequest().getSession();
        session.setAttribute(USER_SESSION, user);
    }
}
