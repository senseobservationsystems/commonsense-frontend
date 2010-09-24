package nl.sense_os.commonsense.server;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
import nl.sense_os.commonsense.dto.exceptions.TooMuchDataException;
import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;
import nl.sense_os.commonsense.server.data.FloatValue;
import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.User;
import nl.sense_os.commonsense.server.utility.BooleanValueConverter;
import nl.sense_os.commonsense.server.utility.FloatValueConverter;
import nl.sense_os.commonsense.server.utility.JsonValueConverter;
import nl.sense_os.commonsense.server.utility.StringValueConverter;
import nl.sense_os.commonsense.server.utility.TimestampConverter;
import nl.sense_os.commonsense.server.utility.UserConverter;

public class DataServiceImpl extends RemoteServiceServlet implements DataService {

    private static final Logger log = Logger.getLogger("DataServiceImpl");
    private static final long serialVersionUID = 1L;
    private static final String URL_BASE = "http://demo.almende.com/commonSense2/gae/";
    private static final String URL_GET_SENSOR_DATA = URL_BASE + "get_sensor_data.php";
    private static final String URL_GET_SENSOR_DATA_PAGED = URL_BASE + "get_sensor_data_paged.php";
    private static final String URL_GET_TAGS = URL_BASE + "get_tags.php";
    private static final String URL_LOGIN = URL_BASE + "login.php";
    private static final String USER_SESSION = "GWTAppUser";

