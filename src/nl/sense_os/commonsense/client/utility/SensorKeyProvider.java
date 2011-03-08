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
        int tagType = 0;
        while (model != null) {
            if (false == model.getProperties().containsKey("tagType")) {
                Log.e(TAG, "No tagType?!?!");
                for (String property : model.getPropertyNames()) {
                    Log.e(TAG, "  " + model.get(property));
                }
                model = model.getParent();
                continue;
            }
            tagType = model.get("tagType");
            switch (tagType) {
            case TagModel.TYPE_DEVICE:
                key += "D_" + model.<String> get("id") + ": " + model.<String> get("uuid") + "; ";
                break;
            case TagModel.TYPE_GROUP:
                key += "G_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
                break;
            case TagModel.TYPE_SENSOR:
                key += "S_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
                break;
            case TagModel.TYPE_SERVICE:
                key += "X_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
                break;
            case TagModel.TYPE_USER:
                key += "U_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
                break;
            case TagModel.TYPE_CATEGORY:
                key += "C_" + model.<String> get("text") + "; ";
                break;
            default:
                Log.d(TAG, "Unexpected tagType");
            }
            model = model.getParent();
        }
        return key;
    }
}
