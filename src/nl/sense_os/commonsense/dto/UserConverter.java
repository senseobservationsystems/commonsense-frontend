package nl.sense_os.commonsense.dto;

import nl.sense_os.commonsense.server.data.User;

public class UserConverter {

	public static UserModel entityToModel(User user) {  
	   UserModel userModel = new UserModel(
			   user.getName(),
			   user.getPassword());
	   return userModel;  
	}
	
}
