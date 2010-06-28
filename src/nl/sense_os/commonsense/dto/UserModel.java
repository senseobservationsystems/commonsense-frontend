package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseModel;

@SuppressWarnings("serial")
public class UserModel extends BaseModel {

	    public UserModel() {   
		}

		public UserModel(String name, String password) {
		    setName(name);
		    setPassword(password);
		}
			
		public String getName() {
			return get("name");
		}

		public String getPassword() {
			return get("password");
		}

		public void setName(String name) {
	        set("name", name);
		}

		public void setPassword(String password) {
	        set("password", password);
		}
}	
