package nl.sense_os.commonsense.client.common.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ServiceMethodModel extends BaseModel {

    private static final long serialVersionUID = 1L;
    public static final String NAME = "name";
    public static final String RETURN_VALUE = "return value";
    public static final String PARAMETERS = "parameters";

    public ServiceMethodModel() {
        super();
    }

    public ServiceMethodModel(HashMap<String, Object> properties) {
        super(properties);
    }

    public ServiceMethodModel(ServiceMethodJso jso) {
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

    public ServiceMethodModel setName(String name) {
        set(NAME, name);
        return this;
    }

    public ServiceMethodModel setParameters(List<String> parameters) {
        set(PARAMETERS, parameters);
        return this;
    }

    public ServiceMethodModel setReturnValue(String returnValue) {
        set(RETURN_VALUE, returnValue);
        return this;
    }
}
