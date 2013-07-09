package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

public class ServiceMethodResponse extends SenseApiResponse {

    protected ServiceMethodResponse() {
        // empty protected constructor
    }

    public final native String getResult() /*-{
		if (undefined != this.result) {
			return this.result;
		} else {
			return null;
		}
	}-*/;
}
