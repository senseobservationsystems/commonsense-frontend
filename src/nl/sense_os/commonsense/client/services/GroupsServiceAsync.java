package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface GroupsServiceAsync {

    public void getGroups(String sessionId, AsyncCallback<List<TreeModel>> callback);
}
