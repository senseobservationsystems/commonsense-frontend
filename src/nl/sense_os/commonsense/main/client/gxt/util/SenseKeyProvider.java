package nl.sense_os.commonsense.main.client.gxt.util;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;

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

        if (model instanceof GxtDevice) {
            GxtDevice device = (GxtDevice) model;
            return "D_" + device.getId() + ": " + device.getUuid() + "; ";
        } else if (model instanceof GxtGroup) {
            GxtGroup group = (GxtGroup) model;
            return "G_" + group.getId() + ": " + group.getName() + "; ";
        } else if (model instanceof GxtSensor) {
            GxtSensor sensor = (GxtSensor) model;
            return "S_" + sensor.getId() + sensor.getName() + sensor.getDescription()
                    + sensor.getType() + "; ";
        } else if (model instanceof GxtUser) {
            GxtUser user = (GxtUser) model;
            return "U_" + user.getId() + ": " + user.getName() + "; ";
        } else {
            LOG.severe("Unexpected class: " + model);
            return model + "; ";
        }
    }
}
