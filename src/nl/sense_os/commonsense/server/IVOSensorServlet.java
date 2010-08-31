package nl.sense_os.commonsense.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.sense_os.commonsense.server.data.PMF;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOSensorServlet extends HttpServlet {

	//private static final Logger log = Logger.getLogger("IVODataServlet");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		try {
			String s = req.getParameter("changes");
			if (s != null) {
				JSONArray changes = new JSONArray(req.getParameter("changes"));
				resp.getWriter().println(extract(changes));
			} else {
				resp.getWriter().println("Ehm... please specify something crunchable.");
			}
		} catch (JSONException e) {
			resp.getWriter().println("OOPS! sorry, something went wrong here. Did you submit valid JSON?");
		}		
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		doGet(req, resp);
	}
	
	private String extract(JSONArray changes) {
		String result = "";
		int i;

		for (i = 0; i < changes.length(); i++) {
			try {
				JSONObject change = (JSONObject) changes.get(i);
				if (change.has("CREATE")) {
					change = (JSONObject) change.get("CREATE");
					if (create(change)) {
						result += Integer.toString(change.getInt("id"));
					}
				} else if (change.has("UPDATE")) {
					change = (JSONObject) change.get("UPDATE");
					if (update(change)) {
						result += Integer.toString(((JSONObject) change.get("old")).getInt("id"));
					}
				} else if (change.has("DELETE")) {
					change = (JSONObject) change.get("DELETE");
					if (delete(change)) {
						result += Integer.toString(change.getInt("id"));
					}
				}
				result += ",";
			} catch (JSONException e) {
			}
		}	
		return result;
	}
	
	private boolean create(JSONObject change) {
	    PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // pm.makePersistent(s);
        } finally {
            pm.close();
        }

        return true;
	}
		
	private boolean update(JSONObject change) {
		return true;
	}

	private boolean delete(JSONObject change) {
		return true;
	}

}
