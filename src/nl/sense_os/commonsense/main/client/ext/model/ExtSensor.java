package nl.sense_os.commonsense.main.client.ext.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Device;
import nl.sense_os.commonsense.common.client.model.Environment;
import nl.sense_os.commonsense.common.client.model.Sensor;
import nl.sense_os.commonsense.common.client.model.User;
import nl.sense_os.commonsense.common.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.core.client.JsArray;

/**
 * Model for a sensor. GXT-style bean, used in various GXT components.
 */
public class ExtSensor extends BaseTreeModel {

	public static final String DATA_STRUCTURE = "data_structure";
	public static final String DATA_TYPE = "data_type";
	public static final String DESCRIPTION = "device_type";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DISPLAY_NAME = "display_name";
	public static final String TYPE = "type";
	public static final String PAGER_TYPE = "pager_type";

	public static final String ALIAS = "alias";
	public static final String USERS = "users";
	public static final String AVAIL_SERVICES = "available_services";

	public static final String OWNER = "owner";
	public static final String OWNER_ID = "owner_id";
	public static final String OWNER_MOBILE = "owner_mobile";
	public static final String OWNER_NAME = "owner_name";
	public static final String OWNER_SURNAME = "owner_surname";
	public static final String OWNER_USERNAME = "owner_username";
	public static final String OWNER_EMAIL = "owner_email";

	public static final String DEVICE = "device";
	public static final String DEVICE_ID = "device_id";
	public static final String DEVICE_TYPE = "nested_device_type";
	public static final String DEVICE_UUID = "device_uuid";

