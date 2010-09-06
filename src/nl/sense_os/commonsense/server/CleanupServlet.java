package nl.sense_os.commonsense.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.sense_os.commonsense.server.data.PMF;
import nl.sense_os.commonsense.server.data.SensorType;

@SuppressWarnings("serial")
public class CleanupServlet extends HttpServlet {

	protected static final Logger log = Logger.getLogger("IVOServlet");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		
		String entityName = req.getParameter("entityName");
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String query = "select from " + entityName;
			Query q = pm.newQuery(query);
			long deletedCount = q.deletePersistentAll();
			resp.getWriter().println(deletedCount + " Entities removed!");
		} catch (JDOFatalUserException e) {
			resp.getWriter().println("Problem: " + entityName + " unknown!");
			log.warning("Cleanup failed - entityName " + entityName + " unknown.");
		} finally {
			pm.close();
		} 
	}	
}
