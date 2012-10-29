package nl.sense_os.commonsense.main.client.gxt.util;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.shared.client.util.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;

public class SensorOwnerFilter<M extends GxtSensor> implements StoreFilter<M> {

	private boolean enabled;

	@Override
	public boolean select(Store<M> store, M parent, M item, String property) {
		GxtUser user = Registry.get(Constants.REG_USER);
		return !enabled || item.get(GxtSensor.OWNER_USERNAME, "").equals(user.getUsername());
	};

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
};
