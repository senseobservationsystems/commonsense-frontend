package nl.sense_os.commonsense.main.client.gxt.model;

import java.util.Map;

import nl.sense_os.commonsense.shared.client.model.Device;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a device. GXT-style bean, used in various GXT components.
 */
public class GxtDevice extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String UUID = "uuid";

    public GxtDevice() {
        super();
    }

    public GxtDevice(Device jso) {
        this();
        setId(jso.getId());
        setType(jso.getType());
        setUuid(jso.getUuid());
    }

    public GxtDevice(Map<String, Object> properties) {
        super(properties);
    }

    public GxtDevice(TreeModel parent) {
        super(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GxtDevice) {
            return getId().equals(((GxtDevice) obj).getId());
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

    public GxtDevice setId(String id) {
        set(ID, id);
        return this;
    }

    public GxtDevice setType(String type) {
        set(TYPE, type);
        return this;
    }

    public GxtDevice setUuid(String uuid) {
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
