package nl.sense_os.commonsense.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.User;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
public class DataServiceImpl extends RemoteServiceServlet implements
		DataService {

    public static final String URL_BASE = "http://demo.almende.com/commonSense/";    
    public static final String URL_LOGIN = URL_BASE + "login.php";
    
	private static final String USER_SESSION = "GWTAppUser";

	private static final long serialVersionUID = 1;

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
			User user = new User();
			user.setUserName(line + password);
			setUserInSession(user);
			return user;
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
		User bean = getUserFromSession();
		if ((bean != null) && (bean.getUserName().length() != 0)) {
			System.out.println("User " + bean.getUserName()
					+ " is already logged in");
			return bean;
		}
		return null;
	}
}
