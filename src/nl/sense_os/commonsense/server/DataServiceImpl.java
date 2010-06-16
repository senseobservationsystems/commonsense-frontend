package nl.sense_os.commonsense.server;

import javax.servlet.http.HttpSession;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.User;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
public class DataServiceImpl extends RemoteServiceServlet implements
		DataService {

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

		if (userName.equalsIgnoreCase("gwt")) { // Check the database
			User user = new User();
			user.setUserName(userName);
			setUserInSession(user);
			return user;
		} else
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
