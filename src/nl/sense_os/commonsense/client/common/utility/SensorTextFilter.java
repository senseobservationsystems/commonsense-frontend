package nl.sense_os.commonsense.client.common.utility;

import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public class SensorTextFilter<M extends SensorModel> extends StoreFilterField<M> {

    @Override
    protected boolean doSelect(Store<M> store, M parent, M record, String property, String filter) {

        String matchMe = record.getDisplayName().toLowerCase() + " "
                + record.getDescription().toLowerCase() + " "
                + record.<String> get(SensorModel.DEVICE_TYPE, "").toLowerCase() + " "
                + record.<String> get(SensorModel.ENVIRONMENT_NAME, "").toLowerCase() + " "
                + record.<String> get(SensorModel.OWNER_USERNAME, "").toLowerCase();

        return matchMe.contains(filter.toLowerCase());
    }
};
