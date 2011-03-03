package nl.sense_os.commonsense.client.services;

import java.util.List;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SensorsProxyAsync {

    void getAvailableSensors(String sessionId, TreeModel service,
            AsyncCallback<List<TreeModel>> callback);

    void getSharedSensors(String sessionId, List<TreeModel> groups,
            AsyncCallback<List<TreeModel>> callback);

    void getMySensors(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getMyServices(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getAvailableServices(String sessionId, AsyncCallback<List<TreeModel>> callback);
}
