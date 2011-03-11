package nl.sense_os.commonsense.shared;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Utility class to copy Sense tree-based objects.
 */
public class Copier {

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
