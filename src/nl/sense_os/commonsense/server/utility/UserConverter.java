package nl.sense_os.commonsense.server.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.UserModel;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

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

                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(UserModel.ID, user.getString(UserModel.ID));
                properties.put(UserModel.EMAIL, user.optString(UserModel.EMAIL));
                properties.put(UserModel.NAME, user.optString(UserModel.NAME));
                properties.put(UserModel.SURNAME, user.optString(UserModel.SURNAME));
                properties.put(UserModel.USERNAME, user.optString(UserModel.USERNAME));
                properties.put(UserModel.MOBILE, user.optString(UserModel.MOBILE));

                // front end-only properties
                properties.put("tagType", TagModel.TYPE_USER);
                String text = user.optString("name", "") + " " + user.optString("surname", "");
                if (text.length() < 3) {
                    text = "User #" + properties.get("id");
                }
                properties.put("text", text);

                UserModel model = new UserModel(properties);

                result.add(model);
            }

            // return list of tags
            return result;

        } catch (JSONException e) {
            log.severe("GET GROUP DETAILS JSONException: " + e.getMessage());
            throw (new WrongResponseException(e.getMessage()));
        }
    }
}
