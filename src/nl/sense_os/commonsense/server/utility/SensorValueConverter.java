package nl.sense_os.commonsense.server.utility;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import java.sql.Timestamp;
import java.util.logging.Logger;

import nl.sense_os.commonsense.dto.BluetoothValueModel;
import nl.sense_os.commonsense.dto.BooleanValueModel;
import nl.sense_os.commonsense.dto.CallStateValueModel;
import nl.sense_os.commonsense.dto.DoubleValueModel;
import nl.sense_os.commonsense.dto.MyriaHumValueModel;
import nl.sense_os.commonsense.dto.MyriaTempValueModel;
import nl.sense_os.commonsense.dto.PositionValueModel;
import nl.sense_os.commonsense.dto.SensorValueModel;
import nl.sense_os.commonsense.dto.ServiceStateValueModel;
import nl.sense_os.commonsense.dto.StringValueModel;
import nl.sense_os.commonsense.server.data.BluetoothValue;
import nl.sense_os.commonsense.server.data.BooleanValue;
import nl.sense_os.commonsense.server.data.CallStateValue;
import nl.sense_os.commonsense.server.data.DoubleValue;
import nl.sense_os.commonsense.server.data.MyriaHumValue;
import nl.sense_os.commonsense.server.data.MyriaTempValue;
import nl.sense_os.commonsense.server.data.PositionValue;
import nl.sense_os.commonsense.server.data.SensorValue;
import nl.sense_os.commonsense.server.data.ServiceStateValue;
import nl.sense_os.commonsense.server.data.StringValue;

public class SensorValueConverter {

    private static final Logger log = Logger.getLogger("SensorValueConverter");

    @SuppressWarnings("deprecation")
    public static SensorValueModel entityToModel(SensorValue sensorValue) throws JSONException {
        SensorValueModel sensorValueModel = null;

        // find out the sensor type
        final Timestamp ts = sensorValue.getTimestamp();
        final int sensorType = sensorValue.getType();
        switch (sensorType) {
        case SensorValue.AUDIOSTREAM:
        case SensorValue.BLUETOOTH_ADDR:
        case SensorValue.DATA_CONNECTION:
        case SensorValue.IP:
        case SensorValue.MIC:
            StringValue sv = (StringValue) sensorValue;
            sensorValueModel = new StringValueModel(ts, sensorType, sv.getValue());
            break;
        case SensorValue.BLUETOOTH_DISC:
            BluetoothValue bt = (BluetoothValue) sensorValue;
            sensorValueModel = new BluetoothValueModel(ts, bt.getLocalAddress(), bt.getNames(), bt.getAddresses(), bt.getRssis());
            break;
        case SensorValue.CALLSTATE:
            CallStateValue cs = (CallStateValue) sensorValue;
            sensorValueModel = new CallStateValueModel(ts, cs.getCallState(), cs.getNumber());
            break;
        case SensorValue.DEVICE_PROPS:
            // do nothing
            log.warning("Device properties is not a valid sensor value type");
            break;
        case SensorValue.MYRIA_HUMIDITY:
            MyriaHumValue mh = (MyriaHumValue) sensorValue;
            sensorValueModel = new MyriaHumValueModel(ts, mh.getNodeId(), mh.getSensorName(),
                    mh.getValue(), mh.getVariance());
            break;
        case SensorValue.MYRIA_TEMPERATURE:
            MyriaTempValue mt = (MyriaTempValue) sensorValue;
            sensorValueModel = new MyriaTempValueModel(ts, mt.getNodeId(), mt.getSensorName(),
                    mt.getValue(), mt.getVariance());
            break;
        case SensorValue.NOISE:
            DoubleValue dv = (DoubleValue) sensorValue;
            sensorValueModel = new DoubleValueModel(ts, sensorType, dv.getValue());
            break;
        case SensorValue.POSITION:
            PositionValue pv = (PositionValue) sensorValue;
            sensorValueModel = new PositionValueModel(ts, pv.getAccuracy(), pv.getAltitude(), pv.getLatitude(), pv.getLongitude(),
                    pv.getSpeed());
            break;
        case SensorValue.SERVICE_STATE:
            ServiceStateValue ssv = (ServiceStateValue) sensorValue;
            sensorValueModel = new ServiceStateValueModel(ts, ssv.getState(), ssv.getPhoneNumber(), ssv.getManualSet());
            break;
        case SensorValue.UNREAD_MSG:
            BooleanValue bv = (BooleanValue) sensorValue;
            sensorValueModel = new BooleanValueModel(ts, sensorType, bv.getValue());
            break;
        }
        return sensorValueModel;
    }

