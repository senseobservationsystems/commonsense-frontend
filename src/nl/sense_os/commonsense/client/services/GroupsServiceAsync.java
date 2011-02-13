package nl.sense_os.commonsense.client.services;

import java.util.List;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GroupsServiceAsync {

    void getGroups(String sessionId, AsyncCallback<List<TreeModel>> callback);
}
