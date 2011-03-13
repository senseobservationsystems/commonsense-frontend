package nl.sense_os.commonsense.client.services;

import java.util.List;

import nl.sense_os.commonsense.shared.ServiceModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SensorsProxyAsync {

    void getAvailableSensors(String sessionId, String serviceName,
            AsyncCallback<List<TreeModel>> callback);

    void getSharedSensors(String sessionId, List<TreeModel> groups,
            AsyncCallback<List<TreeModel>> callback);

    void getMySensors(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getStateSensors(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getAvailableServices(String sessionId, AsyncCallback<List<ServiceModel>> callback);
}
