package nl.sense_os.commonsense.main.client.ext.util;

import nl.sense_os.commonsense.main.client.ext.model.ExtDevice;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtService;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Utility class to copy Sense tree-based objects.
 */
public class TreeCopier {

    private TreeCopier() {
        // Private constructor to make sure this class is not instantiated.
    }

    /**
     * Copies a TreeModel by creating a new instance, including it's children. This is useful
     * because otherwise the original TreeModel will be detached from it's original parent.
     * 
     * @param sensor
     *            the original
     * @return copy of the original TreeModel
     */
    public static TreeModel copySensor(ModelData sensor) {
        TreeModel copy = null;

        // create copy instance with same properties as original
        if (sensor instanceof ExtSensor) {
            copy = new ExtSensor(sensor.getProperties());
        } else if (sensor instanceof ExtDevice) {
            copy = new ExtDevice(sensor.getProperties());
        } else if (sensor instanceof ExtGroup) {
            copy = new ExtGroup(sensor.getProperties());
        } else if (sensor instanceof ExtUser) {
            copy = new ExtUser(sensor.getProperties());
        } else if (sensor instanceof ExtService) {
            copy = new ExtService(sensor.getProperties());
        } else if (sensor instanceof TreeModel) {
            copy = new BaseTreeModel(sensor.getProperties());
        }

        // recurse over children
        if (null != copy) {
            for (ModelData child : ((TreeModel) sensor).getChildren()) {
                TreeModel childCopy = copySensor(child);
                if (null != childCopy) {
                    copy.add(childCopy);
                }
            }
        }

        return copy;
    }
}
