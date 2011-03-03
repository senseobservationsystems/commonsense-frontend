package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SensorIconProvider implements ModelIconProvider<TreeModel> {

    private static final String TAG = "SensorIconProvider";

    @Override
    public AbstractImagePrototype getIcon(TreeModel model) {
        int tagType = model.<Integer> get("tagType");
        if (tagType == TagModel.TYPE_GROUP) {
            return IconHelper.create("img/icons/icon_user_group_16px.png");
        } else if (tagType == TagModel.TYPE_DEVICE) {
            return IconHelper.create("img/icons/icon_mobile_16px.png");
        } else if (tagType == TagModel.TYPE_SENSOR) {
            int type = Integer.parseInt(model.<String> get("type"));
            switch (type) {
                case 0 :
                    return IconHelper.create("img/icons/icon_rss_16px.gif");
                case 1 :
                    return IconHelper.create("img/icons/icon_sense_red_16px.gif");
                case 2 :
                    return IconHelper.create("img/icons/icon_sense_magenta_16px.gif");
                case 3 :
                    return IconHelper.create("img/icons/icon_sense_orange_16px.gif");
                case 4 :
                    return IconHelper.create("img/icons/icon_sense_black_16px.gif");
                default :
                    return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
            }
        } else if (tagType == TagModel.TYPE_SERVICE) {
            return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
        } else if (tagType == TagModel.TYPE_USER) {
            return IconHelper.create("img/icons/icon_user_16px.png");
        } else if (tagType == TagModel.TYPE_CATEGORY) {
            return IconHelper.create("gxt/images/gxt/icons/folder.gif");
        } else {
            Log.e(TAG, "unexpected tag type in ModelIconProvider: " + tagType);
            return IconHelper.create("gxt/images/gxt/icons/done.gif");
        }
    }
}
