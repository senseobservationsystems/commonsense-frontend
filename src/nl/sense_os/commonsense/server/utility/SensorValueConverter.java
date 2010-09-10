package nl.sense_os.commonsense.server.utility;

public abstract class SensorValueConverter {

    /*
    public static SensorValueModel entityToModel(SensorValue sensorValue) throws JSONException {
        SensorValueModel sensorValueModel = null;

        // find out the sensor type
        final Date ts = sensorValue.getTimestamp();
        final int dataType = sensorValue.getType();
        switch (dataType) {
        case SensorValue.BOOL:
            BooleanValue bv = (BooleanValue) sensorValue;
            sensorValueModel = new BooleanValueModel(ts, bv.getValue());
            break;
        case SensorValue.FLOAT:
            FloatValue fv = (FloatValue) sensorValue;
            sensorValueModel = new FloatValueModel(ts, fv.getValue());
            break;
        case SensorValue.JSON:
            JsonValue jv = (JsonValue) sensorValue;
            sensorValueModel = new JsonValueModel(ts, jv.getFields());
            break;
        case SensorValue.STRING:
            StringValue sv = (StringValue) sensorValue;
            sensorValueModel = new StringValueModel(ts, sv.getValue());
            break;
        }
        return sensorValueModel;
    }

    // compatibility method
    public static SensorValue jsonToEntity(int deviceId, int sensorType, JSONObject jsonSensorValue, String dataType)
            throws JSONException {
        SensorValue s = null;

        Date ts = TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t"));
        String value = jsonSensorValue.getString("v");

        if (dataType.equals("string")) {
            s = new StringValue(deviceId, sensorType, ts, value);
        } else if (dataType.equals("json")) {
            s = new JsonValue(deviceId, sensorType, ts, value);
        } else if (dataType.equals("float")) {
            s = new FloatValue(deviceId, sensorType, ts, Double.parseDouble(value));
        } else if (dataType.equals("bool")) {
            s = new BooleanValue(deviceId, sensorType, ts, Boolean.parseBoolean(value));
        } else {
            log.warning("Error converting sensor value: Unknown data type.");
        }

        return s;
    }

    public static SensorValue jsonToEntity(JSONObject jsonSensorValue, int dataType) throws JSONException {
    	SensorValue s = null;

        Date ts = TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("date"));
    	String value = jsonSensorValue.getString("sensor_value");

    	switch (dataType) {
    		case SensorType.STRING:
    			s = new StringValue(jsonSensorValue.getInt("device_id"), jsonSensorValue.getInt("sensor_type"), ts, value.replace("\\\"", "\""));
    			break;
    		case SensorType.JSON:
    			s = new JsonValue(jsonSensorValue.getInt("device_id"), jsonSensorValue.getInt("sensor_type"), ts, value.replace("\\\"", "\""));
    			break;
    		case SensorType.FLOAT:
    			s = new FloatValue(jsonSensorValue.getInt("device_id"), jsonSensorValue.getInt("sensor_type"), ts, Double.parseDouble(value));
    			break;
    		case SensorType.BOOL:
    			s = new BooleanValue(jsonSensorValue.getInt("device_id"), jsonSensorValue.getInt("sensor_type"), ts, Boolean.parseBoolean(value));
    			break;
    		default:
        		log.warning("Error converting sensor value: Unknown data type: " + dataType);
    	}
    	return s;
    }
    */
}