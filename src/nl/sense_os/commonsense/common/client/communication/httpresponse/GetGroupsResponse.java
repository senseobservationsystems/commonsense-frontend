package nl.sense_os.commonsense.common.client.communication.httpresponse;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.Group;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

public class GetGroupsResponse extends SenseApiResponse {

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
