package nl.sense_os.commonsense.shared.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.shared.client.model.Group;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetGroupsResponse extends JavaScriptObject {

	protected GetGroupsResponse() {
		// empty protected constructor
	}

	public final native JsArray<Group> getRawGroups() /*-{
		if (undefined != this.groups) {
			return this.groups;
		} else {
			return [];
		}
	}-*/;

	public final List<Group> getGroups() {
		List<Group> list = new ArrayList<Group>();

		JsArray<Group> groups = getRawGroups();
		for (int i = 0; i < groups.length(); i++) {
			list.add(groups.get(i));
		}

		return list;
	}
}
