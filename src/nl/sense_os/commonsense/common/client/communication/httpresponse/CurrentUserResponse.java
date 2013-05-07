package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.User;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

/**
 * Overlay for response to request for current user from CommonSense.
 */
public class CurrentUserResponse extends SenseApiResponse {

	protected CurrentUserResponse() {
		// empty protected constructor
	}

	public final native User getUser() /*-{
		return this.user;
	}-*/;
}
