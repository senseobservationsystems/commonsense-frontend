package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.DeviceModel;
import nl.sense_os.commonsense.shared.EnvironmentModel;
import nl.sense_os.commonsense.shared.SensorModel;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.data.ModelProcessor;

/**
 * ModelProcessor for easy display of the sensor's owner, device and environment.
 * 
 * @param <M>
 */
public class SensorProcessor<M extends SensorModel> extends ModelProcessor<M> {

    @Override
    public M prepareData(M model) {

        @SuppressWarnings("unchecked")
        final M result = (M) new SensorModel(model.getProperties());

        final UserModel owner = model.getOwner();
        if (null != owner) {
            result.set(SensorModel.OWNER_EMAIL, owner.getEmail());
            result.set(SensorModel.OWNER_ID, owner.getId());
            result.set(SensorModel.OWNER_MOBILE, owner.getMobile());
            result.set(SensorModel.OWNER_NAME, owner.getName());
            result.set(SensorModel.OWNER_SURNAME, owner.getSurname());
            result.set(SensorModel.OWNER_USERNAME, owner.getUsername());
        }

        final DeviceModel device = model.getDevice();
        if (null != device) {
            result.set(SensorModel.DEVICE_ID, device.getId());
            result.set(SensorModel.DEVICE_TYPE, device.getType());
            result.set(SensorModel.DEVICE_UUID, device.getUuid());
        }

        final EnvironmentModel environment = model.getEnvironment();
        if (null != environment) {
            result.set(SensorModel.ENVIRONMENT_ID, environment.getId());
            result.set(SensorModel.ENVIRONMENT_NAME, environment.getName());
            result.set(SensorModel.ENVIRONMENT_FLOORS, environment.getFloors());
            result.set(SensorModel.ENVIRONMENT_OUTLINE, environment.getOutline());
            result.set(SensorModel.ENVIRONMENT_POSITION, environment.getPosition());
        }
        return result;
    }
}
