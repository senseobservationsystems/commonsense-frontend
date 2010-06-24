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
			JSONObject json = new JSONObject(req.getParameter("changes"));
			resp.getWriter().println(json.toString());
			resp.getWriter().println(extract(json));
		} catch (JSONException e) {
			resp.getWriter().println("OOPS! sorry, something went wrong here. Did you submit valid JSON?");
		}		
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		doGet(req, resp);
	}
	
	private String extract(JSONObject json) {
		JSONObject result = new JSONObject();
		Random rnd = new Random(new Date().getTime());
		JSONArray ar;
		int i;
		
		// Creates
		JSONArray creates = new JSONArray(); 
		try {
			ar = (JSONArray) json.get("CREATE");
			for (i = 0; i < ar.length(); i++) {
				int userId = rnd.nextInt(100)+50;
				JSONObject create = new JSONObject();
				create.put("userId", userId);
				creates.put(create);
			}
			result.put("CREATE", creates);
		} catch (JSONException e) {
		}
		
		// Updates
		JSONArray updates = new JSONArray();
		try {
			ar = (JSONArray) json.get("UPDATE");
			for (i = 0; i < ar.length(); i++) {
				int userId = ((JSONObject) ((JSONObject) ar.get(i)).get("old")).getInt("userId");
				JSONObject update = new JSONObject();
				update.put("userId", userId);
				updates.put(update);
			}
			result.put("UPDATE", updates);
		} catch (JSONException e) {
		}

		// Deletes
		JSONArray deletes = new JSONArray();
		try {
			ar = (JSONArray) json.get("DELETE");
			for (i = 0; i < ar.length(); i++) {
				int userId = ((JSONObject) ar.get(i)).getInt("userId");
				JSONObject delete = new JSONObject();
				delete.put("userId", userId);
				deletes.put(delete);
			}
			result.put("DELETE", updates);
		} catch (JSONException e) {
		}
		return result.toString();
	}	
}