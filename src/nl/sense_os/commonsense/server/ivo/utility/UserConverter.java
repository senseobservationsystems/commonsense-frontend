package nl.sense_os.commonsense.server.ivo.utility;

import nl.sense_os.commonsense.server.ivo.data.User;
import nl.sense_os.commonsense.shared.UserModel;

public class UserConverter {

    public static UserModel entityToModel(User user) {
        return new UserModel("" + user.getId(), user.getUsername(), user.getEmail(),
                user.getName(), user.getSurname(), user.getMobile(), user.getUuid());
    }

    public static User modelToEntity(UserModel userModel) {
        int id = Integer.parseInt(userModel.getId());
        return new User(id, userModel.getUsername(), userModel.getEmail(), userModel.getName(),
                userModel.getSurname(), userModel.getMobile(), userModel.getUuid());
    }
}
