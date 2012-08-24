package nl.sense_os.commonsense.main.client.gxt.model;

import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.common.client.model.Service;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a service. GXT-style bean, used in various GXT components.
 */
public class GxtService extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String DATA_FIELDS = "data_fields";
    public static final String NAME = "name";
    public static final String ID = "id";

    public GxtService() {
        super();
    }

    public GxtService(Map<String, Object> properties) {
        super(properties);
    }

    public GxtService(TreeModel parent) {
        super(parent);
    }

    public GxtService(Service jso) {
        this();
        setId(jso.getId());
        setName(jso.getName());
        setDataFields(jso.getDataFields());
    }

    public GxtService setDataFields(List<String> dataFields) {
        set(DATA_FIELDS, dataFields);
        return this;
    }

    public GxtService setName(String name) {
        set(NAME, name);
        return this;
    }

    public GxtService setId(int id) {
        set(ID, id);
        return this;
    }

    public List<String> getDataFields() {
        return get(DATA_FIELDS);
    }

    public int getId() {
        Object property = get(ID);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            return -1;
        }
    }

    public String getName() {
        return get(NAME);
    }

    @Override
    public String toString() {
        return getName();
    }
}
