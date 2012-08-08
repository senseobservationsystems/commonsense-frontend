package nl.sense_os.commonsense.common.client.util;

import nl.sense_os.commonsense.common.client.model.ExtDevice;
import nl.sense_os.commonsense.common.client.model.ExtEnvironment;
import nl.sense_os.commonsense.common.client.model.ExtSensor;
import nl.sense_os.commonsense.common.client.model.ExtUser;

import com.extjs.gxt.ui.client.data.ModelProcessor;

/**
 * ModelProcessor for easy display of the sensor's owner, device and environment.
 * 
 * @param <M>
 */
public class SensorProcessor<M extends ExtSensor> extends ModelProcessor<M> {

    @Override
    public M prepareData(M model) {

        final ExtUser owner = model.getOwner();
        if (null != owner) {
            model.set(ExtSensor.OWNER_EMAIL, owner.getEmail());
            model.set(ExtSensor.OWNER_ID, "" + owner.getId());
            model.set(ExtSensor.OWNER_MOBILE, owner.getMobile());
            model.set(ExtSensor.OWNER_NAME, owner.getName());
            model.set(ExtSensor.OWNER_SURNAME, owner.getSurname());
            model.set(ExtSensor.OWNER_USERNAME, owner.getUsername());
        } else {
            model.remove(ExtSensor.OWNER_EMAIL);
            model.remove(ExtSensor.OWNER_ID);
            model.remove(ExtSensor.OWNER_MOBILE);
            model.remove(ExtSensor.OWNER_NAME);
            model.remove(ExtSensor.OWNER_SURNAME);
            model.remove(ExtSensor.OWNER_USERNAME);
        }

        final ExtDevice device = model.getDevice();
        if (null != device) {
            model.set(ExtSensor.DEVICE_ID, "" + device.getId());
            model.set(ExtSensor.DEVICE_TYPE, device.getType());
            model.set(ExtSensor.DEVICE_UUID, device.getUuid());
        } else {
            model.remove(ExtSensor.DEVICE_ID);
            model.remove(ExtSensor.DEVICE_TYPE);
            model.remove(ExtSensor.DEVICE_UUID);
        }

        final ExtEnvironment environment = model.getEnvironment();
        if (null != environment) {
            model.set(ExtSensor.ENVIRONMENT_ID, "" + environment.getId());
            model.set(ExtSensor.ENVIRONMENT_NAME, environment.getName());
            model.set(ExtSensor.ENVIRONMENT_FLOORS, "" + environment.getFloors());
            model.set(ExtSensor.ENVIRONMENT_OUTLINE, "" + environment.getOutline());
            model.set(ExtSensor.ENVIRONMENT_POSITION, "" + environment.getPosition());
        } else {
            model.remove(ExtSensor.ENVIRONMENT_ID);
            model.remove(ExtSensor.ENVIRONMENT_NAME);
            model.remove(ExtSensor.ENVIRONMENT_FLOORS);
            model.remove(ExtSensor.ENVIRONMENT_OUTLINE);
            model.remove(ExtSensor.ENVIRONMENT_POSITION);
        }

        return model;
    }
}
