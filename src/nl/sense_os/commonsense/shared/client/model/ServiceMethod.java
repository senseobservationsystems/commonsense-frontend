package nl.sense_os.commonsense.shared.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ServiceMethod extends JavaScriptObject {

    protected ServiceMethod() {
        // protected empty constructor
    }

    public native final String getName() /*-{
        if (undefined != this.name) {
            return this.name;
        } else {
            return '';
        }
    }-*/;

    public native final String getReturnValue() /*-{
        if (undefined != this['return value']) {
            return this['return value'];
        } else {
            return '';
        }
    }-*/;

    public native final JsArrayString getRawParameters() /*-{
        if (undefined != this.parameters) {
            return this.parameters;
        } else {
            return [];
        }
    }-*/;

    public final List<String> getParameters() {
        JsArrayString rawParams = getRawParameters();

        List<String> list = new ArrayList<String>(rawParams.length());
        for (int i = 0; i < rawParams.length(); i++) {
            list.add(rawParams.get(i));
        }

        return list;
    }
}
