package nl.sense_os.commonsense.client.common.json.overlays;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class BackEndSensorData extends JavaScriptObject {

    protected BackEndSensorData() {
        // empty protected constructor
    }

    /**
     * Creates a BackEndSensorData from the JSON response from CommonSense. Also converts any
     * "embedded" JSON-disguised-as-String objects, so that e.g.
     * <code>{"foo":"{\"bar\":\"baz\"}"}</code> will get completely converted.
     * 
     * @param source
     *            Raw response from CommonSense.
     * @return JavaScriptObject representing the response.
     */
    public final static native BackEndSensorData create(String source) /*-{
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
        return eval('(' + jsonFixed + ')');
    }-*/;

    public final native JsArray<BackEndDataPoint> getData() /*-{
        return this.data;
    }-*/;

    public final native int getTotal() /*-{
        return this.total;
    }-*/;
}