    @Override
    public UserModel checkLogin(String name, String password) throws DbConnectionException,
            WrongResponseException {

        // get response from server
        String response = "";
        try {
            final URL url = new URL(URL_LOGIN + "?email=" + name + "&password=" + password);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            response = reader.readLine();
            reader.close();
        } catch (MalformedURLException e) {
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            throw (new DbConnectionException(e.getMessage()));
        }

        // read user id from JSON response
        try {
            JSONObject json = new JSONObject(response);

            final int id = json.getInt("user_id");
            User user = new User(id, name, password);
            setUserInSession(user);

            return UserConverter.entityToModel(user);

        } catch (JSONException e) {
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    @Override
    public TaggedDataModel getSensorValuesPaged(TagModel tag, int offset, int limit)
            throws TooMuchDataException, DbConnectionException, WrongResponseException {

        final User user = getUserFromSession();
        final int sensorType = tag.getTaggedId();
        final int deviceId = tag.getParentId();

        // Get response from CommonSense
        String response = "";
        try {
            URL url = new URL(URL_GET_SENSOR_DATA_PAGED + "?email=" + user.getName() + "&password="
                    + user.getPassword() + "&d_id=" + deviceId + "&s_id=" + sensorType + "&limit="
                    + limit + "&offset=" + offset);

            FetchOptions fetchOptions = FetchOptions.Builder.withDefaults().setDeadline(30d);

            HTTPRequest httpReq = new HTTPRequest(url, HTTPMethod.GET, fetchOptions);
            URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
            HTTPResponse httpResponse = fetcher.fetch(httpReq);

            response = new String(httpResponse.getContent());
        } catch (MalformedURLException e) {
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            throw (new DbConnectionException(e.getMessage()));
        } catch (ResponseTooLargeException e) {
            throw (new TooMuchDataException(e.getMessage()));
        }

        // Convert JSON response to sensor value objects
        try {
            JSONObject json = new JSONObject(response);
            String dataType = json.getString("data_type");
            int totalCount = json.getInt("total");
            JSONArray jsonSensorValues = json.getJSONArray("data");
            SensorValueModel[] sensorValues;

            if (dataType.equals("json")) {
                sensorValues = JsonValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else if (dataType.equals("string")) {
                sensorValues = StringValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else if (dataType.equals("bool")) {
                sensorValues = BooleanValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else if (dataType.equals("float")) {
                sensorValues = FloatValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else
                sensorValues = new SensorValueModel[0];

            // return the result
            // TODO return a pagingloadresult ?
            return null;

        } catch (JSONException e) {
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    @Override
    public TaggedDataModel getSensorValues(TagModel tag, Date begin, Date end)
            throws TooMuchDataException, DbConnectionException, WrongResponseException {

        final User user = getUserFromSession();
        final int sensorType = tag.getTaggedId();
        final int deviceId = tag.getParentId();
        final String beginTime = TimestampConverter.timestampToEpochSecs(begin);
        final String endTime = TimestampConverter.timestampToEpochSecs(end);

        // Get response from CommonSense
        String response = "";
        try {
            URL url = new URL(URL_GET_SENSOR_DATA + "?email=" + user.getName() + "&password="
                    + user.getPassword() + "&d_id=" + deviceId + "&s_id=" + sensorType
                    + "&t_begin=" + beginTime + "&t_end=" + endTime);

            FetchOptions fetchOptions = FetchOptions.Builder.withDefaults().setDeadline(30d);

            HTTPRequest httpReq = new HTTPRequest(url, HTTPMethod.GET, fetchOptions);
            URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
            HTTPResponse httpResponse = fetcher.fetch(httpReq);

            response = new String(httpResponse.getContent());
        } catch (MalformedURLException e) {
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            throw (new DbConnectionException(e.getMessage()));
        } catch (ResponseTooLargeException e) {
            throw (new TooMuchDataException(e.getMessage()));
        }

        // Convert JSON response to sensor value objects
        try {
            JSONObject json = new JSONObject(response);
            String dataType = json.getString("data_type");
            JSONArray jsonSensorValues = json.getJSONArray("data");
            SensorValueModel[] sensorValues;

            if (dataType.equals("json")) {
                sensorValues = JsonValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else if (dataType.equals("string")) {
                sensorValues = StringValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else if (dataType.equals("bool")) {
                sensorValues = BooleanValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else if (dataType.equals("float")) {
                sensorValues = FloatValueConverter.jsonsToModels(jsonSensorValues,
                        tag.getParentId(), tag.getTaggedId());
            } else
                sensorValues = new SensorValueModel[0];

            // return the result
            return new TaggedDataModel(tag, sensorValues);

        } catch (JSONException e) {
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TaggedDataModel getIvoSensorValues(TagModel tag, Date begin, Date end)
            throws WrongResponseException {

        final PersistenceManager pm = PMF.get().getPersistenceManager();

        final Query query = pm.newQuery(FloatValue.class);
        int sensorType = tag.getTaggedId();
        int deviceId = tag.getParentId();
        query.setFilter("sensorType == " + sensorType + " && deviceId == " + deviceId
                + " && timestamp > begin");
        query.declareParameters("java.util.Date begin");

        log.warning(query.toString());

        TaggedDataModel result = null;
        try {
            List<FloatValue> queryResult = (List<FloatValue>) query.execute(begin);

            log.warning("Query result: " + queryResult.size() + " entries");

            SensorValueModel[] values = new SensorValueModel[queryResult.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = FloatValueConverter.entityToModel(queryResult.get(i));
            }

            result = new TaggedDataModel(tag, values);

        } catch (JSONException e) {
            log.severe("JSONException converting persisted sensor values to DTO");
            throw (new WrongResponseException(
                    "JSONException converting persisted sensor values to DTO"));
        } finally {
            query.closeAll();
            pm.close();
        }
        return result;
    }

    @Override
    public List<TagModel> getTags(TagModel rootTag) throws DbConnectionException,
            WrongResponseException {

        User user = getUserFromSession();
        if (rootTag == null) {
            final int userId = user.getId();
            rootTag = new TagModel("/" + userId + "/", userId, 0, TagModel.TYPE_USER);
        }

        // Get response from server
        String response = "";
        try {
            final String root = URLEncoder.encode(rootTag.getPath(), "UTF8");
            final URL url = new URL(URL_GET_TAGS + "?email=" + user.getName() + "&password="
                    + user.getPassword() + "&root=" + root);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            throw (new DbConnectionException(e.getMessage()));
        }

        // Convert JSON response to list of tags
        try {
            List<TagModel> tagsList = new ArrayList<TagModel>();
            JSONArray tags = (JSONArray) new JSONObject(response).get("tags");
            for (int i = 0; i < tags.length(); i++) {
                JSONObject tag = tags.getJSONObject(i);

                // get child tag's properties
                String path = tag.getString("path");
                int taggedId = tag.getInt("t_id");
                int parentId = tag.getInt("p_id");
                String typeString = tag.getString("type");
                int type = -1;
                if (typeString.equals("devices")) {
                    type = TagModel.TYPE_DEVICE;
                } else if (typeString.equals("group")) {
                    type = TagModel.TYPE_GROUP;
                } else if (typeString.equals("sensor_type")) {
                    type = TagModel.TYPE_SENSOR;
                } else if (typeString.equals("users")) {
                    type = TagModel.TYPE_USER;
                }

                tagsList.add(new TagModel(path, taggedId, parentId, type));
            }

            // return list of tags
            return tagsList;

        } catch (JSONException e) {
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private User getUserFromSession() {
        HttpSession session = getThreadLocalRequest().getSession();
        return (User) session.getAttribute(USER_SESSION);
    }

    @Override
    public UserModel isSessionAlive() {
        User user = getUserFromSession();
        if ((user != null) && (user.getName().length() != 0)) {
            System.out.println("User " + user.getName() + " is already logged in");
            return UserConverter.entityToModel(user);
        }
        return null;
    }

    @Override
    public void logout() {
        HttpSession session = getThreadLocalRequest().getSession();
        if (session != null)
            session.invalidate();
    }

    private void setUserInSession(User user) {
        HttpSession session = getThreadLocalRequest().getSession();
        session.setAttribute(USER_SESSION, user);
    }

    // private static final String URL_GET_PHONE_DETAILS = URL_BASE + "get_phone_details.php";
    // private static final String URL_GET_PHONE_SENSORS = URL_BASE + "get_phone_sensors.php";

    // public List<SenseTreeModel> getSensors(int phoneId) {
    // List<SenseTreeModel> sensorList = new ArrayList<SenseTreeModel>();
    // String jsonText = "";
    //
    // User user = getUserFromSession();
    //
    // // Get json object
    // try {
    // final URL url = new URL(URL_GET_PHONE_SENSORS + "?email=" + user.getName()
    // + "&password=" + user.getPassword() + "&device_id=" + phoneId);
    // BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    // String line;
    // while ((line = reader.readLine()) != null) {
    // jsonText += line;
    // }
    // reader.close();
    // } catch (MalformedURLException e) {
    // log.severe("MalFormedUrlException in getSensors");
    // log.severe(e.getMessage());
    // } catch (IOException e) {
    // log.severe("IOException in getSensors");
    // log.severe(e.getMessage());
    // }
    //
    // // Convert to object
    // JSONArray sensors;
    // try {
    // sensors = (JSONArray) new JSONObject(jsonText).get("sensors");
    // for (int i = 0; i < sensors.length(); i++) {
    // JSONObject jsonSensor = (JSONObject) sensors.get(i);
    // Sensor sensor = SensorConverter.jsonToEntity(jsonSensor, phoneId);
    // sensorList.add(SensorConverter.entityToModel(sensor));
    // }
    // } catch (JSONException e) {
    // log.severe("JSONException in getSensors");
    // log.severe(e.getMessage());
    // }
    // return sensorList;
    // }

    // public List<SenseTreeModel> getPhoneDetails() {
    //
    // List<SenseTreeModel> phoneList = new ArrayList<SenseTreeModel>();
    // String jsonText = "";
    //
    // User user = getUserFromSession();
    //
    // // Get json object
    // try {
    // final URL url = new URL(URL_GET_PHONE_DETAILS + "?email=" + user.getName()
    // + "&password=" + user.getPassword());
    // BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
    // String line;
    // while ((line = reader.readLine()) != null) {
    // jsonText += line;
    // }
    // reader.close();
    // } catch (MalformedURLException e) {
    // log.severe("MalFormedUrlException in getPhoneDetails");
    // log.severe(e.getMessage());
    // } catch (IOException e) {
    // log.severe("IOException in getPhoneDetails");
    // log.severe(e.getMessage());
    // }
    //
    // // Convert to object
    // JSONArray phones;
    // try {
    // phones = (JSONArray) new JSONObject(jsonText).get("phones");
    // for (int i = 0; i < phones.length(); i++) {
    // JSONObject jsonPhone = (JSONObject) phones.get(i);
    // Phone phone = PhoneConverter.jsonToEntity(jsonPhone);
    // // phone.setSensors(getPhoneSensors(phone));
    // phoneList.add(PhoneConverter.entityToModel(phone));
    // }
    // } catch (JSONException e) {
    // log.severe("JSONException in getPhoneDetails");
    // log.severe(e.getMessage());
    // }
    // return phoneList;
    // }
}
