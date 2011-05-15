package nl.sense_os.commonsense.client.rpc;

import java.util.List;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;
import nl.sense_os.commonsense.shared.models.GroupModel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("groups")
public interface GroupsProxy extends RemoteService {

    List<GroupModel> getGroups(String sessionId) throws DbConnectionException,
            WrongResponseException;
}
