package nl.sense_os.commonsense.client.utility;

import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SensorIconProvider implements ModelIconProvider<SensorModel> {

    @Override
    public AbstractImagePrototype getIcon(SensorModel model) {
        final int type = Integer.parseInt(model.<String> get("type"));
        switch (type) {
        case 0:
            return SenseIconProvider.ICON_SENSOR_FEED;
        case 1:
            return SenseIconProvider.ICON_SENSOR_DEVICE;
        case 2:
            return SenseIconProvider.ICON_SENSOR_STATE;
        case 3:
            return SenseIconProvider.ICON_SENSOR_PUBLIC;
        case 4:
            return SenseIconProvider.ICON_SENSOR_ENVIRONMENT;
        default:
            return SenseIconProvider.ICON_GXT_LEAF;
        }
    }
}
