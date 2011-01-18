package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface TagServiceAsync {

    public void getTags(String sessionId, AsyncCallback<List<TreeModel>> callback);
}
