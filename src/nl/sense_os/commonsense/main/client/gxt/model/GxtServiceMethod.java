package nl.sense_os.commonsense.main.client.gxt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.commonsense.shared.client.model.ServiceMethod;

import com.extjs.gxt.ui.client.data.BaseModel;

public class GxtServiceMethod extends BaseModel {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "name";
    public static final String RETURN_VALUE = "return value";
    public static final String PARAMETERS = "parameters";

    public GxtServiceMethod() {
        super();
    }

    public GxtServiceMethod(HashMap<String, Object> properties) {
        super(properties);
    }

    public GxtServiceMethod(ServiceMethod jso) {
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

    public GxtServiceMethod setName(String name) {
        set(NAME, name);
        return this;
    }

    public GxtServiceMethod setParameters(List<String> parameters) {
        set(PARAMETERS, parameters);
        return this;
    }

    public GxtServiceMethod setReturnValue(String returnValue) {
        set(RETURN_VALUE, returnValue);
        return this;
    }
}
