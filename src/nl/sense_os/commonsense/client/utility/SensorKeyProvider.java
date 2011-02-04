package nl.sense_os.commonsense.client.utility;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Key provider to help grids and trees distinguish between different sensors.
 */
public class SensorKeyProvider implements ModelKeyProvider<TreeModel> {

    @Override
    public String getKey(TreeModel model) {
        String key = model.<String> get("text");
        TreeModel parent = model.getParent();
        while (parent != null) {
            key += "/" + parent.<String> get("text");
            parent = parent.getParent();
        }
        return key;
    }

}
