package nl.sense_os.commonsense.main.client.ext.model;

import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.User;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Model for a user. GXT-style bean, used in various GXT components.
 */
public class ExtUser extends BaseTreeModel {

	public static final String ID = "id";
	public static final String EMAIL = "email";
	public static final String MOBILE = "mobile";
	public static final String NAME = "name";
	public static final String SURNAME = "surname";
	public static final String USERNAME = "username";
	public static final String UUID = "uuid";
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ExtUser.class.getName());

	public ExtUser() {
		super();
	}

	public ExtUser(Map<String, Object> properties) {
		super(properties);
	}

	public ExtUser(TreeModel parent) {
		super(parent);
	}

	public ExtUser(User jso) {
		this();
		setId(jso.getId());
		setEmail(jso.getEmail());
		setMobile(jso.getMobile());
		setName(jso.getName());
		setSurname(jso.getSurname());
		setUsername(jso.getUsername());
		setUuid(jso.getUuid());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExtUser) {
			ExtUser user = ((ExtUser) obj);
			if (null == user.getParent()) {
				return getId() == ((ExtUser) obj).getId();
			} else {
				if (user.getParent() != this.getParent()) {
					return false;
				} else {
					return getId() == ((ExtUser) obj).getId();
				}
			}
		} else {
			return super.equals(obj);
		}
	}

	public String getEmail() {
		return get(EMAIL);
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

	public String getMobile() {
		return get(MOBILE);
	}

	public String getName() {
		return get(NAME);
	}

	public String getSurname() {
		return get(SURNAME);
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public String getUuid() {
		return get(UUID);
	}

	public ExtUser setEmail(String email) {
		set(EMAIL, email);
		return this;
	}

	public ExtUser setId(int id) {
		set(ID, id);
		return this;
	}

	public ExtUser setMobile(String mobile) {
		set(MOBILE, mobile);
		return this;
	}

	public ExtUser setName(String name) {
		set(NAME, name);
		return this;
	}

	public ExtUser setSurname(String surname) {
		set(SURNAME, surname);
		return this;
	}

	public ExtUser setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	private void setUuid(String uuid) {
		set(UUID, uuid);
	}

	@Override
	public String toString() {
		return getUsername();
	}
}
