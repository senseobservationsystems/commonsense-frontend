package nl.sense_os.commonsense.shared.client.communication;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.client.model.Group;
import nl.sense_os.commonsense.shared.client.util.Constants;
import nl.sense_os.commonsense.shared.client.util.Md5Hasher;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window.Location;

public class CommonSenseApi {

    public static class Urls {
        private static final String PATH_PREFIX = Constants.STABLE_MODE || Constants.RC_MODE
                || Constants.DEV_MODE ? "api/" : "";
        public static final String HOST = Constants.STABLE_MODE ? "common.sense-os.nl"
                : Constants.RC_MODE ? "rc.sense-os.nl"
                        : Constants.DEV_MODE ? "common.dev.sense-os.nl" : "api.sense-os.nl";
        public static final String PROTOCOL = "http";

        // main paths
        public static final String PATH_SENSORS = PATH_PREFIX + "sensors";
        public static final String PATH_GROUPS = PATH_PREFIX + "groups";
        public static final String PATH_USERS = PATH_PREFIX + "users";
        public static final String PATH_ENVIRONMENTS = PATH_PREFIX + "environments";
        public static final String PATH_LOGIN = PATH_PREFIX + "login";
        public static final String PATH_LOGIN_GOOGLE = "login/openID/google"; // no prefix!
        public static final String PATH_LOGOUT = PATH_PREFIX + "logout";
        public static final String PATH_PW_RESET = PATH_PREFIX + "resetPassword";
        public static final String PATH_FORGOT_PASSWORD = PATH_PREFIX + "requestPasswordReset";

        // sensors paths
        public static final String PATH_AVAIL_SERVICES = PATH_SENSORS + "/services/available";
        public static final String PATH_SENSOR_DATA = PATH_SENSORS + "/%1/data";
        public static final String PATH_SENSOR_USERS = PATH_SENSORS + "/%1/users";
        public static final String PATH_SENSOR_DEVICE = PATH_SENSORS + "/%1/device";
        public static final String PATH_SERVICE = PATH_SENSORS + "/%1/services/%2";
        public static final String PATH_SERVICE_METHODS = PATH_SERVICE + "/methods";
        public static final String PATH_CONNECTED_SENSORS = PATH_SENSORS + "/%1/sensors";

        // users paths
        public static final String PATH_CURRENT_USER = PATH_USERS + "/current";

        // groups paths
        public static final String PATH_GROUP_USERS = PATH_GROUPS + "/%1/users";
        public static final String PATH_ALL_GROUPS = PATH_GROUPS + "/all";

        // environments paths
        public static final String PATH_ENVIRONMENT_SENSORS = PATH_ENVIRONMENTS + "/%1/sensors";

        private Urls() {
            // do not instantiate
        }
    }

