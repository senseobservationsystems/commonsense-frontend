package nl.sense_os.commonsense.main.client.gxt.util;

import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtEnvironment;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;

import com.extjs.gxt.ui.client.data.ModelProcessor;

/**
 * ModelProcessor for easy display of the sensor's owner, device and environment.
 * 
 * @param <M>
 */
public class SensorProcessor<M extends GxtSensor> extends ModelProcessor<M> {

    @Override
    public M prepareData(M model) {

        final GxtUser owner = model.getOwner();
        if (null != owner) {
            model.set(GxtSensor.OWNER_EMAIL, owner.getEmail());
            model.set(GxtSensor.OWNER_ID, "" + owner.getId());
            model.set(GxtSensor.OWNER_MOBILE, owner.getMobile());
            model.set(GxtSensor.OWNER_NAME, owner.getName());
            model.set(GxtSensor.OWNER_SURNAME, owner.getSurname());
            model.set(GxtSensor.OWNER_USERNAME, owner.getUsername());
        } else {
            model.remove(GxtSensor.OWNER_EMAIL);
            model.remove(GxtSensor.OWNER_ID);
            model.remove(GxtSensor.OWNER_MOBILE);
            model.remove(GxtSensor.OWNER_NAME);
            model.remove(GxtSensor.OWNER_SURNAME);
            model.remove(GxtSensor.OWNER_USERNAME);
        }

        final GxtDevice device = model.getDevice();
        if (null != device) {
            model.set(GxtSensor.DEVICE_ID, "" + device.getId());
            model.set(GxtSensor.DEVICE_TYPE, device.getType());
            model.set(GxtSensor.DEVICE_UUID, device.getUuid());
        } else {
            model.remove(GxtSensor.DEVICE_ID);
            model.remove(GxtSensor.DEVICE_TYPE);
            model.remove(GxtSensor.DEVICE_UUID);
        }

        final GxtEnvironment environment = model.getEnvironment();
        if (null != environment) {
            model.set(GxtSensor.ENVIRONMENT_ID, "" + environment.getId());
            model.set(GxtSensor.ENVIRONMENT_NAME, environment.getName());
            model.set(GxtSensor.ENVIRONMENT_FLOORS, "" + environment.getFloors());
            model.set(GxtSensor.ENVIRONMENT_OUTLINE, "" + environment.getOutline());
            model.set(GxtSensor.ENVIRONMENT_POSITION, "" + environment.getPosition());
        } else {
            model.remove(GxtSensor.ENVIRONMENT_ID);
            model.remove(GxtSensor.ENVIRONMENT_NAME);
            model.remove(GxtSensor.ENVIRONMENT_FLOORS);
            model.remove(GxtSensor.ENVIRONMENT_OUTLINE);
            model.remove(GxtSensor.ENVIRONMENT_POSITION);
        }

        return model;
    }
}
