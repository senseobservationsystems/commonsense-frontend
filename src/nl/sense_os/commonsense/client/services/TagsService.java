package nl.sense_os.commonsense.client.services;

import java.util.List;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("tags")
public interface TagsService extends RemoteService {

    List<TreeModel> getAvailableSensors(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException;

    TreeModel getGroupSensors(String sessionId, TreeModel group) throws DbConnectionException,
            WrongResponseException;

    List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException;

    List<TreeModel> getMyServices(String sessionId) throws DbConnectionException,
            WrongResponseException;

    List<TreeModel> getAvailableServices(String sessionId) throws DbConnectionException,
            WrongResponseException;
}