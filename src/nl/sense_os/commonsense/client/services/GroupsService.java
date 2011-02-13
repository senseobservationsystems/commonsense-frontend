package nl.sense_os.commonsense.client.services;

import java.util.List;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("groups")
public interface GroupsService extends RemoteService {

    List<TreeModel> getGroups(String sessionId) throws DbConnectionException,
            WrongResponseException;
}
