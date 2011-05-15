package nl.sense_os.commonsense.client.rpc;

import java.util.List;

import nl.sense_os.commonsense.shared.GroupModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GroupsProxyAsync {

    void getGroups(String sessionId, AsyncCallback<List<GroupModel>> callback);
}
