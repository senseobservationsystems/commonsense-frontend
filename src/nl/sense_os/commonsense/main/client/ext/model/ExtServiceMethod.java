package nl.sense_os.commonsense.main.client.ext.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.common.client.model.ServiceMethod;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ExtServiceMethod extends BaseModel {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "name";
    public static final String RETURN_VALUE = "return value";
    public static final String PARAMETERS = "parameters";

    public ExtServiceMethod() {
        super();
    }

    public ExtServiceMethod(HashMap<String, Object> properties) {
        super(properties);
    }

    public ExtServiceMethod(ServiceMethod jso) {
        this();
        setName(jso.getName());
        setReturnValue(jso.getReturnValue());
        setParameters(jso.getParameters());
    }

    public String getName() {
        return get(NAME);
    }

    public List<String> getParameters() {
        return get(PARAMETERS, new ArrayList<String>());
    }

    public String getReturnValue() {
        return get(RETURN_VALUE);
    }

    public ExtServiceMethod setName(String name) {
        set(NAME, name);
        return this;
    }

    public ExtServiceMethod setParameters(List<String> parameters) {
        set(PARAMETERS, parameters);
        return this;
    }

    public ExtServiceMethod setReturnValue(String returnValue) {
        set(RETURN_VALUE, returnValue);
        return this;
    }
}
