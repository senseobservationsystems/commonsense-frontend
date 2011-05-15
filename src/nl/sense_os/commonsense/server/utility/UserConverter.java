package nl.sense_os.commonsense.server.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;
import nl.sense_os.commonsense.shared.models.TagModel;
import nl.sense_os.commonsense.shared.models.UserModel;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class UserConverter {

    private static final Logger log = Logger.getLogger("UserConverter");

    public static List<UserModel> parseGroupUsers(String response) throws WrongResponseException {

        try {
            List<UserModel> result = new ArrayList<UserModel>();
            JSONArray users = (JSONArray) new JSONObject(response).get("users");
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);

                result.add(parseUser(user));
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET GROUP DETAILS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }

    public static UserModel parseUser(JSONObject user) throws JSONException {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(UserModel.ID, user.getString(UserModel.ID));
        properties.put(UserModel.EMAIL, user.optString(UserModel.EMAIL));
        properties.put(UserModel.NAME, user.optString(UserModel.NAME));
        properties.put(UserModel.SURNAME, user.optString(UserModel.SURNAME));
        properties.put(UserModel.USERNAME, user.optString(UserModel.USERNAME));
        properties.put(UserModel.MOBILE, user.optString(UserModel.MOBILE));

        // front end-only properties
        properties.put("tagType", TagModel.TYPE_USER);
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
}