    private static final Logger LOG = Logger.getLogger(CommonSenseApi.class.getName());
    private static final String JSON_TYPE = "application/json";
    private static final String WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static void addEnvironmentSensors(RequestCallback callback, String environmentId,
            List<String> sensorIds) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_ENVIRONMENT_SENSORS.replace("%1", environmentId));
        String url = urlBuilder.buildString();

        // prepare body
        String sensorsArray = "[";
        for (String id : sensorIds) {
            sensorsArray += "{\"id\":" + id + "},";
        }
        sensorsArray = sensorsArray.substring(0, sensorsArray.length() - 1) + "]";
        String data = "{\"sensors\":" + sensorsArray + "}";

        // send request
        sendRequest(method, url, sessionId, data, callback);

    }

    public static void addGroupUser(RequestCallback callback, String groupId, String username) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUP_USERS.replace("%1", groupId));
        String url = urlBuilder.buildString();
        String data = "{\"user\":{\"username\":\"" + username + "\"}}";

        // send request
        sendRequest(method, url, sessionId, data, callback);

    }

    public static void addSensorData(RequestCallback callback, String sensorId, String value,
            long timestamp) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSOR_DATA.replace("%1", sensorId));
        String url = urlBuilder.buildString();

        // prepare body
        String data = "{\"data\":[";
        data += "{\"value\":\"" + value + "\",\"date\":"
                + NumberFormat.getFormat("#.#").format(timestamp / 1000) + "}";
        data += "]}";

        // send request
        sendRequest(method, url, sessionId, data, callback);

    }

    public static void addSensorDevice(RequestCallback callback, String sensorId, String deviceId,
            String deviceType, String deviceUuid) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSOR_DEVICE.replace("%1", sensorId));
        String url = urlBuilder.buildString();

        // prepare body
        String data = "{\"device\":{";
        data += "\"id\":\"" + deviceId + "\",";
        data += "\"type\":\"" + deviceType + "\",";
        data += "\"uuid\":\"" + deviceUuid + "\"}";
        data += "}";

        // send request
        sendRequest(method, url, sessionId, data, callback);

    }

    public static void createEnvironment(RequestCallback callback, String name, int floors,
            String gpsOutline, String position) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_ENVIRONMENTS);
        String url = urlBuilder.buildString();

        // prepare data
        String data = "{\"environment\":{";
        data += "\"name\":\"" + name + "\",";
        data += "\"floors\":" + floors + ",";
        data += "\"gps_outline\":\"" + gpsOutline + "\",";
        data += "\"position\":\"" + position + "\"}";
        data += "}";

        // send request
        sendRequest(method, url, sessionId, data, callback);

    }

    public static void createGroup(RequestCallback callback, Group group) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUPS);
        String url = urlBuilder.buildString();
        String groupString = new JSONObject(group).toString();
        String data = "{\"group\":" + groupString + "}";

        // send request
        sendRequest(method, url, sessionId, data, callback);

    }

    public static void createSensor(RequestCallback callback, String name, String displayName,
            String description, String dataType, String dataStructure) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSORS);
        String url = urlBuilder.buildString();

        // prepare body
        String sensor = "{";
        sensor += "\"name\":\"position\",";
        sensor += "\"display_name\":\"position\",";
        sensor += "\"device_type\":\"position\",";
        sensor += "\"data_type\":\"json\",";
        sensor += "\"data_structure\":\"" + dataStructure + "\"";
        sensor += "}";
        String data = "{\"sensor\":" + sensor + "}";

        // send request
        sendRequest(method, url, sessionId, data, callback);
    }

    public static void deleteEnvironment(RequestCallback callback, String environmentId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.DELETE;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_ENVIRONMENTS + "/" + environmentId);
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);

    }

    public static void deleteSensor(RequestCallback callback, String sensorId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.DELETE;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSORS + "/" + sensorId);
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void disconnectService(RequestCallback callback, String sensorId, String serviceId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.DELETE;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId));
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    /**
     * @param callback
     *            RequestCallback to handle HTTP response
     * @param username
     * @param email
     */
    public static void forgotPassword(RequestCallback callback, String username, String email) {

        // prepare request details
        Method method = RequestBuilder.POST;
        String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_FORGOT_PASSWORD).buildString();

        // prepare request data
        String requestData = null != username ? "username=" + URL.encode(username) : "email="
                + URL.encode(email);

        // send request
        try {
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
            builder.setHeader("Accept", JSON_TYPE);
            builder.sendRequest(requestData, callback);
        } catch (Exception e) {
            LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
            callback.onError(null, e);
        }
    }

    public static void getAllGroups(RequestCallback callback, Integer perPage, Integer page) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_ALL_GROUPS);
        if (null != page) {
            urlBuilder.setParameter("page", page.toString());
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage.toString());
        }
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void getAvailableServices(RequestCallback callback, String perPage, String page,
            String groupId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_AVAIL_SERVICES);
        if (null != page) {
            urlBuilder.setParameter("page", page);
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage);
        }
        if (null != groupId) {
            urlBuilder.setParameter("group_id", groupId);
        }
        String url = urlBuilder.buildString();

        sendRequest(method, url, sessionId, null, callback);
    }

    public static void getConnectedSensors(RequestCallback callback, String sensorId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_CONNECTED_SENSORS.replace("%1", sensorId));
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    /**
     * @param callback
     *            RequestCallback to handle HTTP response
     */
    public static void getCurrentUser(RequestCallback callback) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        Method method = RequestBuilder.GET;
        String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_CURRENT_USER).buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void getEnvironments(RequestCallback callback, String perPage, String page) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_ENVIRONMENTS);
        if (null != page) {
            urlBuilder.setParameter("page", page);
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage);
        }
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void getGroupDetails(RequestCallback callback, String groupId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUPS + "/" + groupId);
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);

    }

    public static void getGroups(RequestCallback callback, Integer perPage, Integer page) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUPS);
        if (null != page) {
            urlBuilder.setParameter("page", page.toString());
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage.toString());
        }
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void getGroupUsers(RequestCallback callback, String groupId, String perPage,
            String page) {
        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUP_USERS.replace("%1", groupId));
        if (null != page) {
            urlBuilder.setParameter("page", page);
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage);
        }
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    /**
     * @param callback
     *            RequestCallback to handle HTTP response
     * @param sensorId
     *            ID of the sensor to request data for
     * @param startDate
     * @param endDate
     * @param date
     * @param perPage
     * @param page
     * @param interval
     * @param next
     * @param last
     */
    public static void getSensorData(RequestCallback callback, String sensorId, String startDate,
            String endDate, String date, String perPage, String page, String interval, String next,
            String last, String sort) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSOR_DATA.replace("%1", sensorId));
        if (null != startDate) {
            urlBuilder.setParameter("start_date", startDate);
        }
        if (null != endDate) {
            urlBuilder.setParameter("end_date", endDate);
        }
        if (null != date) {
            urlBuilder.setParameter("date", date);
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage);
        }
        if (null != page) {
            urlBuilder.setParameter("page", page);
        }
        if (null != interval) {
            urlBuilder.setParameter("interval", interval);
        }
        if (null != last) {
            urlBuilder.setParameter("last", last);
        }
        if (null != next) {
            urlBuilder.setParameter("next", next);
        }
        if (null != sort) {
            urlBuilder.setParameter("sort", sort);
        }
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    /**
     * @param callback
     *            RequestCallback to handle HTTP response
     * @param perPage
     * @param page
     * @param shared
     * @param owned
     * @param physical
     * @param details
     * @param groupId
     */
    public static void getSensors(RequestCallback callback, Integer perPage, Integer page,
            Boolean shared, Boolean owned, Boolean physical, String details, String groupId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSORS);
        if (null != page) {
            urlBuilder.setParameter("page", page.toString());
        }
        if (null != perPage) {
            urlBuilder.setParameter("per_page", perPage.toString());
        }
        if (null != shared) {
            urlBuilder.setParameter("shared", shared ? "1" : "0");
        }
        if (null != owned) {
            urlBuilder.setParameter("owned", owned ? "1" : "0");
        }
        if (null != physical) {
            urlBuilder.setParameter("physical", physical ? "1" : "0");
        }
        if (null != details) {
            urlBuilder.setParameter("details", details);
        }
        if (null != groupId) {
            urlBuilder.setParameter("group_id", groupId);
        }
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void getServiceMethods(RequestCallback callback, String sensorId, String serviceId) {
        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.GET;
        UrlBuilder urlBuilder = new UrlBuilder()
                .setProtocol(Urls.PROTOCOL)
                .setHost(Urls.HOST)
                .setPath(Urls.PATH_SERVICE_METHODS.replace("%1", sensorId).replace("%2", serviceId));
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    /**
     * Tries to connect the current user account with his or her Google account
     */
    public static void googleConnect() {

        // get the session ID
        String sessionId = SessionManager.getSessionId();

        // set the current location as the callback
        UrlBuilder callbackBuilder = new UrlBuilder();
        callbackBuilder.setProtocol(Location.getProtocol());
        callbackBuilder.setHost(Location.getHost());
        callbackBuilder.setPath(Location.getPath());
        for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
            if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
                // do not include these parameters
            } else {
                callbackBuilder.setParameter(entry.getKey(), entry.getValue()
                        .toArray(new String[0]));
            }
        }
        String callbackUrl = callbackBuilder.buildString();

        // relocate to OpenID handler
        Location.replace(new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
                .setParameter("session_id", sessionId).buildString());
    }

    /**
     * Relocates the browser to the Google Open ID login page.
     */
    public static void googleLogin() {

        // set the current location as the callback
        UrlBuilder callbackBuilder = new UrlBuilder();
        callbackBuilder.setProtocol(Location.getProtocol());
        callbackBuilder.setHost(Location.getHost());
        callbackBuilder.setPath(Location.getPath());
        for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
            if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
                // do not include these parameters
            } else {
                callbackBuilder.setParameter(entry.getKey(), entry.getValue()
                        .toArray(new String[0]));
            }
        }
        String callbackUrl = callbackBuilder.buildString();

        // relocate to OpenID handler
        Location.replace(new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost("api.sense-os.nl")
                .setPath(Urls.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
                .buildString());
    }

    public static void joinGroup(RequestCallback callback, String groupId, String userId,
            List<String> sensorIds) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder()
                .setProtocol(Urls.PROTOCOL)
                .setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUP_USERS.replace("%1", groupId));
        String url = urlBuilder.buildString();

        // prepare data
        JSONObject userJson = new JSONObject();
        userJson.put("id", new JSONString(userId));
        JSONArray sensorsJson = new JSONArray();
        for (String sensorId : sensorIds) {
            sensorsJson.set(sensorsJson.size(), new JSONString(sensorId));
        }
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("user", userJson);
        bodyJson.put("sensors", sensorsJson);
        String data = bodyJson.toString();

        // send request
        sendRequest(method, url, sessionId, data, callback);
    }

    /**
     * @param callback
     *            RequestCallback to handle HTTP response
     * @param username
     *            Username
     * @param password
     *            Password (unhashed)
     */
    public static void login(RequestCallback callback, String username, String password) {
        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_LOGIN);
        String url = urlBuilder.buildString();

        // prepare request data
        String hashedPass = Md5Hasher.hash(password);
        String requestData = "username=" + username + "&password=" + hashedPass;

        // send request
        try {
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
            builder.setHeader("Accept", JSON_TYPE);
            builder.sendRequest(requestData, callback);
        } catch (Exception e) {
            LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
            callback.onError(null, e);
        }
    }

    /**
     * @param callback
     */
    public static void logout(RequestCallback callback) {
        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();

        Method method = RequestBuilder.GET;
        String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_LOGOUT).buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void removeGroupUser(RequestCallback callback, String id, String userId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.DELETE;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_GROUP_USERS.replace("%1", id) + "/" + userId);
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }

    public static void resetPassword(RequestCallback callback, String password, String token) {

        // prepare request details
        Method method = RequestBuilder.POST;
        String url = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_PW_RESET).buildString();

        // prepare request data
        String hashedPass = Md5Hasher.hash(password);
        String data = "password=" + hashedPass + "&token=" + token;

        try {
            RequestBuilder builder = new RequestBuilder(method, url);
            builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
            builder.setHeader("Accept", JSON_TYPE);
            builder.sendRequest(data, callback);
        } catch (Exception e) {
            LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
            callback.onError(null, e);
        }
    }

    private static void sendRequest(Method method, String url, String sessionId, String data,
            RequestCallback callback) {
        try {
            RequestBuilder builder = new RequestBuilder(method, url);
            if (null != sessionId) {
                builder.setHeader("X-SESSION_ID", sessionId);
            }
            builder.setHeader("Accept", JSON_TYPE);
            if (null != data && data.startsWith("{")) {
                builder.setHeader("Content-Type", JSON_TYPE);
            }

            builder.sendRequest(data, callback);
        } catch (Exception e) {
            LOG.warning(method + " " + url + " request threw exception: " + e.getMessage());
            callback.onError(null, e);
        }
    }

    public static void shareSensor(RequestCallback callback, String sensorId, String username) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.POST;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSOR_USERS.replace("%1", sensorId));
        String url = urlBuilder.buildString();
        String data = "{\"user\":{\"username\":\"" + username + "\"}}";

        // send request
        sendRequest(method, url, sessionId, data, callback);
    }

    public static void unshareSensor(RequestCallback callback, String sensorId, String userId) {

        // check if there is a session ID
        String sessionId = SessionManager.getSessionId();
        if (null == sessionId) {
            callback.onError(null, new Exception("Not logged in"));
            return;
        }

        // prepare request properties
        Method method = RequestBuilder.DELETE;
        UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                .setPath(Urls.PATH_SENSOR_USERS.replace("%1", sensorId) + "/" + userId);
        String url = urlBuilder.buildString();

        // send request
        sendRequest(method, url, sessionId, null, callback);
    }
}