    @SuppressWarnings("deprecation")
    public static SensorValue jsonToEntity(JSONObject jsonSensorValue, int sensorType)
            throws JSONException {
        SensorValue s = null;

        Timestamp ts = TimestampConverter.epochSecsToTimestamp(jsonSensorValue.getString("t"));
        String value = jsonSensorValue.getString("v");

        switch (sensorType) {
        case SensorValue.AUDIOSTREAM:
        case SensorValue.BLUETOOTH_ADDR:
        case SensorValue.DATA_CONNECTION:
        case SensorValue.IP:
        case SensorValue.MIC:
            s = new StringValue(ts, sensorType, value);
            break;
        case SensorValue.BLUETOOTH_DISC:
            JSONObject btDevices = new JSONObject(value);
            String localAddress = btDevices.getString("local_bt_address");
            JSONArray devices = btDevices.getJSONArray("bt_devices");
            String[] addresses = new String[devices.length()];
            String[] names = new String[devices.length()];
            String[] rssis = new String[devices.length()];
            for (int i = 0; i < devices.length(); i++) {
                JSONObject obj = devices.getJSONObject(i);
                addresses[i] = obj.getString("address");
                names[i] = obj.getString("name");
                rssis[i] = obj.getString("rssi");
            }
            s = new BluetoothValue(ts, localAddress, names, addresses, rssis);
            break;
        case SensorValue.CALLSTATE:
            JSONObject callState = new JSONObject(value);
            String state = callState.getString("state");
            String number = null;
            if (state.equals("ringing")) {
                number = callState.getString("incomingNumber");
            }
            s = new CallStateValue(ts, state, number);
            break;
        case SensorValue.DEVICE_PROPS:
            // do nothing
            log.warning("Device properties is not a valid sensor value type");
            break;
        case SensorValue.MYRIA_HUMIDITY:
            JSONObject myriaHum = new JSONObject(value);
            int myriaHumNodeId = myriaHum.getInt("node_id");
            String myriaHumSensorName = myriaHum.getString("sensor");
            int myriaHumVal = myriaHum.getInt("value");
            int myriaHumVar = myriaHum.getInt("variance");
            s = new MyriaHumValue(ts, myriaHumNodeId, myriaHumSensorName, myriaHumVal, myriaHumVar);
            break;
        case SensorValue.MYRIA_TEMPERATURE:
            JSONObject myriaTemp = new JSONObject(value);
            int myriaTempNodeId = myriaTemp.getInt("node_id");
            String myriaTempSensorName = myriaTemp.getString("sensor");
            int myriaTempVal = myriaTemp.getInt("value");
            int myriaTempVar = myriaTemp.getInt("variance");
            s = new MyriaTempValue(ts, myriaTempNodeId, myriaTempSensorName, myriaTempVal,
                    myriaTempVar);
            break;
        case SensorValue.NOISE:
            s = new DoubleValue(ts, sensorType, Double.parseDouble(value));
            break;
        case SensorValue.POSITION:
            JSONObject pos = new JSONObject(value);
            double accuracy = pos.getDouble("accuracy");
            double altitude = pos.getDouble("altitude");
            double latitude = pos.getDouble("latitude");
            double longitude = pos.getDouble("longitude");
            double speed = pos.getDouble("speed");
            s = new PositionValue(ts, accuracy, altitude, latitude, longitude, speed);
            break;
        case SensorValue.SERVICE_STATE:
            JSONObject stateObj = new JSONObject(value);
            String serviceState = stateObj.getString("state");
            String phoneNumber = null;
            if (serviceState.equals("in service")) {
                phoneNumber = stateObj.getString("phone number");
            }
            String manualSet = stateObj.getString("manualSet");
            s = new ServiceStateValue(ts, serviceState, phoneNumber, manualSet);
            break;
        case SensorValue.UNREAD_MSG:
            s = new BooleanValue(ts, sensorType, Boolean.parseBoolean(value));
            break;
        }

        return s;
    }
}
