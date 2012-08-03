package nl.sense_os.commonsense.common.client.model;

import java.util.Map;
import java.util.logging.Logger;


import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a device. GXT-style bean, used in various GXT components.
 */
public class DeviceModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String UUID = "uuid";
    private static final Logger LOGGER = Logger.getLogger(DeviceModel.class.getName());

    public DeviceModel() {
        super();
    }

    public DeviceModel(DeviceJso jso) {
        this();
        setId(jso.getId());
        setType(jso.getType());
        setUuid(jso.getUuid());
    }

    public DeviceModel(Map<String, Object> properties) {
        super(properties);
    }

    public DeviceModel(TreeModel parent) {
        super(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceModel) {
            return getId() == ((DeviceModel) obj).getId();
        } else {
            return super.equals(obj);
        }
    }

    public int getId() {
        Object property = get(ID);
        if (property instanceof Integer) {
            return ((Integer) property).intValue();
        } else if (property instanceof String) {
            return Integer.parseInt((String) property);
        } else {
            LOGGER.severe("Missing property: " + ID);
            return -1;
        }
    }

    public String getType() {
        return get(TYPE);
    }

    public String getUuid() {
        return get(UUID);
    }

    public DeviceModel setId(int id) {
        set(ID, id);
        return this;
    }

    public DeviceModel setType(String type) {
        set(TYPE, type);
        return this;
    }

    public DeviceModel setUuid(String uuid) {
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
