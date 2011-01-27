package nl.sense_os.commonsense.client.services;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.InternalError;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

@RemoteServiceRelativePath("tags")
public interface TagsService extends RemoteService {

    List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException;

    TreeModel getGroupSensors(String sessionId, TreeModel group) throws DbConnectionException,
            WrongResponseException;

    Integer shareSensors(String sessionId, List<TreeModel> sensors, TreeModel user)
            throws DbConnectionException, WrongResponseException, InternalError;

    List<TreeModel> getServices(String sessionId) throws DbConnectionException, WrongResponseException;
}