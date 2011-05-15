package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.GroupModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Key provider to help grids and trees distinguish between different sensors.
 */
public class SenseKeyProvider<M extends TreeModel> implements ModelKeyProvider<M> {

    private static final String TAG = "SenseKeyProvider";

    @SuppressWarnings("unchecked")
    @Override
    public String getKey(M model) {
        String key = "";
        while (model != null) {
            key += getPartialKey(model);
            model = (M) model.getParent();
        }
        return key;
    }

    private String getPartialKey(M model) {

        if (model instanceof DeviceModel) {
            DeviceModel device = (DeviceModel) model;
            return "D_" + device.getId() + ": " + device.getUuid() + "; ";
        } else if (model instanceof GroupModel) {
            GroupModel group = (GroupModel) model;
            return "G_" + group.getId() + ": " + group.getName() + "; ";
        } else if (model instanceof SensorModel) {
            SensorModel sensor = (SensorModel) model;
            return "S_" + sensor.getId() + sensor.getName() + sensor.getPhysicalSensor()
                    + sensor.getType() + "; ";
        } else if (model instanceof UserModel) {
            UserModel user = (UserModel) model;
            return "U_" + user.getId() + ": " + user.getName() + "; ";
        } else {
            Log.e(TAG, "Unexpected class: " + model);
            return model + "; ";
        }
    }
}
