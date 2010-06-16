package nl.sense_os.commonsense.client;

import java.io.Serializable;

public class User implements Serializable{
	/**
	 * Add this variable for serialization 
	 */
	private static final long serialVersionUID = 1L;
	
	private String userName;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
