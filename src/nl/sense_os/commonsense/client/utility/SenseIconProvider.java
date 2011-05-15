package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.constants.Constants;
import nl.sense_os.commonsense.shared.models.DeviceModel;
import nl.sense_os.commonsense.shared.models.GroupModel;
import nl.sense_os.commonsense.shared.models.SensorModel;
import nl.sense_os.commonsense.shared.models.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SenseIconProvider<M extends TreeModel> implements ModelIconProvider<M> {

    private static final String TAG = "SenseIconProvider";

    public static final String GXT_ICONS_PATH = "gxt/images/gxt/icons/";
    public static final String SENSE_ICONS_PATH = "img/icons/16/";

    public static final AbstractImagePrototype ICON_DEVICE = IconHelper.create(SENSE_ICONS_PATH
            + "phone_Android.png");
    public static final AbstractImagePrototype ICON_GROUP = IconHelper.create(SENSE_ICONS_PATH
            + "group.png");
    public static final AbstractImagePrototype ICON_GXT_DONE = IconHelper.create(GXT_ICONS_PATH
            + "done.gif");
    public static final AbstractImagePrototype ICON_GXT_FOLDER = IconHelper.create(GXT_ICONS_PATH
            + "folder.gif");
    public static final AbstractImagePrototype ICON_GXT_LEAF = IconHelper.create(GXT_ICONS_PATH
            + "tabs.gif");
    public static final AbstractImagePrototype ICON_SENSOR_DEVICE = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_red.gif");
    public static final AbstractImagePrototype ICON_SENSOR_ENVIRONMENT = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange.gif");
    public static final AbstractImagePrototype ICON_SENSOR_FEED = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "rss.gif");
    public static final AbstractImagePrototype ICON_SENSOR_PUBLIC = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_black.gif");
    public static final AbstractImagePrototype ICON_SENSOR_STATE = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_magenta.gif");
    public static final AbstractImagePrototype ICON_USER = IconHelper.create(SENSE_ICONS_PATH
            + "user.png");
    public static final AbstractImagePrototype ICON_USER_ME = IconHelper.create(SENSE_ICONS_PATH
            + "user_zorro.png");

    public static final AbstractImagePrototype ICON_LOADING = IconHelper
            .create("gxt/images/gxt/icons/loading.gif");
    public static final AbstractImagePrototype ICON_BUTTON_GO = IconHelper
            .create("gxt/images/gxt/icons/page-next.gif");

    @Override
    public AbstractImagePrototype getIcon(M model) {

        if (model instanceof GroupModel) {
            return ICON_GROUP;

        } else if (model instanceof DeviceModel) {
            return ICON_DEVICE;

        } else if (model instanceof SensorModel) {
            final int type = Integer.parseInt(model.<String> get("type"));
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

        } else if (model instanceof UserModel) {
            final UserModel me = Registry.<UserModel> get(Constants.REG_USER);
            if (model.equals(me)) {
                return ICON_USER_ME;
            } else {
                return ICON_USER;
            }

        } else {
            Log.e(TAG, "Unexpected model class: " + model);
            return ICON_GXT_DONE;
        }
    }
}
