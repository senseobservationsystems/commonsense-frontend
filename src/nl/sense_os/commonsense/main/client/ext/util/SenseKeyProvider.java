package nl.sense_os.commonsense.main.client.ext.util;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.model.ExtDevice;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

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

        if (model instanceof ExtDevice) {
            ExtDevice device = (ExtDevice) model;
            return "D_" + device.getId() + ": " + device.getUuid() + "; ";
        } else if (model instanceof ExtGroup) {
            ExtGroup group = (ExtGroup) model;
            return "G_" + group.getId() + ": " + group.getName() + "; ";
        } else if (model instanceof ExtSensor) {
            ExtSensor sensor = (ExtSensor) model;
            return "S_" + sensor.getId() + sensor.getName() + sensor.getDescription()
                    + sensor.getType() + "; ";
        } else if (model instanceof ExtUser) {
            ExtUser user = (ExtUser) model;
            return "U_" + user.getId() + ": " + user.getName() + "; ";
        } else {
            LOG.severe("Unexpected class: " + model);
            return model + "; ";
        }
    }
}
