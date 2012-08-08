package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.Group;

import com.google.gwt.core.client.JavaScriptObject;

public class GetGroupDetailsResponse extends JavaScriptObject {

	protected GetGroupDetailsResponse() {
		// empty protected constructor
	}

	public final native Group getGroup() /*-{
		if (undefined != this.group) {
			return this.group;
		} else {
			return {};
		}
	}-*/;
}
