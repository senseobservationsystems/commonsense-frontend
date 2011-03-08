package nl.sense_os.commonsense.client.services;

import java.util.List;

import nl.sense_os.commonsense.shared.ServiceModel;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sensors")
public interface SensorsProxy extends RemoteService {

    List<TreeModel> getAvailableSensors(String sessionId, TreeModel service)
            throws WrongResponseException, DbConnectionException;

    List<TreeModel> getSharedSensors(String sessionId, List<TreeModel> groups)
            throws DbConnectionException, WrongResponseException;

    List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException;

    List<TreeModel> getMyServices(String sessionId) throws DbConnectionException,
            WrongResponseException;

    List<ServiceModel> getAvailableServices(String sessionId) throws DbConnectionException,
            WrongResponseException;
}