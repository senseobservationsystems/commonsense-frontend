package nl.sense_os.commonsense.common.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class Service extends JavaScriptObject {

    protected Service() {
        // empty protected constructor
    }

    public final native int getId() /*-{
		if (undefined != this.id) {
			return parseInt(this.id);
		} else {
			return -1;
		}
    }-*/;

    public final native String getName() /*-{
		return this.name;
    }-*/;

    public final native JsArrayString getRawDataFields() /*-{
		return this.data_fields;
    }-*/;

    public final List<String> getDataFields() {
        List<String> list = new ArrayList<String>();

        JsArrayString dataFields = getRawDataFields();
        if (null != dataFields) {
            for (int i = 0; i < dataFields.length(); i++) {
                list.add(dataFields.get(i));
            }
        }

        return list;
    }
}
