package nl.sense_os.commonsense.client.common.utility;

import nl.sense_os.commonsense.client.common.models.DeviceModel;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.common.models.ServiceModel;
import nl.sense_os.commonsense.client.common.models.UserModel;

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
        if (sensor instanceof SensorModel) {
            copy = new SensorModel(sensor.getProperties());
        } else if (sensor instanceof DeviceModel) {
            copy = new DeviceModel(sensor.getProperties());
        } else if (sensor instanceof GroupModel) {
            copy = new GroupModel(sensor.getProperties());
        } else if (sensor instanceof UserModel) {
            copy = new UserModel(sensor.getProperties());
        } else if (sensor instanceof ServiceModel) {
            copy = new ServiceModel(sensor.getProperties());
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
