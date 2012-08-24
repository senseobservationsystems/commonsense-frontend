package nl.sense_os.commonsense.main.client.gxt.util;

import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtService;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;

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
        if (sensor instanceof GxtSensor) {
            copy = new GxtSensor(sensor.getProperties());
        } else if (sensor instanceof GxtDevice) {
            copy = new GxtDevice(sensor.getProperties());
        } else if (sensor instanceof GxtGroup) {
            copy = new GxtGroup(sensor.getProperties());
        } else if (sensor instanceof GxtUser) {
            copy = new GxtUser(sensor.getProperties());
        } else if (sensor instanceof GxtService) {
            copy = new GxtService(sensor.getProperties());
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
