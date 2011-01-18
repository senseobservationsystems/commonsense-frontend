package nl.sense_os.commonsense.client.services;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

@RemoteServiceRelativePath("tags")
public interface TagService extends RemoteService {
    
    public List<TreeModel> getTags(String sessionId) throws DbConnectionException,
            WrongResponseException;
}