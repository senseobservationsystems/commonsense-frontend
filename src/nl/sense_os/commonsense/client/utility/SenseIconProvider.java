package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SenseIconProvider<D extends TreeModel> implements ModelIconProvider<TreeModel> {

    private static final String TAG = "SenseIconProvider";

    public static final String GXT_ICONS_PATH = "gxt/images/gxt/icons/";
    public static final String SENSE_ICONS_PATH = "img/icons/16/";
    private static final SensorIconProvider SENSORPROVIDER = new SensorIconProvider();

    protected static final AbstractImagePrototype ICON_DEVICE = IconHelper.create(SENSE_ICONS_PATH
            + "phone_Android.png");
    protected static final AbstractImagePrototype ICON_GROUP = IconHelper.create(SENSE_ICONS_PATH
            + "group.png");
    protected static final AbstractImagePrototype ICON_GXT_DONE = IconHelper.create(GXT_ICONS_PATH
            + "done.gif");
    protected static final AbstractImagePrototype ICON_GXT_FOLDER = IconHelper
            .create(GXT_ICONS_PATH + "folder.gif");
    protected static final AbstractImagePrototype ICON_GXT_LEAF = IconHelper.create(GXT_ICONS_PATH
            + "tabs.gif");
    protected static final AbstractImagePrototype ICON_SENSOR_DEVICE = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_red.gif");
    protected static final AbstractImagePrototype ICON_SENSOR_ENVIRONMENT = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_orange.gif");
    protected static final AbstractImagePrototype ICON_SENSOR_FEED = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "rss.gif");
    protected static final AbstractImagePrototype ICON_SENSOR_PUBLIC = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_black.gif");
    protected static final AbstractImagePrototype ICON_SENSOR_STATE = IconHelper
            .create(SenseIconProvider.SENSE_ICONS_PATH + "sense_magenta.gif");
    protected static final AbstractImagePrototype ICON_USER = IconHelper.create(SENSE_ICONS_PATH
            + "user.png");
    protected static final AbstractImagePrototype ICON_USER_ME = IconHelper.create(SENSE_ICONS_PATH
            + "user_zorro.png");

    @Override
    public AbstractImagePrototype getIcon(TreeModel model) {

        final int tagType = model.<Integer> get("tagType");
        if (tagType == TagModel.TYPE_GROUP) {
            return ICON_GROUP;

        } else if (tagType == TagModel.TYPE_DEVICE) {
            return ICON_DEVICE;

        } else if (tagType == TagModel.TYPE_SENSOR) {
            return SENSORPROVIDER.getIcon((SensorModel) model);

        } else if (tagType == TagModel.TYPE_SERVICE) {
            return ICON_GXT_LEAF;

        } else if (tagType == TagModel.TYPE_USER) {
            final UserModel me = Registry.<UserModel> get(Constants.REG_USER);
            if (model.equals(me)) {
                return ICON_USER_ME;
            } else {
                return ICON_USER;
            }

        } else if (tagType == TagModel.TYPE_CATEGORY) {
            return ICON_GXT_FOLDER;

        } else {
            Log.e(TAG, "Unexpected tag type in ModelIconProvider: " + tagType);
            return ICON_GXT_DONE;
        }
    }
}
