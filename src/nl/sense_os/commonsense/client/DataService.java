package nl.sense_os.commonsense.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.sql.Timestamp;
import java.util.List;

import nl.sense_os.commonsense.dto.SenseTreeModel;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.TaggedDataModel;
import nl.sense_os.commonsense.dto.UserModel;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public UserModel checkLogin(String name, String password);
	public List<SenseTreeModel> getPhoneDetails();
	public List<SenseTreeModel> getSensors(int phoneId);
    public TaggedDataModel getSensorValues(TagModel tag, Timestamp begin, 
            Timestamp end);
	public List<TagModel> getTags(TagModel rootTag);
	public UserModel isSessionAlive();
	public void logout();
}
