package nl.sense_os.commonsense.common.client.util;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.DeviceModel;
import nl.sense_os.commonsense.common.client.model.GroupModel;
import nl.sense_os.commonsense.common.client.model.SensorModel;
import nl.sense_os.commonsense.common.client.model.UserModel;

import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * Key provider to help grids and trees distinguish between different sensors.
 */
public class SenseKeyProvider<M extends TreeModel> implements ModelKeyProvider<M> {

    private static final Logger LOG = Logger.getLogger(SenseKeyProvider.class.getName());

    @Override
    public String getKey(M initialModel) {
        String key = "";
        TreeModel model = initialModel;
        while (model != null) {
            key += getPartialKey(model);
            model = model.getParent();
        }
        return key;
    }

    private String getPartialKey(TreeModel model) {

        if (model instanceof DeviceModel) {
            DeviceModel device = (DeviceModel) model;
            return "D_" + device.getId() + ": " + device.getUuid() + "; ";
        } else if (model instanceof GroupModel) {
            GroupModel group = (GroupModel) model;
            return "G_" + group.getId() + ": " + group.getName() + "; ";
        } else if (model instanceof SensorModel) {
            SensorModel sensor = (SensorModel) model;
            return "S_" + sensor.getId() + sensor.getName() + sensor.getDescription()
                    + sensor.getType() + "; ";
        } else if (model instanceof UserModel) {
            UserModel user = (UserModel) model;
            return "U_" + user.getId() + ": " + user.getName() + "; ";
        } else {
            LOG.severe("Unexpected class: " + model);
            return model + "; ";
        }
    }
}
