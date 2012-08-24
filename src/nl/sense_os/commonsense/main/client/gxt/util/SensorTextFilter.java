package nl.sense_os.commonsense.main.client.gxt.util;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public class SensorTextFilter<M extends GxtSensor> extends StoreFilterField<M> {

    @Override
    protected boolean doSelect(Store<M> store, M parent, M record, String property, String filter) {

        String matchMe = record.getDisplayName().toLowerCase() + " "
                + record.getDescription().toLowerCase() + " "
                + record.<String> get(GxtSensor.DEVICE_TYPE, "").toLowerCase() + " "
                + record.<String> get(GxtSensor.ENVIRONMENT_NAME, "").toLowerCase() + " "
                + record.<String> get(GxtSensor.OWNER_USERNAME, "").toLowerCase();

        return matchMe.contains(filter.toLowerCase());
    }
};
