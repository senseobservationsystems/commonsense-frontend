package nl.sense_os.commonsense.client.states.list;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.models.ServiceMethodJso;
import nl.sense_os.commonsense.client.common.models.ServiceMethodModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetMethodsResponseJso extends JavaScriptObject {

    protected GetMethodsResponseJso() {
        // protected empty constructor
    }

    public native final JsArray<ServiceMethodJso> getRawMethods() /*-{
        if (undefined != this.methods) {
            return this.methods;
        } else {
            return [];
        }
    }-*/;

    public final List<ServiceMethodModel> getMethods() {

        JsArray<ServiceMethodJso> rawMethods = getRawMethods();

        List<ServiceMethodModel> list = new ArrayList<ServiceMethodModel>();
        for (int i = 0; i < rawMethods.length(); i++) {
            list.add(new ServiceMethodModel(rawMethods.get(i)));
        }

        return list;
    }
}
