package nl.sense_os.commonsense.client.main.components;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * Component with the top navigation bar. Highlights the current location and shows the current user
 * when logged in.
 */
public class NavPanel extends LayoutContainer {

    private static final String TAG = "NavPanel";
    public static final String HELP = "help";
    public static final String HOME = "home";
    public static final String SETTINGS = "settings";
    public static final String SIGN_IN = "signin";
    public static final String SIGN_OUT = "signout";
    public static final String VISUALIZATION = "viz";
    private boolean isLoggedIn = false;
    private final Text stretch = new Text();
    private final Text userName = new Text();
    private Hyperlink current;
    private final Hyperlink help = new Hyperlink("help", HELP);
    private final Hyperlink home = new Hyperlink("home", HOME);
    private final Hyperlink login = new Hyperlink("sign in", SIGN_IN);
    private final Hyperlink logout = new Hyperlink("sign out", SIGN_OUT);
    private final Hyperlink settings = new Hyperlink("settings", SETTINGS);
    private final Hyperlink viz = new Hyperlink("visualizations", VISUALIZATION);

    public NavPanel() {

        this.setLayout(new RowLayout(Orientation.HORIZONTAL));
        this.setLayoutOnChange(true);

        initLinks();
        initUsername();
    }

    private void initLinks() {
        this.help.setStylePrimaryName("x-sense-nav-deselected");
        this.help.setWidth("auto");
        this.home.setStylePrimaryName("x-sense-nav-deselected");
        this.home.setWidth("auto");
        this.login.setStylePrimaryName("x-sense-nav-deselected");
        this.login.setWidth("auto");
        this.logout.setStylePrimaryName("x-sense-nav-deselected");
        this.logout.setWidth("auto");
        this.settings.setStylePrimaryName("x-sense-nav-deselected");
        this.settings.setWidth("auto");
        this.viz.setStylePrimaryName("x-sense-nav-deselected");
        this.viz.setWidth("auto");
    }

    private void initUsername() {
        this.userName.setStyleAttribute("font-weight", "bold");
        this.userName.setStyleAttribute("font-size", "13px");
        this.userName.setStyleAttribute("color", "black");
        this.userName.setStyleAttribute("text-align", "right");

        this.userName.setAutoWidth(true);
    }

    /**
     * Updates the layout to display only the public or also the private parts of the application.
     */
    private void relayout() {

        final int vMargin = 2;
        final int hMargin = 5;
        RowData startData = new RowData(-1, 1, new Margins(vMargin, hMargin, vMargin, 2 * hMargin));
        RowData endData = new RowData(-1, 1, new Margins(vMargin, 2 * hMargin, vMargin, hMargin));
        RowData itemData = new RowData(-1, 1, new Margins(vMargin, hMargin, vMargin, hMargin));
        RowData stretchData = new RowData(1, 1, new Margins(0));

        removeAll();
        if (this.isLoggedIn) {
            this.add(this.home, startData);
            this.add(this.viz, itemData);
            this.add(this.stretch, stretchData);
            this.add(this.userName, itemData);
            this.add(this.settings, itemData);
            this.add(this.help, itemData);
            this.add(this.logout, endData);
        } else {
            this.add(this.home, startData);
            this.add(this.stretch, stretchData);
            this.add(this.help, itemData);
            this.add(this.login, endData);
        }
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
            this.current.setStylePrimaryName("x-sense-nav-deselected");
            this.current.setWidth("auto");
        }

        // set new navigation label selected
        if (highlight.equals(HOME)) {
            this.current = this.home;
        } else if (highlight.equals(VISUALIZATION)) {
            this.current = this.viz;
        } else if (highlight.equals(SETTINGS)) {
            this.current = this.settings;
        } else if (highlight.equals(HELP)) {
            this.current = this.help;
        } else if (highlight.equals(SIGN_OUT)) {
            this.current = this.logout;
        } else if (highlight.equals(SIGN_IN)) {
            this.current = this.login;
        } else {
            Log.w(TAG, "Unexpected highlight: " + highlight);
        }
        this.current.setStylePrimaryName("x-sense-nav-selected");
        this.current.setWidth("auto");

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
            Log.e(TAG, "Something is wrong: user=null");
            this.userName.setText("NULL");
        }
    }
}
