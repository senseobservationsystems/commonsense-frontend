package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.models.DeviceModel;
import nl.sense_os.commonsense.shared.models.EnvironmentModel;
import nl.sense_os.commonsense.shared.models.SensorModel;
import nl.sense_os.commonsense.shared.models.UserModel;

import com.extjs.gxt.ui.client.data.ModelProcessor;

/**
 * ModelProcessor for easy display of the sensor's owner, device and environment.
 * 
 * @param <M>
 */
public class SensorProcessor<M extends SensorModel> extends ModelProcessor<M> {

    @Override
    public M prepareData(M model) {

        final UserModel owner = model.getOwner();
        if (null != owner) {
            model.set(SensorModel.OWNER_EMAIL, owner.getEmail());
            model.set(SensorModel.OWNER_ID, owner.getId());
            model.set(SensorModel.OWNER_MOBILE, owner.getMobile());
            model.set(SensorModel.OWNER_NAME, owner.getName());
            model.set(SensorModel.OWNER_SURNAME, owner.getSurname());
            model.set(SensorModel.OWNER_USERNAME, owner.getUsername());
        } else {
            model.remove(SensorModel.OWNER_EMAIL);
            model.remove(SensorModel.OWNER_ID);
            model.remove(SensorModel.OWNER_MOBILE);
            model.remove(SensorModel.OWNER_NAME);
            model.remove(SensorModel.OWNER_SURNAME);
            model.remove(SensorModel.OWNER_USERNAME);
        }

        final DeviceModel device = model.getDevice();
        if (null != device) {
            model.set(SensorModel.DEVICE_ID, device.getId());
            model.set(SensorModel.DEVICE_TYPE, device.getType());
            model.set(SensorModel.DEVICE_UUID, device.getUuid());
        } else {
            model.remove(SensorModel.DEVICE_ID);
            model.remove(SensorModel.DEVICE_TYPE);
            model.remove(SensorModel.DEVICE_UUID);
        }

        final EnvironmentModel environment = model.getEnvironment();
        if (null != environment) {
            model.set(SensorModel.ENVIRONMENT_ID, environment.getId());
            model.set(SensorModel.ENVIRONMENT_NAME, environment.getName());
            model.set(SensorModel.ENVIRONMENT_FLOORS, environment.getFloors());
            model.set(SensorModel.ENVIRONMENT_OUTLINE, environment.getOutline());
            model.set(SensorModel.ENVIRONMENT_POSITION, environment.getPosition());
        } else {
            model.remove(SensorModel.ENVIRONMENT_ID);
            model.remove(SensorModel.ENVIRONMENT_NAME);
            model.remove(SensorModel.ENVIRONMENT_FLOORS);
            model.remove(SensorModel.ENVIRONMENT_OUTLINE);
            model.remove(SensorModel.ENVIRONMENT_POSITION);
        }

        return model;
    }
}
