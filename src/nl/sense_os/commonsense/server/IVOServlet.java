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

import nl.sense_os.commonsense.server.data.Counter;
import nl.sense_os.commonsense.server.data.PMF;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger("IVOServlet");

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
	
	@SuppressWarnings("unchecked")
	private String extract(JSONArray changes) {
		String result = "";
		Random rnd = new Random(new Date().getTime());
		int i;
		int total = 0;

		for (i = 0; i < changes.length(); i++) {
			try {
				JSONObject change = (JSONObject) changes.get(i);
				if (change.has("CREATE")) {
					change = (JSONObject) change.get("CREATE");
					int userId = rnd.nextInt(100)+50;
					result += Integer.toString(userId);
					total++;
				} else if (change.has("UPDATE")) {
					change = (JSONObject) change.get("UPDATE");
					int userId = ((JSONObject) change.get("old")).getInt("userId");
					result += Integer.toString(userId);
				} else if (change.has("DELETE")) {
					change = (JSONObject) change.get("DELETE");
					int userId = change.getInt("userId");
					result += Integer.toString(userId);
					total--;
				}
				result += ",";
			} catch (JSONException e) {
			}
		}	

		
		Counter c;
	    PersistenceManager pm = PMF.get().getPersistenceManager();
	    String query = "select from " + Counter.class.getName();
	    List<Counter> counters = (List<Counter>) pm.newQuery(query).execute();
	    if (counters.isEmpty()) {
	    	c = new Counter(total);
	    } else {
	    	c = counters.get(0);
	    	c.setValue(c.getValue()+total);
	    }
        try {
            pm.makePersistent(c);
        } finally {
            pm.close();
        }
		
		return result;
	}
}
