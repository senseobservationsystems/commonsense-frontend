package nl.sense_os.commonsense.client.components;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.UserModel;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * Component with the top navigation bar. Highlights the current location and shows the current user
 * when logged in.
 */
public class NavPanel extends LayoutContainer {

    public static final String BUILDING_MGMT = "building_mgmt";
    public static final String HELP = "help";
    public static final String HOME = "home";
    public static final String SETTINGS = "settings";
    public static final String SHARE_DATA = "share";
    public static final String SIGN_IN = "signin";
    public static final String SIGN_OUT = "signout";
    private static final String TAG = "NavPanel";
    public static final String TRAINING_DATA = "train";
    public static final String VISUALIZATION = "viz";
    private final Hyperlink buildingMgmt = new Hyperlink("building management", BUILDING_MGMT);
    private Hyperlink current;
    private final Hyperlink help = new Hyperlink("help", HELP);
    private final Hyperlink home = new Hyperlink("home", HOME);
    private boolean isLoggedIn = false;
    private final Hyperlink login = new Hyperlink("sign in", SIGN_IN);
    private final Hyperlink logout = new Hyperlink("sign out", SIGN_OUT);
    private final Margins margins = new Margins(0, 7, 0, 7);
    private final Hyperlink settings = new Hyperlink("settings", SETTINGS);
    private final Hyperlink share = new Hyperlink("share data", SHARE_DATA);
    private final Text stretch = new Text();
    private final Hyperlink train = new Hyperlink("training data", TRAINING_DATA);
    private final Text userName = new Text();
    private final Hyperlink viz = new Hyperlink("visualizations", VISUALIZATION);

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        this.userName.setStyleAttribute("font-weight", "bold");
        this.userName.setStyleAttribute("text-align", "right");

        // container for the horizontal box layout
        final HBoxLayout boxLayout = new HBoxLayout();
        boxLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        setLayout(boxLayout);

        relayout();
    }

    /**
     * Updates the layout to display only the public or also the private parts of the application.
     */
    private void relayout() {

        removeAll();

        final HBoxLayoutData boxData = new HBoxLayoutData(this.margins);
        final HBoxLayoutData flexData = new HBoxLayoutData(this.margins);
        flexData.setFlex(1);

        if (this.isLoggedIn) {
            add(this.home, boxData);
            add(this.viz, boxData);
            add(this.buildingMgmt, boxData);
            add(this.share, boxData);
            add(this.train, boxData);
            add(this.stretch, flexData);
            add(this.userName, boxData);
            add(this.settings, boxData);
            add(this.help, boxData);
            add(this.logout, boxData);
        } else {
            add(this.home, boxData);
            add(this.stretch, flexData);
            add(this.help, boxData);
            add(this.login, boxData);
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
        if (null != this.current) {
            this.current.setStyleName("sense-nav-deselected");
        }

        // set new navigation label selected
        if (highlight.equals(HOME)) {
            this.current = this.home;
        } else if (highlight.equals(VISUALIZATION)) {
            this.current = this.viz;
        } else if (highlight.equals(SHARE_DATA)) {
            this.current = this.share;
        } else if (highlight.equals(TRAINING_DATA)) {
            this.current = this.train;
        } else if (highlight.equals(SETTINGS)) {
            this.current = this.settings;
        } else if (highlight.equals(HELP)) {
            this.current = this.help;
        } else if (highlight.equals(SIGN_OUT)) {
            this.current = this.logout;
        } else if (highlight.equals(SIGN_IN)) {
            this.current = this.login;
        } else if (highlight.equals(BUILDING_MGMT)) {
            this.current = this.buildingMgmt;
        }
        this.current.setStyleName("sense-nav-selected");

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