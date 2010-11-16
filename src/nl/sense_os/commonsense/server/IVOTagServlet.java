package nl.sense_os.commonsense.server;

import javax.jdo.PersistenceManager;

import nl.sense_os.commonsense.server.persistent.PMF;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;


@SuppressWarnings("serial")
public class IVOTagServlet extends IVOServlet {
	
	@Override
    protected void initialize() {
	}

	@Override
    protected void finalize() {
	}

	@Override
    protected String create(JSONObject change) throws JSONException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
   	    try {
            // pm.makePersistent(s);
        } finally {
            pm.close();
        }
        return Integer.toString(change.getInt("id"));
	}
		
	@Override
    protected String update(JSONObject change) throws JSONException {
		return Integer.toString(((JSONObject) change.get("old")).getInt("id"));
	}

	@Override
    protected String delete(JSONObject change) throws JSONException {
        return Integer.toString(change.getInt("id"));
	}

}
