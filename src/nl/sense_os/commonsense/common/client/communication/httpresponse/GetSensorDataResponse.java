package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.BackEndDataPoint;
import nl.sense_os.commonsense.lib.client.model.httpresponse.SenseApiResponse;

import com.google.gwt.core.client.JsArray;

public class GetSensorDataResponse extends SenseApiResponse {

	/**
	 * Creates a GetSensorDataResponseJso from the JSON response from CommonSense. Also converts any
	 * "embedded" JSON-disguised-as-String objects, so that e.g.
	 * <code>{"foo":"{\"bar\":\"baz\"}"}</code> will get completely converted.
	 * 
	 * @param source
	 *            Raw response from CommonSense.
	 * @return JavaScriptObject representing the response.
	 */
	public static final GetSensorDataResponse createFixedJson(String source) {
		String fixed = fixJson(source);
		return create(fixed).cast();
	}

	private native static final String fixJson(String source) /*-{
		function stripslashes(str) {
			return (str + '').replace(/\\(.?)/g, function(s, n1) {
				switch (n1) {
				case '\\':
					return '\\';
				case '0':
					return '\u0000';
				case '':
					return '';
				default:
					return n1;
				}
			});
		}
		var stripped = stripslashes(source);
		var jsonFixed = stripped.replace(/:\"{/g, ':{').replace(/}\"/g, '}');
		return jsonFixed;
	}-*/;

	protected GetSensorDataResponse() {
		// empty protected constructor
	}

	/**
	 * @return The 'data' property of the response, containing an array with sensor data. If 'data'
	 *         is undefined, an empty array is returned.
	 */
	public native final JsArray<BackEndDataPoint> getData() /*-{
		if (undefined != this.data) {
			return this.data;
		} else {
			return [];
		}
	}-*/;

	/**
	 * @return The 'total' property of the response. If 'total' is undefined, -1 is returned.
	 */
	public native final int getTotal() /*-{
		if (undefined != this.total) {
			return this.total;
		} else {
			return -1;
		}
	}-*/;
}
