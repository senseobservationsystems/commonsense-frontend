package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;

import nl.sense_os.commonsense.client.services.DataService;
import nl.sense_os.commonsense.client.services.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.NavBar;
import nl.sense_os.commonsense.client.widgets.building.BuildingMgmt;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.dto.exceptions.DbConnectionException;
import nl.sense_os.commonsense.dto.exceptions.WrongResponseException;

/**
 * Entry point for the CommonSense web application. Holds the background and controls the navigation
 * to other parts of the application.
 */
public class CommonSense implements EntryPoint, ValueChangeHandler<String> {

    private static final String TAG = "CommonSense";
    private LayoutContainer mainBorderLayout;
    private final NavBar topNavBar = new NavBar();
    private UserModel user;

    private LayoutContainer createMainPanel() {

        // layouts for the different panels
        final BorderLayoutData northLayout = new BorderLayoutData(LayoutRegion.NORTH, 25);
        northLayout.setMargins(new Margins(0));
        northLayout.setSplit(false);

        // main container with top and center separation
        mainBorderLayout = new LayoutContainer(new BorderLayout());
        mainBorderLayout.setStyleAttribute("backgroundColor", "transparent");
        mainBorderLayout.setStyleAttribute("border-width", "0px");
        mainBorderLayout.add(topNavBar, northLayout);

        /* NB: there are two containers to handle the background! */

        // Inner container with top right background
        LayoutContainer innerContainer = new LayoutContainer(new FitLayout());
        innerContainer.setStyleAttribute("background",
                "url('img/bg/right_top_pre-light.png') no-repeat top right;");
        innerContainer.add(mainBorderLayout);

        // Outer container with bottom left background
        LayoutContainer outerContainer = new LayoutContainer(new FitLayout());
        outerContainer.setStyleAttribute("background",
                "url('img/bg/left_bot_pre-light.png') no-repeat bottom left;");
        outerContainer.setStyleAttribute("backgroundColor", "white");
        outerContainer.add(innerContainer, new FlowData(0));

        return outerContainer;
    }

    private void onLogin(UserModel user) {
        this.user = user;

        topNavBar.setUser(user);
        topNavBar.setLogin(true);

        // continue to visualization screen
        History.newItem(NavBar.VISUALIZATION);
    }

    private void onLogout() {
        this.user = null;
        Cookies.removeCookie("user_name");
        Cookies.removeCookie("user_pass");

        topNavBar.setUser(null);
        topNavBar.setLogin(false);
        mainBorderLayout.layout();

        // continue to home screen
        History.newItem(NavBar.HOME);
    }

    @Override
    public void onModuleLoad() {
        String now = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(new Date());
        Log.d(TAG, "===== Module Load (" + now + ") =====");

        // check for initial token
        String token = History.getToken();
        if ((token.length() == 0)
                || (!(token.equals(NavBar.HOME) || token.equals(NavBar.HELP) || token
                        .equals(NavBar.SIGN_IN)))) {
            History.newItem("home");
        }

        tryAutoSignIn();

        Viewport vp = new Viewport();
        vp.setSize("100%", "100%");
        vp.setLayout(new FitLayout());
        vp.add(createMainPanel());

        RootPanel root = RootPanel.get();
        root.add(vp);

        History.addValueChangeHandler(this);
        History.fireCurrentHistoryState();
    }

    /**
     * Changes the component in the content panel when a History Event occurs. Usually these are
     * generated by the navigation links in the top panel.
     * 
     * @param event
     *            the event to respond to, containing the history token String
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        final String token = event.getValue();

        // remove the component that is currently displayed in the content panel
        if (mainBorderLayout.getItemCount() > 1) {
            mainBorderLayout.remove(mainBorderLayout.getItem(1));
        }

        final BorderLayoutData centerLayout = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayout.setMargins(new Margins(0));

        // create the right widget for this history token
        Widget w = new Text("");
        if (token.equals(NavBar.HOME)) {
            w = showHomeScreen();
        } else if (token.equals(NavBar.SIGN_IN)) {
            w = showLoginScreen();
        } else if (token.equals(NavBar.VISUALIZATION)) {
            w = showVizScreen();
        } else if (token.equals(NavBar.SIGN_OUT)) {
            onLogout();
            return;
        } else if (token.equals(NavBar.HELP)) {
            w = showHelpScreen();
        } else if (token.equals(NavBar.BUILDING_MGMT)) {
            w = showBuildingMgmt();
        } else {
            LayoutContainer lc = new LayoutContainer(new CenterLayout());
            lc.add(new Text("Under construction..."));
            w = lc;
        }
        mainBorderLayout.add(w, centerLayout);
        mainBorderLayout.layout();
    }

    private Widget showHomeScreen() {
        final Frame welcomeFrame = new Frame("http://welcome.sense-os.nl");
        welcomeFrame.setStylePrimaryName("senseFrame");

        return welcomeFrame;
    }

    private Widget showBuildingMgmt() {
        final LayoutContainer imgUpload = new BuildingMgmt("" + user.getId());
        return imgUpload;
    }

    private Widget showHelpScreen() {
        final Frame helpFrame = new Frame("http://welcome.sense-os.nl/node/6");
        helpFrame.setStylePrimaryName("senseFrame");

        return helpFrame;
    }

    private Widget showLoginScreen() {

        // create login form widget
        AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
            @Override
            public void onFailure(Throwable ex) {
                onLogout();
            }

            @Override
            public void onSuccess(UserModel result) {
                onLogin(result);
            }
        };
        Login login = new Login(callback);

        return login;
    }

    private Widget showVizScreen() {
        return new Visualization(this.user);
    }

    private void tryAutoSignIn() {
        // get user from Cookie
        String cookieName = Cookies.getCookie("user_name");
        String cookiePass = Cookies.getCookie("user_pass");
        if ((null != cookieName) && (null != cookiePass) && (cookieName.length() > 0)
                && (cookiePass.length() > 0)) {
            Log.d(TAG, "Autologin");

            // show progress dialog
            final MessageBox waitBox = MessageBox.wait("CommonSense Login",
                    "Logging in, please wait...", "Logging in...");

            // perform logout when the login fails
            final Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>() {

                @Override
                public void handleEvent(MessageBoxEvent be) {
                    onLogout();
                }
            };

            final AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
                @Override
                public void onFailure(Throwable ex) {
                    waitBox.close();

                    String title = "Login failure!";
                    if (ex instanceof WrongResponseException) {
                        MessageBox.alert(title, "Invalid username or password.", l);
                    } else if (ex instanceof DbConnectionException) {
                        MessageBox.alert(title, "Failed to connect to CommonSense database.", l);
                    } else {
                        MessageBox.alert(title, "Server-side failure: " + ex.getMessage(), l);
                    }
                }

                @Override
                public void onSuccess(UserModel user) {
                    waitBox.close();
                    if (user != null) {
                        onLogin(user);
                    } else {
                        MessageBox.alert("Login failure!", "Invalid username or password.", l);
                    }
                }
            };
            final DataServiceAsync service = (DataServiceAsync) GWT.create(DataService.class);
            service.checkLogin(cookieName, cookiePass, callback);
        }
    }
}
