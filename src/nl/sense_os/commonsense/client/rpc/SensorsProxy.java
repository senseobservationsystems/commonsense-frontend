package nl.sense_os.commonsense.client.rpc;

import java.util.List;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;
import nl.sense_os.commonsense.shared.models.ServiceModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sensors")
public interface SensorsProxy extends RemoteService {

    List<TreeModel> getAvailableSensors(String sessionId, String serviceName)
            throws WrongResponseException, DbConnectionException;

    List<TreeModel> getSharedSensors(String sessionId, List<TreeModel> groups)
            throws DbConnectionException, WrongResponseException;

    List<TreeModel> getMySensors(String sessionId) throws DbConnectionException,
            WrongResponseException;

    List<TreeModel> getStateSensors(String sessionId) throws DbConnectionException,
            WrongResponseException;

    List<ServiceModel> getAvailableServices(String sessionId) throws DbConnectionException,
            WrongResponseException;
}