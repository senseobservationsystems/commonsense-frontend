package nl.sense_os.commonsense.main.client.ext.model;

import java.util.Map;

import nl.sense_os.commonsense.common.client.model.Device;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a device. GXT-style bean, used in various GXT components.
 */
public class ExtDevice extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String UUID = "uuid";

    public ExtDevice() {
        super();
    }

    public ExtDevice(Device jso) {
        this();
        setId(jso.getId());
        setType(jso.getType());
        setUuid(jso.getUuid());
    }

    public ExtDevice(Map<String, Object> properties) {
        super(properties);
    }

    public ExtDevice(TreeModel parent) {
        super(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExtDevice) {
            return getId().equals(((ExtDevice) obj).getId());
        } else {
            return super.equals(obj);
        }
    }

    public String getId() {
        return get(ID);
    }

    public String getType() {
        return get(TYPE);
    }

    public String getUuid() {
        return get(UUID);
    }

    public ExtDevice setId(String id) {
        set(ID, id);
        return this;
    }

    public ExtDevice setType(String type) {
        set(TYPE, type);
        return this;
    }

    public ExtDevice setUuid(String uuid) {
        set(UUID, uuid);
        return this;
    }

    @Override
    public String toString() {
        if ("myrianode".equals(getType())) {
            // append the UUID to the String in case the device is a myrianode
            return getType() + " " + getUuid();
        } else {
            return getType();
        }
    }
}
