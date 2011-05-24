package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.UserModel;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class UserParser {

    private static final Logger logger = Logger.getLogger("UserParser");

    public static UserModel parseUser(JSONObject user) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserModel.ID, user.get(UserModel.ID).isString().stringValue());
        if (null != user.get(UserModel.EMAIL)) {
            properties.put(UserModel.EMAIL, user.get(UserModel.EMAIL).isString().stringValue());
        }
        if (null != user.get(UserModel.NAME)) {
            properties.put(UserModel.NAME, user.get(UserModel.NAME).isString().stringValue());
        }
        if (null != user.get(UserModel.SURNAME)) {
            properties.put(UserModel.SURNAME, user.get(UserModel.SURNAME).isString().stringValue());
        }
        if (null != user.get(UserModel.USERNAME)) {
            properties.put(UserModel.USERNAME, user.get(UserModel.USERNAME).isString()
                    .stringValue());
        }
        if (null != user.get(UserModel.MOBILE)) {
            properties.put(UserModel.MOBILE, user.get(UserModel.MOBILE).isString().stringValue());
        }

        // front end-only properties
        String text = (String) properties.get(UserModel.NAME);
        if (text != null && text.length() > 0) {
            String surname = (String) properties.get(UserModel.SURNAME);
            if (surname != null && surname.length() > 0) {
                text += " " + surname;
            }
        } else {
            text = (String) properties.get(UserModel.USERNAME);
        }
        if (text == null || text.length() == 0) {
            // when all else fails:
            text = "User #" + properties.get("id");
        }
        properties.put("text", text);

        return new UserModel(properties);
    }

    public static List<UserModel> parseGroupUsers(String jsonString) {

        List<UserModel> result = new ArrayList<UserModel>();

        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();
            JSONArray users = json.get("users").isArray();
            for (int i = 0; i < users.size(); i++) {
                JSONObject user = users.get(i).isObject();

                result.add(parseUser(user));
            }

        } catch (Exception e) {
            logger.severe("GET GROUP DETAILS JSONException: " + e.getMessage());
            logger.severe("Raw JSON: " + jsonString);
        }

        // return list of tags
        return result;
    }
}
