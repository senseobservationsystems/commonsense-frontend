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
    public static final String VISUALIZATION = "viz";
    public static final int HEIGHT = 30;
    private boolean isLoggedIn = false;
    private final Text userName = new Text();
    private Widget current;
    private final Hyperlink demo = new Hyperlink("demos", DEMO);
    private final Hyperlink help = new Hyperlink("help", HELP);
    private final Hyperlink home = new Hyperlink("home", HOME);
    private final Hyperlink logout = new Hyperlink("sign out", SIGN_OUT);
    private final Hyperlink settings = new Hyperlink("settings", SETTINGS);
    private final Hyperlink viz = new Hyperlink("visualizations", VISUALIZATION);
    private final LayoutContainer spacer = new LayoutContainer();
    private Image logo;

    public NavPanel() {

        this.setLayout(new RowLayout(Orientation.HORIZONTAL));
        this.setId("sense-nav-bar");

        initLogo();
        initLinks();
        initUsername();
    }

    private void initLogo() {
        if (Constants.DEV_MODE) {
            logo = new Image(GWT.getHostPageBaseURL() + "img/logo_dev-header.png");
        } else if (Constants.TEST_MODE) {
            logo = new Image(GWT.getHostPageBaseURL() + "img/logo_test-header.png");
        } else {
            logo = new Image(GWT.getHostPageBaseURL() + "img/logo_sense-header.png");
        }
        logo.setPixelSize(100, 30);
        logo.setStyleName("sense-header-logo");
    }

    private void initLinks() {

        this.home.setStyleName("sense-nav-item");
        this.home.setSize("40px", "25px");

        this.viz.setStyleName("sense-nav-item");
        this.viz.setSize("90px", "25px");

        this.demo.setStyleName("sense-nav-item");
        this.demo.setSize("40px", "25px");

        this.settings.setStyleName("sense-nav-item");
        this.settings.setSize("50px", "25px");

        this.help.setStyleName("sense-nav-item");
        this.help.setSize("40px", "25px");

        this.logout.setStyleName("sense-nav-item");
        this.logout.setSize("50px", "25px");

        this.spacer.setStyleName("sense-header-spacer");
    }

    private void initUsername() {
        this.userName.setStyleName("sense-header-username");
    }

    /**
     * Updates the layout to display only the public or also the private parts of the application.
     */
    private void relayout() {

        LayoutContainer endItem = new LayoutContainer();
        endItem.setStyleName("sense-header-spacer");

        RowData endItemData = new RowData(16, 1, new Margins(0, 0, 0, 4));
        RowData logoData = new RowData(-1, -1, new Margins(0, -2, 0, 0));
        RowData itemData = new RowData(-1, 1, new Margins(0, 2, 0, 4));
        RowData usernameData = new RowData(1, 1, new Margins(0, -2, 0, 2));
        RowData spacerData = new RowData(1, 1, new Margins(0, 0, 0, 4));

        removeAll();
        if (this.isLoggedIn) {
            this.add(logo, logoData);
            this.add(this.viz, itemData);
            this.add(this.demo, itemData);
            this.add(this.settings, itemData);
            this.add(this.help, itemData);
            this.add(userName, usernameData);
            this.add(this.logout, itemData);
            this.add(endItem, endItemData);
        } else {
            this.add(logo, logoData);
            this.add(this.home, itemData);
            this.add(this.demo, itemData);
            this.add(this.help, itemData);
            this.add(this.spacer, spacerData);
        }

        this.layout();
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
        if (null != this.current) {
            this.current.removeStyleName("sense-nav-item-selected");
        }

        // set new navigation label selected
        if (highlight.equals(HOME)) {
            this.current = this.home;
        } else if (highlight.equals(VISUALIZATION)) {
            this.current = this.viz;
        } else if (highlight.equals(DEMO)) {
            this.current = this.demo;
        } else if (highlight.equals(SETTINGS)) {
            this.current = this.settings;
        } else if (highlight.equals(HELP)) {
            this.current = this.help;
        } else if (highlight.equals(SIGN_OUT)) {
            this.current = this.logout;
        } else {
            LOG.warning("Unexpected highlight: " + highlight);
        }
        this.current.addStyleName("sense-nav-item-selected");

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
            this.userName.setText(user.toString());
        } else {
            // should never be visible
            LOG.severe("Something is wrong: user=null");
            this.userName.setText("NULL");
        }
    }
}
