package nl.sense_os.commonsense.server;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.dto.PhoneConverter;
import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.dto.SensorConverter;
import nl.sense_os.commonsense.dto.UserConverter;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.pojo.Phone;
import nl.sense_os.commonsense.pojo.Sensor;
import nl.sense_os.commonsense.pojo.User;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements
		DataService {

    private static final String URL_BASE = "http://demo.almende.com/commonSense/gae/";    
    private static final String URL_LOGIN = URL_BASE + "login.php";
    private static final String URL_GET_PHONE_DETAILS = URL_BASE + "get_phone_details.php";
    private static final String URL_GET_PHONE_SENSORS = URL_BASE + "get_phone_sensors.php";
	private static final String USER_SESSION = "GWTAppUser";

	private void setUserInSession(User user) {
		HttpSession session = getThreadLocalRequest().getSession();
		session.setAttribute(USER_SESSION, user);
	}

	private User getUserFromSession() {
		HttpSession session = getThreadLocalRequest().getSession();
		return (User) session.getAttribute(USER_SESSION);
	}
	
	public UserModel checkLogin(String name, String password) {
        try {
            final URL url = new URL(URL_LOGIN + "?email=" + name + "&password=" + password);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            line = reader.readLine();
            reader.close();
            if (!line.toLowerCase().contains("ok")) {
            	return null;
            } else {
            	User user = new User();
            	user.setName(name);
            	user.setPassword(password);
            	setUserInSession(user);
            	return UserConverter.entityToModel(user);
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
		return null;
	}

	public void logout() {
		HttpSession session = getThreadLocalRequest().getSession();
		if (session != null)
			session.invalidate();
	}

	public UserModel isSessionAlive() {
		User user = getUserFromSession();
		if ((user != null) && (user.getName().length() != 0)) {
			System.out.println("User " + user.getName()
					+ " is already logged in");
			return UserConverter.entityToModel(user);
		}
		return null;
	}

	public List<PhoneModel> getPhoneDetails() {
	    
        List<PhoneModel> phoneList = new ArrayList<PhoneModel>();
		String jsonText = "";
		
		User user = getUserFromSession();
		
		// Get json object
        try {
            final URL url = new URL(URL_GET_PHONE_DETAILS + "?email=" + user.getName() + "&password=" + user.getPassword());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
            	jsonText += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        
        // Convert to object
		JSONObject phones;
		try {
			phones = (JSONObject) new JSONObject(jsonText).get("phones");
			String[] keys = JSONObject.getNames(phones);
			if (keys != null) {
				for (int i = 0; i < keys.length; i++) {
					JSONObject jsonPhone = (JSONObject) phones.get(keys[i]);
					Phone phone = PhoneConverter.jsonToEntity(jsonPhone);
					//phone.setSensors(getPhoneSensors(phone));
					phoneList.add(PhoneConverter.entityToModel(phone));
				}
			}
		} catch (JSONException e) {
		}
		return phoneList;
	}

	private List<Sensor> getPhoneSensors(Phone phone) {
		List<Sensor> sensorList = new ArrayList<Sensor>();
		String jsonText = "";

		User user = getUserFromSession();
		
		// Get json object
        try {
            final URL url = new URL(URL_GET_PHONE_SENSORS + "?email=" + user.getName() + "&password=" + user.getPassword() + "&sp_id=" + phone.getId());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
            	jsonText += line;
            }
            reader.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        
        // Convert to object
		JSONObject sensors;
		try {
			sensors = (JSONObject) new JSONObject(jsonText).get("sensors");
			String[] keys = JSONObject.getNames(sensors);
			if (keys != null) {
				for (int i = 0; i < keys.length; i++) {
					JSONObject jsonSensor = (JSONObject) sensors.get(keys[i]);
					Sensor sensor = SensorConverter.jsonToEntity(jsonSensor);
					sensorList.add(sensor);
				}
			}
		} catch (JSONException e) {
		}
		return sensorList;
	}	
	
}
