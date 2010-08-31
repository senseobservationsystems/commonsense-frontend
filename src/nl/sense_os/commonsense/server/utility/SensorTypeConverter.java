package nl.sense_os.commonsense.server.utility;

import java.util.logging.Logger;
import nl.sense_os.commonsense.server.data.SensorType;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class SensorTypeConverter {

    private static final Logger log = Logger.getLogger("SensorTypeConverter");

    public static SensorType jsonToEntity(JSONObject jsonSensorType) throws JSONException {
		String jsonDataType = jsonSensorType.getString("data_type");
		int dataType = -1;
		if (jsonDataType.equals("string")) {
			dataType = SensorType.STRING;
		} else if (jsonDataType.equals("json")) {
			dataType = SensorType.JSON;
		} else if (jsonDataType.equals("float")) {
			dataType = SensorType.FLOAT;
		} else if (jsonDataType.equals("bool")) {
			dataType = SensorType.BOOL;
		} else {
		    log.warning("Error converting sensor value: Unknown data type.");
		}

		SensorType sensorType = new SensorType(jsonSensorType.getInt("id"), jsonSensorType.getString("name"), dataType);

		return sensorType;
    }

}
