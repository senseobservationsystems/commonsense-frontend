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

import nl.sense_os.commonsense.client.services.GroupsService;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

public class GroupsServiceImpl extends RemoteServiceServlet implements GroupsService {

    private static final Logger log = Logger.getLogger("GroupsServiceImpl");
    private static final long serialVersionUID = 1L;

    @Override
    public List<TreeModel> getGroups(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // get list of groups
        List<ModelData> groupsIds = requestGroups(sessionId);

        List<TreeModel> groups = new ArrayList<TreeModel>(); 
        for (ModelData group : groupsIds) {
            String groupId = group.get("group_id");
            ModelData model = requestGroupDetails(sessionId, groupId);
            
            groups.add(new BaseTreeModel(model.getProperties()));
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

    private ModelData requestGroupDetails(String sessionId, String groupId)
            throws DbConnectionException, WrongResponseException {

        // Get response from server
        String response = "";
        try {
            URL url = new URL(Constants.URL_GROUPS + "/" + groupId);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");

            // perform GET method at URL
            final int statusCode = connection.getResponseCode();
            final String message = connection.getResponseMessage();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // log.info("GET GROUP DETAILS " + statusCode + " " + message);

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } else {
                log.severe("GET GROUP DETAILS failure: " + statusCode + " " + message);
                throw new WrongResponseException("failed to get groups " + statusCode);
            }
        } catch (MalformedURLException e) {
            log.severe("GET GROUP DETAILS MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("GET GROUP DETAILS IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }

        return handleGroupDetailsResponse(response);
    }

    private List<ModelData> requestGroups(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // Get response from server
        String response = "";
        try {
            URL url = new URL(Constants.URL_GROUPS);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");

            // perform GET method at URL
            final int statusCode = connection.getResponseCode();
            final String message = connection.getResponseMessage();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // log.info("GET GROUPS " + statusCode + " " + message);

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } else {
                log.severe("GET GROUPS failure: " + statusCode + " " + message);
                throw new WrongResponseException("failed to get groups " + statusCode);
            }
        } catch (MalformedURLException e) {
            log.severe("GET GROUPS MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("GET GROUPS IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }

        return handleGroupsResponse(response);

    }
}
