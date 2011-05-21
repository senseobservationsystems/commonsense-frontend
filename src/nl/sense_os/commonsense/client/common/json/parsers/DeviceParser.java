package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.HashMap;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.models.DeviceModel;
import nl.sense_os.commonsense.shared.models.TagModel;

import com.google.gwt.json.client.JSONObject;

public class DeviceParser {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger("DeviceParser");

    public static DeviceModel parse(JSONObject json) {

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(DeviceModel.ID, json.get(DeviceModel.ID).isString().stringValue());
        props.put(DeviceModel.TYPE, json.get(DeviceModel.TYPE).isString().stringValue());
        props.put(DeviceModel.UUID, json.get(DeviceModel.UUID).isString().stringValue());

        // optional properties

        // front end-only properties
        props.put("tagType", TagModel.TYPE_DEVICE);
        props.put("text", props.get(DeviceModel.TYPE));

        return new DeviceModel(props);
    }

}
