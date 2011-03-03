package nl.sense_os.commonsense.server.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class GroupConverter {

    private static final Logger log = Logger.getLogger("GroupConverter");

    public static GroupModel parseGroup(String response) throws WrongResponseException {

        try {
            JSONObject group = new JSONObject(response);
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(GroupModel.KEY_ID, group.getString(GroupModel.KEY_ID));
            properties.put(GroupModel.KEY_EMAIL, group.getString(GroupModel.KEY_EMAIL));
            properties.put(GroupModel.KEY_USERNAME, group.getString(GroupModel.KEY_USERNAME));
            properties.put(GroupModel.KEY_NAME, group.getString(GroupModel.KEY_NAME));
            properties.put(GroupModel.KEY_UUID, group.getString(GroupModel.KEY_UUID));

            // font end-only properties
            properties.put("tagType", TagModel.TYPE_GROUP);
            properties.put("text", properties.get(GroupModel.KEY_NAME));

            return new GroupModel(properties);
        } catch (JSONException e) {
            log.severe("JSONException parsing group details: " + e.getMessage());
            log.severe("Raw response: " + response);
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    public static List<ModelData> parseGroupIds(String response) throws WrongResponseException {
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
}
