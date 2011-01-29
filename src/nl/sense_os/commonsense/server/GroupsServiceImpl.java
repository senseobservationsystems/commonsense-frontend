package nl.sense_os.commonsense.server;

import nl.sense_os.commonsense.client.services.GroupsService;
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

public class GroupsServiceImpl extends RemoteServiceServlet implements GroupsService {

    private static final Logger log = Logger.getLogger("GroupsServiceImpl");
    private static final long serialVersionUID = 1L;
    private int responseCode = 0;
    private String responseContent;

    @Override
    public String createGroup(String sessionId, String name, String email, String username,
            String password) throws InternalError, WrongResponseException, DbConnectionException {
        String data = null;
        try {
            JSONObject json = new JSONObject();
            JSONObject group = new JSONObject();
            group.put("name", name);
            group.put("email", email);
            if (null != username) {
                group.put("username", username);
                group.put("password", password);
            }
            json.put("group", group);
            data = json.toString();
        } catch (JSONException e) {
            log.severe("JSONException creating POST data");
            throw new InternalError("JSONException creating POST data.");
        }

        String url = Constants.URL_GROUPS + ".json";
        doRequest(url, sessionId, "POST", data);

        if (this.responseCode != HttpURLConnection.HTTP_CREATED) {
            log.severe("POST GROUPS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to create groups " + this.responseCode);
        }

        return this.responseContent;
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
    public List<TreeModel> getGroups(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // get list of groups
        List<ModelData> groupsIds = requestGroups(sessionId);

        List<TreeModel> groups = new ArrayList<TreeModel>();
        for (ModelData model : groupsIds) {
            String groupId = model.get("group_id");
            ModelData details = requestGroupDetails(sessionId, groupId);
            List<ModelData> users = requestGroupUsers(sessionId, groupId);

            TreeModel group = new BaseTreeModel(details.getProperties());
            for (ModelData userModel : users) {
                group.add(new BaseTreeModel(userModel.getProperties()));
            }
            groups.add(group);
        }
        return groups;
    }

    private ModelData handleGroupDetailsResponse(String response) throws WrongResponseException {
        try {
            JSONObject group = (JSONObject) new JSONObject(response).get("group");

            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("id", group.getString("id"));
            properties.put("email", group.getString("email"));
            properties.put("username", group.getString("username"));
            properties.put("name", group.getString("name"));
            properties.put("UUID", group.getString("UUID"));
            properties.put("tagType", TagModel.TYPE_GROUP);

            return new BaseModelData(properties);

        } catch (JSONException e) {
            log.severe("GET GROUP DETAILS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    /**
     * @param response
     *            response content from GET /groups request
     * @return list of groups, with group id and user id
     * @throws DbConnectionException
     * @throws WrongResponseException
     */
    private List<ModelData> handleGroupsResponse(String response) throws WrongResponseException {
        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray groups = (JSONArray) new JSONObject(response).get("groups");
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("user_id", group.getString("user_id"));
                properties.put("group_id", group.getString("group_id"));

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET GROUPS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    private List<ModelData> handleGroupUsersResponse(String response) throws WrongResponseException {

        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray users = (JSONArray) new JSONObject(response).get("users");
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", user.getString("id"));
                properties.put("email", user.getString("email"));
                properties.put("name", user.getString("name"));
                properties.put("surname", user.getString("surname"));
                properties.put("mobile", user.getString("mobile"));
                properties.put("tagType", TagModel.TYPE_USER);

                ModelData model = new BaseModelData(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET GROUP DETAILS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    @Override
    public String inviteUser(String sessionId, String groupId, String email) throws InternalError,
            WrongResponseException, DbConnectionException {
        
        String url = Constants.URL_GROUPS + "/" + groupId + "/users.json";
        String data = null;
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", email);
            json.put("user", user);
            data = json.toString();
        } catch (JSONException e) {
            log.severe("JSONException creating POST data");
            throw new InternalError("JSONException creating POST data.");
        }
        doRequest(url, sessionId, "POST", data);
        
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("failed to invite user: "+ this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to invite user: " + responseCode);
        }
        return this.responseContent;
    }

    @Override
    public String leaveGroup(String sessionId, String groupId) throws WrongResponseException,
            DbConnectionException {

        String url = Constants.URL_GROUPS + "/" + groupId;
        
        doRequest(url, sessionId, "DELETE", null);
        
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("LEAVE GROUPS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to leave groups " + this.responseCode);
        }

        return this.responseContent;
    }

    private ModelData requestGroupDetails(String sessionId, String groupId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_GROUPS + "/" + groupId;
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET GROUP DETAILS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get group details " + this.responseCode);
        }

        return handleGroupDetailsResponse(this.responseContent);
    }

    private List<ModelData> requestGroups(String sessionId) throws DbConnectionException,
            WrongResponseException {

        String url = Constants.URL_GROUPS;
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET GROUPS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get groups " + this.responseCode);
        }

        return handleGroupsResponse(this.responseContent);
    }

    private List<ModelData> requestGroupUsers(String sessionId, String groupId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_GROUPS + "/" + groupId + "/users";
        doRequest(url, sessionId, "GET", null);
        if (this.responseCode != HttpURLConnection.HTTP_OK) {
            log.severe("GET GROUP USERS failure: " + this.responseCode + " " + this.responseContent);
            throw new WrongResponseException("failed to get group users " + this.responseCode);
        }

        return handleGroupUsersResponse(this.responseContent);
    }
}
