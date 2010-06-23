package nl.sense_os.commonsense.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.data.Phone;
import nl.sense_os.commonsense.data.User;
import nl.sense_os.commonsense.rpc.DataService;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements
		DataService {

    private static final String URL_BASE = "http://demo.almende.com/commonSense/gae/";    
    private static final String URL_LOGIN = URL_BASE + "login.php";
    private static final String URL_GET_PHONE_DETAILS = URL_BASE + "get_phone_details.php";
	private static final String USER_SESSION = "GWTAppUser";

	private void setUserInSession(User user) {
		HttpSession session = getThreadLocalRequest().getSession();
		session.setAttribute(USER_SESSION, user);
	}

	private User getUserFromSession() {
		HttpSession session = getThreadLocalRequest().getSession();
		return (User) session.getAttribute(USER_SESSION);
	}
	
	public User checkLogin(String userName, String password) {
        try {
            final URL url = new URL(URL_LOGIN + "?email=" + userName + "&password=" + password);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            line = reader.readLine();
            reader.close();
            if (!line.toLowerCase().contains("ok")) {
            	return null;
            } else {
            	User user = new User();
            	user.setUserName(userName);
            	setUserInSession(user);
            	return user;
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

	public User isSessionAlive() {
		User user = getUserFromSession();
		if ((user != null) && (user.getUserName().length() != 0)) {
			System.out.println("User " + user.getUserName()
					+ " is already logged in");
			return user;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Phone> getPhoneDetails() {
        List<Phone> phoneList = new ArrayList<Phone>();
		String jsonText = "";
		
		User user = getUserFromSession();
		
		// Get json object
        try {
            final URL url = new URL(URL_GET_PHONE_DETAILS + "?email=" + user.getUserName() + "?password=" + user.getPassword());
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
		JSONObject phones = ((JSONObject) JSONParser.parse((String) jsonText)).get("phones").isObject();
		Set keys = phones.keySet();
		Iterator ki = keys.iterator();
		while (ki.hasNext()) {
			String key = (String) ki.next();
			JSONObject jsonPhone = phones.get(key).isObject();
			Phone phone = new Phone(key, jsonPhone);
			phoneList.add(phone);
		}
		return phoneList;
	}
}
