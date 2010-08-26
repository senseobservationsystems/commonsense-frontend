package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.sql.Timestamp;
import java.util.List;

import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
import nl.sense_os.commonsense.dto.exceptions.TooMuchDataException;
import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
    public UserModel checkLogin(String name, String password) throws DbConnectionException,
            WrongResponseException;

    // public List<SenseTreeModel> getPhoneDetails();
    // public List<SenseTreeModel> getSensors(int phoneId);

    public TaggedDataModel getSensorValues(TagModel tag, Timestamp begin, Timestamp end)
            throws TooMuchDataException, DbConnectionException, WrongResponseException;

    public List<TagModel> getTags(TagModel rootTag) throws DbConnectionException,
            WrongResponseException;

    public UserModel isSessionAlive();

    public void logout();
}
