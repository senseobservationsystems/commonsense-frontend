package nl.sense_os.commonsense.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public abstract class IVOServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger("IVOServlet");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		try {
			resp.setContentType("text/plain");
			String s = req.getParameter("changes");
			if (s != null) {
				JSONArray changes = new JSONArray(req.getParameter("changes"));
				resp.getWriter().println(applyChanges(changes));
			} else {
				resp.getWriter().println("Error");
				log.warning("Got something non-crunchable");
			}
		} catch (JSONException e) {
			resp.getWriter().println("Error");
			log.warning("Got something non-crunchable");
		}		
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		doGet(req, resp);
	}

	private String applyChanges(JSONArray changes) {
		String result = "";
		int i;

		for (i = 0; i < changes.length(); i++) {
			try {
				JSONObject change = (JSONObject) changes.get(i);
				if (change.has("CREATE")) {
					change = (JSONObject) change.get("CREATE");
					result += create(change);
				} else if (change.has("UPDATE")) {
					change = (JSONObject) change.get("UPDATE");
					result += update(change);
				} else if (change.has("DELETE")) {
					change = (JSONObject) change.get("DELETE");
					result += create(change);
				}
				result += ",";
			} catch (JSONException e) {
			}
		}	

		log.info("Processed " + changes.length() + " changes!");
		
		return result;
	}

	protected abstract String create(JSONObject change) throws JSONException;
	protected abstract String update(JSONObject change) throws JSONException;
	protected abstract String delete(JSONObject change) throws JSONException;
	
}
