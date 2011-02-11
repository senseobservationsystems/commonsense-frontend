package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Key provider to help grids and trees distinguish between different sensors.
 */
public class SensorKeyProvider implements ModelKeyProvider<TreeModel> {

    private static final String TAG = "SensorKeyProvider";

    @Override
    public String getKey(TreeModel model) {
        String key = "";
        TreeModel parent = model;
        int tagType = 0;
        while (parent != null) {
            tagType = parent.get("tagType");
            switch (tagType) {
                case TagModel.TYPE_DEVICE :
                    key += "device " + parent.<String> get("uuid") + ";";
                    break;
                case TagModel.TYPE_GROUP :
                    key += "group " + parent.<String> get("text") + ";";
                    break;
                case TagModel.TYPE_SENSOR :
                    key += "sensor " + parent.<String> get("id") + parent.<String> get("text")
                            + ";";
                    break;
                case TagModel.TYPE_SERVICE :
                    key += "service " + parent.<String> get("text") + ";";
                    break;
                case TagModel.TYPE_USER :
                    key += "user " + parent.<String> get("text") + ";";
                    break;
                default :
                    Log.d(TAG, "Unexpected tagType");
            }
            parent = parent.getParent();
        }
        return key;
    }

}
