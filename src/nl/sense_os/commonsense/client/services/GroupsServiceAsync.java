package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface GroupsServiceAsync {

    void getGroups(String sessionId, AsyncCallback<List<TreeModel>> callback);

    void createGroup(String sessionId, String name, String email, String username, String password,
            AsyncCallback<String> callback);
    
    void leaveGroup(String sessionId, String groupId, AsyncCallback<String> callback);
    
    void inviteUser(String sessionId, String groupId, String email, AsyncCallback<String> callback);
}
