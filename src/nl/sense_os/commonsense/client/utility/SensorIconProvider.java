package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.TagModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SensorIconProvider implements ModelIconProvider<TreeModel> {

    private static final String TAG = "SensorIconProvider";
    public static final String SENSE_ICONS_PATH = "img/icons/16/";
    public static final String GXT_ICONS_PATH = "gxt/images/gxt/icons/";

    @Override
    public AbstractImagePrototype getIcon(TreeModel model) {

        final int tagType = model.<Integer> get("tagType");
        if (tagType == TagModel.TYPE_GROUP) {
            return IconHelper.create(SENSE_ICONS_PATH + "group.png");

        } else if (tagType == TagModel.TYPE_DEVICE) {
            return IconHelper.create(SENSE_ICONS_PATH + "phone_Android.png");

        } else if (tagType == TagModel.TYPE_SENSOR) {
            final int type = Integer.parseInt(model.<String> get("type"));
            switch (type) {
                case 0 :
                    return IconHelper.create(SENSE_ICONS_PATH + "rss.gif");
                case 1 :
                    return IconHelper.create(SENSE_ICONS_PATH + "sense_red.gif");
                case 2 :
                    return IconHelper.create(SENSE_ICONS_PATH + "sense_magenta.gif");
                case 3 :
                    return IconHelper.create(SENSE_ICONS_PATH + "sense_orange.gif");
                case 4 :
                    return IconHelper.create(SENSE_ICONS_PATH + "sense_black.gif");
                default :
                    return IconHelper.create(GXT_ICONS_PATH + "tabs.gif");
            }

        } else if (tagType == TagModel.TYPE_SERVICE) {
            return IconHelper.create(GXT_ICONS_PATH + "tabs.gif");

        } else if (tagType == TagModel.TYPE_USER) {
            final UserModel me = Registry.<UserModel> get(Constants.REG_USER);
            if (null != me && me.get(UserModel.ID).equals(model.get(UserModel.ID))) {
                return IconHelper.create(SENSE_ICONS_PATH + "user_zorro.png");
            } else {
                return IconHelper.create(SENSE_ICONS_PATH + "user.png");
            }

        } else if (tagType == TagModel.TYPE_CATEGORY) {
            return IconHelper.create(GXT_ICONS_PATH + "folder.gif");

        } else {
            Log.e(TAG, "unexpected tag type in ModelIconProvider: " + tagType);
            return IconHelper.create(GXT_ICONS_PATH + "done.gif");
        }
    }
}
