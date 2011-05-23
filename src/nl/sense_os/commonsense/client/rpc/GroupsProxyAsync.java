package nl.sense_os.commonsense.client.rpc;

import java.util.List;

import nl.sense_os.commonsense.client.common.models.GroupModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @deprecated Use client-side code to get the groups through XHR.
 */
@Deprecated
public interface GroupsProxyAsync {

    void getGroups(String sessionId, AsyncCallback<List<GroupModel>> callback);
}
