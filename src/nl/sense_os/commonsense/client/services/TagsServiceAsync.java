package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface TagsServiceAsync {

    public void getTags(String sessionId, AsyncCallback<List<TreeModel>> callback);
    
    public void getGroupSensors(String sessionId, AsyncCallback<List<TreeModel>> callback);
}
