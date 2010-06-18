package nl.sense_os.commonsense.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import nl.sense_os.commonsense.client.DataService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class DataServiceImpl extends RemoteServiceServlet implements
		DataService {

    public static final String URL_BASE = "http://demo.almende.com/commonSense/";    
    public static final String URL_GET_PHONE_DETAILS = URL_BASE + "get_phone_details.php";

	public String getPhoneDetails() {
        try {
            final URL url = new URL(URL_GET_PHONE_DETAILS);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String jsonText = "";
            String line;
            while ((line = reader.readLine()) != null) {
            	jsonText += line;
            }
            reader.close();
			return jsonText;
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
		return null;
	}
}
