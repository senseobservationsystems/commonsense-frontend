package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.Group;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

public class GetGroupDetailsResponse extends SenseApiResponse {

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
