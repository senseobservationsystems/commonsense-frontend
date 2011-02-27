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
            return IconHelper.create("gxt/images/gxt/icons/folder.gif");
        } else if (tagType == TagModel.TYPE_DEVICE) {
            return IconHelper.create("gxt/images/gxt/icons/folder.gif");
        } else if (tagType == TagModel.TYPE_SENSOR) {
            int type = Integer.parseInt(model.<String> get("type"));
            switch (type) {
            case 0:
                return IconHelper.create("img/icons/rss_icon.gif");
            case 1:
                return IconHelper.create("img/icons/sense_red.gif");
            case 2:
                return IconHelper.create("img/icons/sense_magenta.gif");
            case 3:
                return IconHelper.create("img/icons/sense_orange.gif");
            case 4:
                return IconHelper.create("img/icons/sense_black.gif");
            default:
                return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
            }
        } else if (tagType == TagModel.TYPE_SERVICE) {
            return IconHelper.create("gxt/images/gxt/icons/tabs.gif");
        } else if (tagType == TagModel.TYPE_USER) {
            return IconHelper.create("img/icons/user.gif");
        } else {
            Log.e(TAG, "unexpected tag type in ModelIconProvider");
            return IconHelper.create("gxt/images/gxt/icons/done.gif");
        }
    }
}
