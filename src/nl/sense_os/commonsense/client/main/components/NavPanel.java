package nl.sense_os.commonsense.client.main.components;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.UserModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Component with the top navigation bar. Highlights the current location and shows the current user
 * when logged in.
 */
public class NavPanel extends LayoutContainer {

    private static final Logger LOG = Logger.getLogger(NavPanel.class.getName());
    public static final String DEMO = "demo";
    public static final String HELP = "help";
    public static final String HOME = "home";
    public static final String SETTINGS = "settings";
    public static final String SIGN_OUT = "signout";
    public static final String VISUALIZATION = "sensors";
    public static final String RESET_PASSWORD = "resetPassword";
    public static final int HEIGHT = 30;
    private boolean isLoggedIn = false;
    private final Text userName = new Text();
    private Widget current;
    private final Hyperlink demo = new Hyperlink("demos", DEMO);
    private final Hyperlink help = new Hyperlink("help", HELP);
    private final Hyperlink home = new Hyperlink("home", HOME);
    private final Hyperlink logout = new Hyperlink("sign out", SIGN_OUT);
    private final Hyperlink settings = new Hyperlink("settings", SETTINGS);
    private final Hyperlink viz = new Hyperlink("my sensors", VISUALIZATION);
    private final LayoutContainer spacer = new LayoutContainer();
    private Image logo;

    public NavPanel() {
        setSize("800px", "30px");

        setLayout(new RowLayout(Orientation.HORIZONTAL));
        setId("sense-nav-bar");

        initLogo();
        initLinks();
        initUsername();
    }

    private void initLinks() {
        home.setStyleName("sense-nav-item");
        viz.setStyleName("sense-nav-item");
        demo.setStyleName("sense-nav-item");
        settings.setStyleName("sense-nav-item");
        help.setStyleName("sense-nav-item");
        logout.setStyleName("sense-nav-item");
        spacer.setStyleName("sense-header-spacer");
    }

    private void initLogo() {
        if (Constants.DEV_MODE) {
            logo = new Image(GWT.getHostPageBaseURL() + "img/logo_dev-header.png");
        } else if (Constants.RC_MODE) {
            logo = new Image(GWT.getHostPageBaseURL() + "img/logo_test-header.png");
        } else {
            logo = new Image(GWT.getHostPageBaseURL() + "img/logo_sense-header.png");
        }
        logo.setPixelSize(100, 30);
        logo.setStyleName("sense-header-logo");
    }

    private void initUsername() {
        userName.setStyleName("sense-header-username");
    }

    /**
     * Updates the layout to display only the public or also the private parts of the application.
     */
    private void relayout() {

        LayoutContainer endItem = new LayoutContainer();
        endItem.setStyleName("sense-header-spacer");

        // reusable margins
        Margins rightMargin = new Margins(0, 3, 0, 0);
        Margins noMargins = new Margins(0, 0, 0, 0);

        removeAll();
        if (isLoggedIn) {
            add(logo, new RowData(-1, -1, rightMargin));
            add(viz, new RowData(90.0, 1, rightMargin));
            add(demo, new RowData(50.0, 1, rightMargin));
            add(settings, new RowData(60.0, 1, rightMargin));
            add(help, new RowData(50.0, 1, rightMargin));
            add(userName, new RowData(1, 1, rightMargin));
            add(logout, new RowData(66.6, 1, rightMargin));
            add(endItem, new RowData(16, 1, noMargins));
        } else {
            add(logo, new RowData(-1, -1, rightMargin));
            add(home, new RowData(50.0, 1, rightMargin));
            add(demo, new RowData(50.0, 1, rightMargin));
            add(help, new RowData(50.0, 1, rightMargin));
            add(spacer, new RowData(1, 1, noMargins));
        }

        layout();
    }

    /**
     * Updates the layout when the user navigates to a different part of the application.
     * 
     * @param event
     *            ValueChangeEvent containing the new History token
     * @see History
     */
    public void setHighlight(String highlight) {

        // reset style of previously selected navigation label
        if (null != current) {
            current.removeStyleName("sense-nav-item-selected");
        }

        // set new navigation label selected
        if (highlight.equals(HOME)) {
            current = home;
        } else if (highlight.equals(VISUALIZATION)) {
            current = viz;
        } else if (highlight.equals(DEMO)) {
            current = demo;
        } else if (highlight.equals(SETTINGS)) {
            current = settings;
        } else if (highlight.equals(HELP)) {
            current = help;
        } else if (highlight.equals(SIGN_OUT)) {
            current = logout;
        } else if (highlight.equals(RESET_PASSWORD)) {
            // nothing to highlight
        } else {
            LOG.warning("Unexpected highlight: " + highlight);
        }

        if (null != current) {
            current.addStyleName("sense-nav-item-selected");
        }

        relayout();
    }

    /**
     * Shows or hides some navigation options based on login status
     */
    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        relayout();
    }

    /**
     * Changes the displayed user name
     */
    public void setUser(UserModel user) {
        if (null != user) {
            userName.setText(user.toString());
        } else {
            // should never be visible
            LOG.severe("Something is wrong: user=null");
            userName.setText("NULL");
        }
    }
}
