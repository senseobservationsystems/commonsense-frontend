package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface TagsServiceAsync {

    void getMySensors(String sessionId, AsyncCallback<List<TreeModel>> callback);
    
    void getGroupSensors(String sessionId, TreeModel group, AsyncCallback<TreeModel> callback);
    
    void shareSensors(String sessionId, List<TreeModel> sensors, TreeModel user, AsyncCallback<Integer> callback);
    
    void getServices(String sessionId, AsyncCallback<List<TreeModel>> callback);
}