	public static final String ENVIRONMENT = "environment";
	public static final String ENVIRONMENT_ID = "environment_id";
	public static final String ENVIRONMENT_NAME = "environment_name";
	public static final String ENVIRONMENT_FLOORS = "environment_floors";
	public static final String ENVIRONMENT_OUTLINE = "environment_outline";
	public static final String ENVIRONMENT_POSITION = "environment_position";

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ExtSensor.class.getName());

	public ExtSensor() {
		super();
	}

	public ExtSensor(Map<String, Object> properties) {
		super(properties);
	}

	public ExtSensor(Sensor jso) {
		this();
		setId(jso.getId());
		setName(jso.getName());
		setType(jso.getType());
		setDescription(jso.getDescription());
		setDataStructure(jso.getDataStructure());
		setDataType(jso.getDataType());
		setDisplayName(jso.getDisplayName());
		setPagerType(jso.getPagerType());

		// device
		Device device = jso.getDevice();
		if (null != device) {
			setDevice(new ExtDevice(jso.getDevice()));
		}

		// environment
		Environment environment = jso.getEnvironment();
		if (null != environment) {
			setEnvironment(new ExtEnvironment(jso.getEnvironment()));
		}

		// owner
		User owner = jso.getOwner();
		if (null != owner) {
			setOwner(new ExtUser(jso.getOwner()));
		} else {
			setOwner(Registry.<ExtUser> get(Constants.REG_USER));
		}

		// users
		JsArray<User> users = jso.getUsers();
		List<ExtUser> extUsers = new ArrayList<ExtUser>(users.length());
		for (int i = 0; i < users.length(); i++) {
			extUsers.add(new ExtUser(users.get(i)));
		}
		setUsers(extUsers);
	}

	public ExtSensor(TreeModel parent) {
		super(parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExtSensor) {
			final ExtSensor sensor = (ExtSensor) obj;

			boolean idEqual = getId() == sensor.getId();
			boolean ownerEqual = (getOwner() == null && sensor.getOwner() == null)
					|| (null != getOwner() && getOwner().equals(sensor.getOwner()));
			boolean environmentEqual = (getEnvironment() == null && sensor.getEnvironment() == null)
					|| (null != getEnvironment() && getEnvironment()
							.equals(sensor.getEnvironment()));
			boolean deviceEqual = (getDevice() == null && sensor.getDevice() == null)
					|| (null != getDevice() && getDevice().equals(sensor.getDevice()));
			boolean parentEqual = (getParent() == null && sensor.getParent() == null)
					|| (null != getParent() && getParent().equals(sensor.getParent()));

			return idEqual && ownerEqual && environmentEqual && deviceEqual && parentEqual;

		} else {
			return super.equals(obj);
		}
	}

	/**
	 * @return The ID of the group that can access this sensor, if the current user is not directly
	 *         able to access it. If the sensor has no alias, -1 is returned.
	 */
	public int getAlias() {
		Object property = get(ALIAS);
		if (property instanceof Integer) {
			return ((Integer) property).intValue();
		} else if (property instanceof String) {
			return Integer.parseInt((String) property);
		} else {
			return -1;
		}
	}

	/**
	 * @return A list of services that are available for this sensor.
	 */
	public List<ExtService> getAvailServices() {
		return get(AVAIL_SERVICES);
	}

	/**
	 * @return The data structure of this sensor, if it has a complex data type. For JSON sensors,
	 *         this should contain the names of the fields and their types.
	 */
	public String getDataStructure() {
		return get(DATA_STRUCTURE);
	}

	/**
	 * @return The data type of this sensor. E.g. 'float', 'string or 'json'.
	 */
	public String getDataType() {
		return get(DATA_TYPE);
	}

	/**
	 * @return The description of this sensor, of any.
	 */
	public String getDescription() {
		return get(DESCRIPTION);
	}

	/**
	 * @return The device that this sensor is connected to.
	 */
	public ExtDevice getDevice() {
		return get(DEVICE);
	}

	/**
	 * @return The sensor's display name.
	 */
	public String getDisplayName() {
		return get(DISPLAY_NAME);
	}

	/**
	 * @return The environment this sensor is connected to (if any).
	 */
	public ExtEnvironment getEnvironment() {
		return get(ENVIRONMENT);
	}

	/**
	 * @return The sensor's ID.
	 */
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

	/**
	 * @return The sensor's name.
	 */
	public String getName() {
		return get(NAME);
	}

	/**
	 * @return The sensor's owner.
	 */
	public ExtUser getOwner() {
		return get(OWNER);
	}

	/**
	 * @return The sensor's pager type.
	 */
	public String getPagerType() {
		return get(PAGER_TYPE);
	}

	/**
	 * @return The sensor type, e.g. a physical sensor, or state sensor.
	 */
	public int getType() {
		Object property = get(TYPE);
		if (property instanceof Integer) {
			return ((Integer) property).intValue();
		} else if (property instanceof String) {
			return Integer.parseInt((String) property);
		} else {
			LOGGER.severe("Missing property: " + TYPE);
			return -1;
		}
	}

	/**
	 * @return The list of users that have access to this sensor.
	 */
	public List<ExtUser> getUsers() {
		return get(USERS);
	}

	public ExtSensor setAlias(int alias) {
		set(ALIAS, alias);
		return this;
	}

	public ExtSensor setAvailServices(List<ExtService> services) {
		set(AVAIL_SERVICES, services);
		return this;
	}

	public ExtSensor setDataStructure(String dataStructure) {
		if (null == dataStructure) {
			remove(DATA_STRUCTURE);
		} else {
			set(DATA_STRUCTURE, dataStructure);
		}
		return this;
	}

	public ExtSensor setDataType(String dataType) {
		if (null == dataType) {
			remove(DATA_TYPE);
		} else {
			set(DATA_TYPE, dataType);
		}
		return this;
	}

	public ExtSensor setDescription(String physicalSensor) {
		if (null == physicalSensor) {
			remove(DESCRIPTION);
		} else {
			set(DESCRIPTION, physicalSensor);
		}
		return this;
	}

	public ExtSensor setDevice(ExtDevice device) {
		if (null == device) {
			remove(DEVICE);
			remove(DEVICE_ID);
			remove(DEVICE_TYPE);
			remove(DEVICE_UUID);
		} else {
			set(DEVICE, device);
		}
		return this;
	}

	public ExtSensor setDisplayName(String displayName) {
		if (null == displayName) {
			remove(DISPLAY_NAME);
		} else {
			set(DISPLAY_NAME, displayName);
		}
		return this;
	}

	public ExtSensor setEnvironment(ExtEnvironment environment) {
		if (null == environment) {
			remove(ENVIRONMENT);
			remove(ENVIRONMENT_FLOORS);
			remove(ENVIRONMENT_ID);
			remove(ENVIRONMENT_NAME);
			remove(ENVIRONMENT_OUTLINE);
			remove(ENVIRONMENT_POSITION);
		} else {
			set(ENVIRONMENT, environment);
		}
		return this;
	}

	public ExtSensor setId(int id) {
		set(ID, id);
		return this;
	}

	public ExtSensor setName(String name) {
		if (null == name) {
			remove(NAME);
		} else {
			set(NAME, name);
		}
		return this;
	}

	public ExtSensor setOwner(ExtUser owner) {
		if (null == owner) {
			remove(OWNER);
			remove(OWNER_EMAIL);
			remove(OWNER_ID);
			remove(OWNER_MOBILE);
			remove(OWNER_NAME);
			remove(OWNER_SURNAME);
			remove(OWNER_USERNAME);
		} else {
			set(OWNER, owner);
		}
		return this;
	}

	public ExtSensor setPagerType(String pagerType) {
		if (null == pagerType) {
			remove(PAGER_TYPE);
		} else {
			set(PAGER_TYPE, pagerType);
		}
		return this;
	}

	public ExtSensor setType(int type) {
		set(TYPE, type);
		return this;
	}

	public ExtSensor setUsers(List<ExtUser> users) {
		if (null == users) {
			remove(USERS);
		} else {
			set(USERS, users);
		}
		return this;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
