package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.UserJso;
import nl.sense_os.commonsense.common.client.model.UserModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetGroupUsersResponseJso extends JavaScriptObject {

    protected GetGroupUsersResponseJso() {
        // empty protected constructor
    }

    public final native JsArray<UserJso> getRawUsers() /*-{
		return this.users;
    }-*/;

    public final List<UserModel> getUsers() {
        List<UserModel> list = new ArrayList<UserModel>();

        JsArray<UserJso> users = getRawUsers();

        if (null != users) {
            for (int i = 0; i < users.length(); i++) {
                list.add(new UserModel(users.get(i)));
            }
        }

        return list;
    }

}
