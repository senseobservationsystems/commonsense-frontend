package nl.sense_os.commonsense.client.rpc;

import java.util.List;

import nl.sense_os.commonsense.client.sensors.library.LibraryController;
import nl.sense_os.commonsense.shared.models.ServiceModel;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SensorsProxyAsync {

    void getAvailableSensors(String sessionId, String serviceName,
            AsyncCallback<List<TreeModel>> callback);

    void getSharedSensors(String sessionId, List<TreeModel> groups,
            AsyncCallback<List<TreeModel>> callback);

    /**
     * @deprecated use client-side code instead: {@link LibraryController}
     */
    @Deprecated
    void getMySensors(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getStateSensors(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getAvailableServices(String sessionId, AsyncCallback<List<ServiceModel>> callback);
}