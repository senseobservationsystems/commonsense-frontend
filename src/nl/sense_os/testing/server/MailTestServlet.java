package nl.sense_os.testing.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MailTestServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {

		Properties props = new Properties();
		Session mailSession = Session.getDefaultInstance(props, null);

		try {
			Message msg = new MimeMessage(mailSession);
			
			// String email = userService.getCurrentUser().getEmail();
			// Or
			String email = "fede.hernandez@gmail.com";
			
			msg.setFrom(new InternetAddress(email));
			msg.addRecipient(Message.RecipientType.TO, 
					new InternetAddress("fede@rotterdam-cs.com", "Fede"));
			msg.setSubject("Test Email");
			msg.setText("Nobody");

			Transport.send(msg);

			resp.getWriter().println("<p>email sent!</p>");

		} catch (Exception e) {
			System.err.println("There was an error: " + e.toString());
		}
	}
}
