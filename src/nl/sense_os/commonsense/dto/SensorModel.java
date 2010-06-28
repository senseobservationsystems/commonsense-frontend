package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseModel;

@SuppressWarnings("serial")
public class SensorModel extends BaseModel {

	    public SensorModel() {   
		}

		public SensorModel(String id, String name) {
		    setId(id);
		    setName(name);
		}
			
		public String getId() {
			return get("id");
		}

		public String getName() {
			return get("name");
		}

		public void setId(String id) {
	        set("id", id);
		}

		public void setName(String name) {
	        set("name", name);
		}
	}	
