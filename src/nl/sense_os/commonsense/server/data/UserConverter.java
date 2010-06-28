package nl.sense_os.commonsense.server.data;

import nl.sense_os.commonsense.dto.UserModel;

public class UserConverter {

	public static UserModel entityToModel(User user) {  
	   UserModel userModel = new UserModel(
			   user.getName(),
			   user.getPassword());
	   return userModel;  
	}
	
}
