package nl.sense_os.commonsense.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.sense_os.commonsense.server.data.BooleanValue;
import nl.sense_os.commonsense.server.data.FloatValue;
import nl.sense_os.commonsense.server.data.JsonValue;
import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.SensorType;
import nl.sense_os.commonsense.server.data.StringValue;

@SuppressWarnings("serial")
public class CleanupServlet extends HttpServlet {

	protected static final Logger log = Logger.getLogger("IVOServlet");

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String query = "select from " + SensorType.class.getName();
			List<SensorType> sensorTypes = (List<SensorType>) pm.newQuery(query).execute();
			pm.deletePersistentAll(sensorTypes);

			query = "select from " + FloatValue.class.getName();
			List<FloatValue> floatValues = (List<FloatValue>) pm.newQuery(query).execute();
			pm.deletePersistentAll(floatValues);

			query = "select from " + StringValue.class.getName();
			List<StringValue> stringValues = (List<StringValue>) pm.newQuery(query).execute();
			pm.deletePersistentAll(stringValues);

			query = "select from " + JsonValue.class.getName();
			List<JsonValue> jsonValues = (List<JsonValue>) pm.newQuery(query).execute();
			pm.deletePersistentAll(jsonValues);

			query = "select from " + BooleanValue.class.getName();
			List<BooleanValue> booleanValues = (List<BooleanValue>) pm.newQuery(query).execute();
			pm.deletePersistentAll(booleanValues);
			
		} finally {
			pm.close();
		}
		resp.getWriter().println("Everything removed!");
	}	
}
