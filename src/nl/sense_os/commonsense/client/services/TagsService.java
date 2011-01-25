package nl.sense_os.commonsense.client.services;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

@RemoteServiceRelativePath("tags")
public interface TagsService extends RemoteService {

    List<TreeModel> getTags(String sessionId) throws DbConnectionException, WrongResponseException;

    List<TreeModel> getGroupSensors(String sessionId) throws DbConnectionException,
            WrongResponseException;
}