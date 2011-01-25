package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;
import nl.sense_os.commonsense.shared.exceptions.InternalError;

@RemoteServiceRelativePath("groups")
public interface GroupsService extends RemoteService {

    List<TreeModel> getGroups(String sessionId) throws DbConnectionException,
            WrongResponseException;

    String createGroup(String sessionId, String name, String email, String username, String password)
            throws InternalError, WrongResponseException, DbConnectionException;

    String leaveGroup(String sessionId, String groupId) throws WrongResponseException,
            DbConnectionException;

    String inviteUser(String sessionId, String groupId, String email) throws InternalError,
            WrongResponseException, DbConnectionException;
}
