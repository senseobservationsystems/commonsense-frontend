package nl.sense_os.commonsense.server.utility;

import nl.sense_os.commonsense.server.data.User;
import nl.sense_os.commonsense.shared.UserModel;

public class UserConverter {

	public static UserModel entityToModel(User user) {  
	   UserModel userModel = new UserModel(user.getId(), user.getUsername(), user.getEmail(), user.getName(), user.getSurname(), user.getMobile(), user.getUuid());
	   return userModel;  
	}
	
	public static User modelToEntity(UserModel userModel) {
	    return new User(userModel.getId(), userModel.getUsername(), userModel.getEmail(), userModel.getName(), userModel.getSurname(), userModel.getMobile(), userModel.getUuid());
	}	
}
