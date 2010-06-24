package nl.sense_os.commonsense.server;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		try {
			JSONArray changes = new JSONArray(req.getParameter("changes"));
			//resp.getWriter().println(json.toString());
			resp.getWriter().println(extract(changes));
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
		JSONArray result = new JSONArray();
		Random rnd = new Random(new Date().getTime());
		int i;

		for (i = 0; i < changes.length(); i++) {
			try {
				JSONObject change = (JSONObject) changes.get(i);
				JSONObject createContainer = new JSONObject();
				JSONObject create = new JSONObject();
				if (change.has("CREATE")) {
					change = (JSONObject) change.get("CREATE");
					int userId = rnd.nextInt(100)+50;
					create.put("userId", userId);
					createContainer.put("CREATE", create);
					result.put(createContainer);
				}
				if (change.has("UPDATE")) {
					change = (JSONObject) change.get("UPDATE");
					int userId = ((JSONObject) change.get("old")).getInt("userId");
					create.put("userId", userId);
					createContainer.put("UPDATE", create);
					result.put(createContainer);
				}
				if (change.has("DELETE")) {
					change = (JSONObject) change.get("DELETE");
					int userId = change.getInt("userId");
					create.put("userId", userId);
					createContainer.put("DELETE", create);
					result.put(createContainer);
				}
			} catch (JSONException e) {
			}
		}	
		return result.toString();
	}
}
