package nl.sense_os.commonsense.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.services.GroupsProxy;
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

public class GroupsProxyImpl extends RemoteServiceServlet implements GroupsProxy {

    private static final Logger log = Logger.getLogger("GroupsProxyImpl");
    private static final long serialVersionUID = 1L;

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

    private ModelData parseGroupDetails(String response) throws WrongResponseException {
        try {
            JSONObject group = (JSONObject) new JSONObject(response).get("group");

            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("id", group.getString("id"));
            properties.put("email", group.getString("email"));
            properties.put("username", group.getString("username"));
            properties.put("name", group.getString("name"));
            properties.put("UUID", group.getString("UUID"));

            // font end-only properties
            properties.put("tagType", TagModel.TYPE_GROUP);
            properties.put("text", properties.get("name"));

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
    private List<ModelData> parseGroups(String response) throws WrongResponseException {
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

    private List<ModelData> parseGroupUsers(String response) throws WrongResponseException {

        try {
            List<ModelData> result = new ArrayList<ModelData>();
            JSONArray users = (JSONArray) new JSONObject(response).get("users");
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("id", user.getString("id"));
                properties.put("email", user.optString("email"));
                properties.put("name", user.optString("name"));
                properties.put("surname", user.optString("surname"));
                properties.put("username", user.optString("username"));
                properties.put("mobile", user.optString("mobile"));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_USER);
                String text = user.optString("name", "") + " " + user.optString("surname", "");
                if (text.length() < 3) {
                    text = "User #" + properties.get("id");
                }
                properties.put("text", text);

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

    private ModelData requestGroupDetails(String sessionId, String groupId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_GROUPS + "/" + groupId;
        String response = Requester.request(url, sessionId, "GET", null);

        return parseGroupDetails(response);
    }

    private List<ModelData> requestGroups(String sessionId) throws DbConnectionException,
            WrongResponseException {

        String url = Constants.URL_GROUPS;
        String response = Requester.request(url, sessionId, "GET", null);
        return parseGroups(response);
    }

    private List<ModelData> requestGroupUsers(String sessionId, String groupId)
            throws DbConnectionException, WrongResponseException {

        String url = Constants.URL_GROUPS + "/" + groupId + "/users";
        String response = Requester.request(url, sessionId, "GET", null);
        return parseGroupUsers(response);
    }
}
