package nl.sense_os.commonsense.main.client.ext.util;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.ext.model.ExtDevice;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SenseIconProvider<M extends TreeModel> implements ModelIconProvider<M> {

	private static final Logger LOGGER = Logger.getLogger(SenseIconProvider.class.getName());

    public static final String GXT_ICONS_PATH = "commonsense/gxt/images/gxt/icons/";
	public static final String SENSE_ICONS_PATH = "img/icons/16/";

	public static final AbstractImagePrototype ICON_DEVICE = IconHelper.create(SENSE_ICONS_PATH
			+ "phone_Android.png");
	public static final AbstractImagePrototype ICON_GROUP = IconHelper.create(SENSE_ICONS_PATH
			+ "group.png");
	public static final AbstractImagePrototype ICON_GOOGLE = IconHelper.create(SENSE_ICONS_PATH
			+ "google.gif");
	public static final AbstractImagePrototype ICON_GXT_DONE = IconHelper.create(GXT_ICONS_PATH
			+ "done.gif");
	public static final AbstractImagePrototype ICON_GXT_FOLDER = IconHelper.create(GXT_ICONS_PATH
			+ "folder.gif");
	public static final AbstractImagePrototype ICON_GXT_LEAF = IconHelper.create(GXT_ICONS_PATH
			+ "tabs.gif");
	public static final AbstractImagePrototype ICON_SENSOR_DEVICE = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_red-unshared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_DEVICE_SHARED = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_red-shared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_ENVIRONMENT = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-unshared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_FEED = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-unshared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_PUBLIC = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_black-unshared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_STATE = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_magenta-unshared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_ENVIRONMENT_SHARED = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-shared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_FEED_SHARED = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange-shared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_PUBLIC_SHARED = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_black-shared.gif");
	public static final AbstractImagePrototype ICON_SENSOR_STATE_SHARED = IconHelper
			.create(SenseIconProvider.SENSE_ICONS_PATH + "sense_magenta-shared.gif");
	public static final AbstractImagePrototype ICON_USER = IconHelper.create(SENSE_ICONS_PATH
			+ "user.png");
	public static final AbstractImagePrototype ICON_USER_ME = IconHelper.create(SENSE_ICONS_PATH
			+ "user_zorro.png");

	@Override
	public AbstractImagePrototype getIcon(M model) {

		if (model instanceof ExtGroup) {
			return ICON_GROUP;

		} else if (model instanceof ExtDevice) {
			return ICON_DEVICE;

		} else if (model instanceof ExtSensor) {
			ExtSensor sensor = (ExtSensor) model;

			List<ExtUser> users = sensor.getUsers();
			ExtUser currentUser = Registry.get(Constants.REG_USER);
			int type = sensor.getType();
			if (users != null
					&& (users.size() > 1 || (!users.contains(currentUser) && users.size() > 0))) {
				switch (type) {
				case 0:
					return ICON_SENSOR_FEED_SHARED;
				case 1:
					return ICON_SENSOR_DEVICE_SHARED;
				case 2:
					return ICON_SENSOR_STATE_SHARED;
				case 3:
					return ICON_SENSOR_PUBLIC_SHARED;
				case 4:
					return ICON_SENSOR_ENVIRONMENT_SHARED;
				default:
					return ICON_GXT_LEAF;
				}
			} else {
				switch (type) {
				case 0:
					return ICON_SENSOR_FEED;
				case 1:
					return ICON_SENSOR_DEVICE;
				case 2:
					return ICON_SENSOR_STATE;
				case 3:
					return ICON_SENSOR_PUBLIC;
				case 4:
					return ICON_SENSOR_ENVIRONMENT;
				default:
					return ICON_GXT_LEAF;
				}
			}

		} else if (model instanceof ExtUser) {
			final ExtUser me = Registry.<ExtUser> get(Constants.REG_USER);
			if (model.equals(me)) {
				return ICON_USER_ME;
			} else {
				return ICON_USER;
			}

		} else {
			LOGGER.severe("Unexpected model class: " + model);
			return ICON_GXT_DONE;
		}
	}
}
