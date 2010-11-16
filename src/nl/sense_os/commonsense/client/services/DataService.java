package nl.sense_os.commonsense.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.Date;
import java.util.List;

import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
import nl.sense_os.commonsense.dto.exceptions.InternalError;
import nl.sense_os.commonsense.dto.exceptions.TooMuchDataException;
import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;
import nl.sense_os.commonsense.dto.sensorvalues.TaggedDataModel;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
    
    public void addLocationValues(int x, int y, int deviceId, String blobKey) throws DbConnectionException, WrongResponseException, InternalError;
    
    public UserModel checkLogin(String name, String password) throws DbConnectionException,
            WrongResponseException;

    public TaggedDataModel getIvoSensorValues(TagModel tag, Date begin, Date end)
            throws WrongResponseException;

    public TaggedDataModel getSensorValues(TagModel tag, Date begin, Date end)
            throws TooMuchDataException, DbConnectionException, WrongResponseException;

    public TaggedDataModel getSensorValuesPaged(TagModel tag, int offset, int limit)
            throws TooMuchDataException, DbConnectionException, WrongResponseException;

    public List<TagModel> getTags(TagModel rootTag) throws DbConnectionException,
            WrongResponseException;

    public UserModel isSessionAlive();

    public void logout();
}