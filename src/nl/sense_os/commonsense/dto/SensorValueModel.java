package nl.sense_os.commonsense.dto;

import java.sql.Timestamp;

import com.extjs.gxt.ui.client.data.BaseModel;

@SuppressWarnings("serial")
public class SensorValueModel extends BaseModel {

	    public SensorValueModel() {   
		}

		public SensorValueModel(Timestamp timestamp, String value) {
		    setTimestamp(timestamp);
		    setValue(value);
		}
			
		public Timestamp getTimestamp() {
			return get("timestamp");
		}

		public String getValue() {
			return get("value");
		}

		public void setTimestamp(Timestamp timestamp) {
	        set("timestamp", timestamp);
		}

		public void setValue(String value) {
	        set("value", value);
		}
	}	
