package nl.sense_os.commonsense.common.client.util;

import nl.sense_os.commonsense.common.client.constant.Constants;
import nl.sense_os.commonsense.common.client.model.SensorModel;
import nl.sense_os.commonsense.common.client.model.UserModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;

public class SensorOwnerFilter<M extends SensorModel> implements StoreFilter<M> {

    private boolean enabled;

    @Override
    public boolean select(Store<M> store, M parent, M item, String property) {
        UserModel user = Registry.get(Constants.REG_USER);
        return !enabled || item.get(SensorModel.OWNER_USERNAME, "").equals(user.getUsername());
    };

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
};
