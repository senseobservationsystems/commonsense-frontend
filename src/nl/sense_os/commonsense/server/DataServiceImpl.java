package nl.sense_os.commonsense.server;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
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

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements DataService {

    private static final String URL_BASE = "http://demo.almende.com/commonSense/gae/";
    private static final String URL_LOGIN = URL_BASE + "login.php";
    private static final String URL_GET_PHONE_DETAILS = URL_BASE + "get_phone_details.php";
    private static final String URL_GET_PHONE_SENSORS = URL_BASE + "get_phone_sensors.php";
    private static final String URL_GET_SENSOR_DATA = URL_BASE + "get_sensor_data.php";
    private static final String USER_SESSION = "GWTAppUser";

    private void setUserInSession(User user) {
        HttpSession session = getThreadLocalRequest().getSession();
        session.setAttribute(USER_SESSION, user);
    }

    private User getUserFromSession() {
        HttpSession session = getThreadLocalRequest().getSession();
        return (User) session.getAttribute(USER_SESSION);
    }

    public UserModel checkLogin(String name, String password) {
        try {
            final URL url = new URL(URL_LOGIN + "?email=" + name + "&password=" + password);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            line = reader.readLine();
            reader.close();
            if (!line.toLowerCase().contains("ok")) {
                return null;
            } else {
                User user = new User();
                user.setName(name);
                user.setPassword(password);
                setUserInSession(user);
                return UserConverter.entityToModel(user);
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return null;
    }

    public void logout() {
        HttpSession session = getThreadLocalRequest().getSession();
        if (session != null)
            session.invalidate();
    }

    public UserModel isSessionAlive() {
        User user = getUserFromSession();
        if ((user != null) && (user.getName().length() != 0)) {
            System.out.println("User " + user.getName() + " is already logged in");
            return UserConverter.entityToModel(user);
        }
        return null;
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
        } catch (IOException e) {
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
        }
        return phoneList;
    }

    public List<SenseTreeModel> getSensors(String phoneId) {
        List<SenseTreeModel> sensorList = new ArrayList<SenseTreeModel>();
        String jsonText = "";

        User user = getUserFromSession();

        // Get json object
        try {
            final URL url = new URL(URL_GET_PHONE_SENSORS + "?email=" + user.getName()
                    + "&password=" + user.getPassword() + "&sp_id=" + phoneId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
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
        }
        return sensorList;
    }

    private static final Logger log = Logger.getLogger("DataServiceImpl");

    @Override
    public List<SensorValueModel> getSensorValues(String phoneId, String sensorId, Timestamp begin,
            Timestamp end) {
        List<SensorValueModel> sensorValueList = new ArrayList<SensorValueModel>();
        String jsonText = "";

        User user = getUserFromSession();

        // Get json object
        try {
            // remove microseconds from timestamps (otherwise comparison in php won't succeed)
            begin.setNanos(0);
            end.setNanos(0);

            String beginTime = TimestampConverter.timestampToMicroEpoch(begin);
            String endTime = TimestampConverter.timestampToMicroEpoch(end);

            final URL url = new URL(URL_GET_SENSOR_DATA + "?email=" + user.getName() + "&password="
                    + user.getPassword() + "&sp_id=" + phoneId + "&sensor_type=" + sensorId
                    + "&ts_begin=" + beginTime + "&ts_end=" + endTime);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
            log.warning("MalformedUrlException in getSensorValues");
        } catch (IOException e) {
            log.warning("IOException in getSensorValues");
        }

        // Convert to object
        JSONArray sensorValues;
        try {
            sensorValues = (JSONArray) new JSONObject(jsonText).get("sensor_data");

            for (int i = 0; i < sensorValues.length(); i++) {
                JSONObject jsonSensorValue = (JSONObject) sensorValues.get(i);
                SensorValue sensorValue = SensorValueConverter.jsonToEntity(jsonSensorValue);
                sensorValueList.add(SensorValueConverter.entityToModel(sensorValue));
            }
        } catch (JSONException e) {
            log.warning("JSONException in getSensorValues:");
            logLongString(e.getMessage());
        }
        return sensorValueList;
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
}
