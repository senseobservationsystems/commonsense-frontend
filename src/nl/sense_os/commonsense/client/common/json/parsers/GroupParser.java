package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class GroupParser {

    private static final Logger logger = Logger.getLogger("GroupParser");

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
            properties.put("text", properties.get(GroupModel.NAME));

            return new GroupModel(properties);

        } catch (Exception e) {
            logger.severe("Exception parsing group details: " + e.getMessage());
            logger.severe("Raw JSON: " + jsonString);
            return null;
        }
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
                properties.put("text", properties.get(GroupModel.NAME));

                GroupModel model = new GroupModel(properties);

                list.add(model);
            }

        } catch (Exception e) {
            logger.severe("GET GROUPS Exception: " + e.getMessage());
            logger.severe("Raw response: " + jsonString);
        }

        return list;
    }
}
