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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Component with the top navigation bar. Highlights the current location and shows the current user
 * when logged in.
 */
public class NavPanel extends LayoutContainer {

    private static final Logger LOG = Logger.getLogger(NavPanel.class.getName());
    private static final String REGISTER = "register";
    private static final String DASHBOARD = "dashboard";
    public static final String HELP = "help";
    public static final String HOME = "home";
    public static final String SIGN_OUT = "signout";
    private static final String ACCOUNT = "account";
    public static final String VISUALIZATION = "sensors";
    public static final String RESET_PASSWORD = "resetPassword";
    public static final int HEIGHT = 30;
    private boolean isLoggedIn = false;
    private final Text userName = new Text();
    private Widget current;
    private final Hyperlink help = new Hyperlink("help", HELP);
    private final Hyperlink home = new Hyperlink("login", HOME);
    private final Hyperlink logout = new Hyperlink("sign out", SIGN_OUT);
    private final Hyperlink viz = new Hyperlink("my sensors", VISUALIZATION);

    private final HTMLPanel register = new HTMLPanel("");
    private final HTMLPanel dashboard = new HTMLPanel("");
    private final HTMLPanel account = new HTMLPanel("");

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

	register.add(new Anchor("register", "http://accounts.sense-os.nl", "_blank"));
	dashboard.add(new Anchor("my dashboard", "http://my.sense-os.nl", "_blank"));
	account.add(new Anchor("my account", "http://accounts.sense-os.nl", "_blank"));

	home.setStyleName("sense-nav-item");
	register.setStyleName("sense-nav-item");
	viz.setStyleName("sense-nav-item");
	dashboard.setStyleName("sense-nav-item");
	account.setStyleName("sense-nav-item");
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
	    add(viz, new RowData(85.0, 1, rightMargin));
	    add(dashboard, new RowData(95.0, 1, rightMargin));
	    add(account, new RowData(85.0, 1, rightMargin));
	    add(help, new RowData(50.0, 1, rightMargin));
	    add(userName, new RowData(1, 1, rightMargin));
	    add(logout, new RowData(66.6, 1, rightMargin));
	    add(endItem, new RowData(16, 1, noMargins));
	} else {
	    add(logo, new RowData(-1, -1, rightMargin));
	    add(home, new RowData(50.0, 1, rightMargin));
	    add(register, new RowData(60.0, 1, rightMargin));
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
	} else if (highlight.equals(REGISTER)) {
	    current = register;
	} else if (highlight.equals(DASHBOARD)) {
	    current = dashboard;
	} else if (highlight.equals(ACCOUNT)) {
	    current = account;
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
