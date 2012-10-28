package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.User;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetGroupUsersResponse extends JavaScriptObject {

	protected GetGroupUsersResponse() {
		// empty protected constructor
	}

	public final native JsArray<User> getRawUsers() /*-{
		if (undefined != this.users) {
			return this.users;
		} else {
			return [];
		}
	}-*/;

	public final List<User> getUsers() {
		List<User> list = new ArrayList<User>();

		JsArray<User> users = getRawUsers();
		for (int i = 0; i < users.length(); i++) {
			list.add(users.get(i));
		}

		return list;
	}
}
