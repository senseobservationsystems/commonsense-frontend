package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface TagsServiceAsync {

    void disconnectService(String sessionId, String sensorId, String serviceId,
            AsyncCallback<Void> callback);

    void getGroupSensors(String sessionId, TreeModel group, AsyncCallback<TreeModel> callback);

    void getMySensors(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void getMyServices(String sessionId, AsyncCallback<List<TreeModel>> callback);
    
    void getAvailableServices(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void shareSensors(String sessionId, List<TreeModel> sensors, TreeModel user,
            AsyncCallback<Void> callback);
}
