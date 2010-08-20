package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;

public class WelcomeTab extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "WelcomeTab";
    private final String username;

    public WelcomeTab(String username) {
        this.username = username;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);         

        Html h = new Html(
                "<div class=text style='width:600px; margin-left:auto; margin-right:auto; text-align:left; padding:10px;'>"
                + "<h1>Welcome to CommonSense, " + this.username + "! </h1>"
                + "<br><br>"
                + "<div style='text-align:center;'><img src='/img/logo_sense-800.png' alt='Sense logo'/></div>"
//                + "<br><br>"
//                + "<h2>Notice: Performance Issues</h2>"
//                + "<hr>"
//                + "<br>"
//                + "<p>CommonSense's database is suffering from performance problems, which results in problems requesting sensor data for visualization. "
//                + "Please hang in there, we are working vigorously to address the issue.</p>"
                + "<br><br>"
                + "<h2>Frequently Asked Questions</h2>"
                + "<hr>"
                + "<br>"
                + "<p><i>What is CommonSense?</i></p>"
                + "<p>CommonSense is the Sense dashboard, where you can see and manage your data. "
                + "CommonSense is still under development, so keep checking back for new features!</p>"
                + "<br>"
                + "<p><i>How do I get to see my data?</i></p>"
                + "<p>Drag one or more sensors from the left panel to this main panel. "
                + "You will get a visualization of the data from that sensor or group of sensors. "
                + "Use the time range selector on the bottom left to request data from longer or shorter periods of time.</p>"
                + "<br>"
                + "<p><i>Are you spying on me?</i></p>"
                + "<p>No. Sense is not gathering data for its own use, you are gathering data for your own use. "
                + "Your data is only accessible to you and to anybody you <i>explicitly</i> shared information with. "
                + "Sharing is not even possible yet at this moment, so your data really only belongs to you.</p>"
                + "<br><br>"
                + "<h2>Known Issues</h2>"
                + "<hr>"
                + "<br>"
                + "<ol style='list-style-type:disc;'>"
                + "<li>It is only possible to visualize data with numbers in it. "
                + "This means that non-numerical data or data on neighboring devices is not visible yet.</li>"
                + "<li>CommonSense does not know whether the Sense App is running or not. "
                + "It simply draws a line between data points, even when they are days or weeks apart.</li>"
                + "<li>Requesting sensor data can take a long time. "
                + "Please keep in mind that it simply is not easy to retrieve and display large amounts of data in your browser :-).</li>"
                + "<li>Requesting too much sensor data at once generates a server error. "
                + "This is due to a restriction in the Google App Engine, we are working our way around it.</li>"
                + "<li>The login form is not behaving as we like it. "
                + "Password managers do not recognize it, and you cannot press Enter to submit the form.</li>"
                + "</ol>"
                + "<br>"
                + "<p>Report more bugs or suggestions to <a href='mailto:info@sense-os.nl'>info@sense-os.nl</a>."
                + "</div>");
        
        this.setLayout(new FitLayout());
        this.setScrollMode(Scroll.AUTOY);
        this.add(h);
//        this.add(new Frame("http://www.sense-os.nl"));
    }
}
