package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.TagModel;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Key provider to help grids and trees distinguish between different sensors.
 */
public class SenseKeyProvider<M extends TreeModel> implements ModelKeyProvider<M> {

    private static final String TAG = "SenseKeyProvider";

    @Override
    public String getKey(M model) {
        String key = "";

        while (model != null) {
            if (false == model.getProperties().containsKey("tagType")) {
                Log.e(TAG, "No tagType?!?!");
                for (String property : model.getPropertyNames()) {
                    Log.e(TAG, "  " + model.get(property));
                }
                model = (M) model.getParent();
                continue;
            }
            key += getPartialKey(model);
            model = (M) model.getParent();
        }
        return key;
    }

    private String getPartialKey(M model) {
        int tagType = model.get("tagType");
        switch (tagType) {
            case TagModel.TYPE_DEVICE :
                return "D_" + model.<String> get("id") + ": " + model.<String> get("uuid") + "; ";
            case TagModel.TYPE_GROUP :
                return "G_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
            case TagModel.TYPE_SENSOR :
                return "S_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
            case TagModel.TYPE_SERVICE :
                return "X_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
            case TagModel.TYPE_USER :
                return "U_" + model.<String> get("id") + ": " + model.<String> get("text") + "; ";
            case TagModel.TYPE_CATEGORY :
                return "C_" + model.<String> get("text") + "; ";
            default :
                Log.d(TAG, "Unexpected tagType");
                return "error";
        }
    }
}
