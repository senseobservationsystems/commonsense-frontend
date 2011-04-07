package nl.sense_os.commonsense.client.ajax.parsers;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupParser {

    private static final String TAG = "GroupParser";

    public static GroupModel parseGroup(String jsonString) {

        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();
            JSONObject group = json.get("group").isObject();

            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties
                    .put(GroupModel.KEY_ID, group.get(GroupModel.KEY_ID).isString().stringValue());
            properties.put(GroupModel.KEY_EMAIL, group.get(GroupModel.KEY_EMAIL).isString()
                    .stringValue());
            properties.put(GroupModel.KEY_USERNAME, group.get(GroupModel.KEY_USERNAME).isString()
                    .stringValue());
            properties.put(GroupModel.KEY_NAME, group.get(GroupModel.KEY_NAME).isString()
                    .stringValue());
            properties.put(GroupModel.KEY_UUID, group.get(GroupModel.KEY_UUID).isString()
                    .stringValue());

            // font end-only properties
            properties.put("tagType", TagModel.TYPE_GROUP);
            properties.put("text", properties.get(GroupModel.KEY_NAME));

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
}
