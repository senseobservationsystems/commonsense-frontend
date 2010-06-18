package nl.sense_os.commonsense.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable{
	
	private String userName;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
