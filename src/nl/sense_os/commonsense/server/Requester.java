package nl.sense_os.commonsense.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

public class Requester {

    private static final Logger log = Logger.getLogger("Requester");

    public static String request(String urlString, String sessionId, String method, String data)
            throws WrongResponseException, DbConnectionException {

        // Get response from server
        String content = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(30000);
            connection.setRequestProperty("X-SESSION_ID", sessionId);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache,max-age=10");

            // log.info(method + " " + connection.getURL().getPath());

            // perform method at URL
            if (null != data) {
                // log.info(data);
                connection.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data);
                writer.close();
            }

            // get response
            int code = connection.getResponseCode();
            content = "";
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                content += line;
            }

            // print warning is request was unsuccessful
            if ((code < 200) || (code > 299)) {
                String failure = "Request " + method + " " + url.getPath() + "?" + url.getQuery()
                        + " failed. Code: " + code + " content: " + content;
                log.severe(failure);
                throw new WrongResponseException(failure);
            }
        } catch (MalformedURLException e) {
            log.severe("MalformedURLException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        } catch (IOException e) {
            log.severe("IOException: " + e.getMessage());
            throw (new DbConnectionException(e.getMessage()));
        }

        return content;
    }
}
