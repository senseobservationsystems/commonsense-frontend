package nl.sense_os.commonsense.main.client.ext.util;

import nl.sense_os.commonsense.common.client.util.Constants;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
import nl.sense_os.commonsense.main.client.ext.model.ExtUser;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;

public class SensorOwnerFilter<M extends ExtSensor> implements StoreFilter<M> {

	private boolean enabled;

	@Override
	public boolean select(Store<M> store, M parent, M item, String property) {
		ExtUser user = Registry.get(Constants.REG_USER);
		return !enabled || item.get(ExtSensor.OWNER_USERNAME, "").equals(user.getUsername());
	};

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
};
