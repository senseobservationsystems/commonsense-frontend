package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.models.GroupModel;
import nl.sense_os.commonsense.shared.models.TagModel;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class GroupParser {

    private static final String TAG = "GroupParser";

    public static GroupModel parseGroup(String jsonString) {

        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();
            JSONObject group = json.get("group").isObject();

            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(GroupModel.ID, group.get(GroupModel.ID).isString().stringValue());
            properties.put(GroupModel.EMAIL, group.get(GroupModel.EMAIL).isString().stringValue());
            properties.put(GroupModel.NAME, group.get(GroupModel.NAME).isString().stringValue());
            properties.put(GroupModel.USERNAME, group.get(GroupModel.USERNAME).isString()
                    .stringValue());

            // front end-only properties
            properties.put("tagType", TagModel.TYPE_GROUP);
            properties.put("text", properties.get(GroupModel.NAME));

            return new GroupModel(properties);

        } catch (Exception e) {
            Log.e(TAG, "Exception parsing group details: " + e.getMessage());
            Log.e(TAG, "Raw JSON: " + jsonString);
            return null;
        }
    }

    public static List<ModelData> parseGroupIds(String jsonString) {

        List<ModelData> list = new ArrayList<ModelData>();

        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();

            // get array of raw sensors
            JSONArray groups = json.get("groups").isArray();

            for (int i = 0; i < groups.size(); i++) {
                JSONObject group = groups.get(i).isObject();

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put("user_id", group.get("user_id").isString().stringValue());
                properties.put("group_id", group.get("group_id").isString().stringValue());

                ModelData model = new BaseModelData(properties);

                list.add(model);
            }

        } catch (Exception e) {
            Log.e(TAG, "GET GROUPS Exception: " + e.getMessage());
            Log.e(TAG, "Raw response: " + jsonString);
        }

        return list;
    }

    public static List<GroupModel> parseGroups(String jsonString) {

        List<GroupModel> list = new ArrayList<GroupModel>();

        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();

            // get array of raw sensors
            JSONArray groups = json.get("groups").isArray();

            for (int i = 0; i < groups.size(); i++) {
                JSONObject group = groups.get(i).isObject();

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(GroupModel.ID, group.get(GroupModel.ID).isString().stringValue());
                properties
                        .put(GroupModel.NAME, group.get(GroupModel.NAME).isString().stringValue());
                properties.put(GroupModel.USERNAME, group.get(GroupModel.USERNAME).isString()
                        .stringValue());
                properties.put(GroupModel.EMAIL, group.get(GroupModel.EMAIL).isString()
                        .stringValue());

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_GROUP);
                properties.put("text", properties.get(GroupModel.NAME));

                GroupModel model = new GroupModel(properties);

                list.add(model);
            }

        } catch (Exception e) {
            Log.e(TAG, "GET GROUPS Exception: " + e.getMessage());
            Log.e(TAG, "Raw response: " + jsonString);
        }

        return list;
    }
}
