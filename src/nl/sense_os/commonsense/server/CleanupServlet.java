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

import nl.sense_os.commonsense.server.persistent.PMF;

@SuppressWarnings("serial")
public class CleanupServlet extends HttpServlet {

	protected static final Logger log = Logger.getLogger("IVOServlet");

	@Override
    @SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
		resp.setContentType("text/plain");
		
		String entityName = req.getParameter("entityName");
		if (entityName == null)
			resp.getWriter().println("Specify entityName and amount (optional)");
		else {
			int amount;
			String amountString = req.getParameter("amount");
			if (amountString != null)
				amount = Integer.parseInt(amountString);
			else
				amount = -1;
			
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				String query = "select from " + entityName;
				Query q = pm.newQuery(query);
				if (amount != -1)
					q.setRange(0, amount);
				List<Object> o = (List<Object>) q.execute();
				pm.deletePersistentAll(o);
				resp.getWriter().println(o.size());
			} catch (JDOFatalUserException e) {
				resp.getWriter().println("Problem: " + entityName + " unknown!");
				log.warning("Cleanup failed - entityName " + entityName + " unknown.");
			} finally {
				pm.close();
			} 
		}
	}	
}
