package nl.sense_os.commonsense.main.client.ext.util;

import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public class SensorTextFilter<M extends ExtSensor> extends StoreFilterField<M> {

    @Override
    protected boolean doSelect(Store<M> store, M parent, M record, String property, String filter) {

        String matchMe = record.getDisplayName().toLowerCase() + " "
                + record.getDescription().toLowerCase() + " "
                + record.<String> get(ExtSensor.DEVICE_TYPE, "").toLowerCase() + " "
                + record.<String> get(ExtSensor.ENVIRONMENT_NAME, "").toLowerCase() + " "
                + record.<String> get(ExtSensor.OWNER_USERNAME, "").toLowerCase();

        return matchMe.contains(filter.toLowerCase());
    }
};
