package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;

import nl.sense_os.commonsense.client.services.BuildingService;
import nl.sense_os.commonsense.client.services.BuildingServiceAsync;
import nl.sense_os.commonsense.client.services.TagService;
import nl.sense_os.commonsense.client.services.TagServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.widgets.Login;
import nl.sense_os.commonsense.client.widgets.NavBar;
import nl.sense_os.commonsense.client.widgets.Visualization;
import nl.sense_os.commonsense.client.widgets.building.BuildingMgmt;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;

/**
 * Entry point for the CommonSense web application. Holds the background and controls the navigation
 * to other parts of the application.
 */
public class CommonSense implements EntryPoint, ValueChangeHandler<String> {

    private static final String TAG = "CommonSense";    
    private LayoutContainer mainBorderLayout;
    private final NavBar topNavBar = new NavBar();
    
    /**
     * History token that we want to navigate to after signing in (default is NavBar.HOME).  
     */
    private String afterSignIn = NavBar.HOME;

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
        // save in local Registry
        Registry.register(Constants.REG_USER, user);

        topNavBar.setUser(user);
        topNavBar.setLogin(true);

        // continue to next screen
        History.newItem(afterSignIn);
    }

    @Override
    public void onModuleLoad() {
        String now = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(new Date());
        Log.d(TAG, "===== Module Load (" + now + ") =====");

        // set up main view
        Viewport vp = new Viewport();
        vp.setSize("100%", "100%");
        vp.setLayout(new FitLayout());
        vp.add(createMainPanel());
        RootPanel.get().add(vp);

        // load services and put them in Registry
        final TagServiceAsync sensorDataService = GWT.create(TagService.class);
        Registry.register(Constants.REG_TAG_SVC, sensorDataService);
        final BuildingServiceAsync buildingService = GWT.create(BuildingService.class);
        Registry.register(Constants.REG_BUILDING_SVC, buildingService);

        // check for initial History token
        String token = History.getToken();
        boolean needsLogin = (token.equals(NavBar.SIGN_IN) || token.equals(NavBar.SETTINGS)
                || token.equals(NavBar.SHARE_DATA) || token.equals(NavBar.TRAINING_DATA) || token
                .equals(NavBar.VISUALIZATION));
        if (true == needsLogin) {
            // save the token so we can navigate to it after signing in
            afterSignIn = token;
            if (token.equals(NavBar.SIGN_IN)) {
                afterSignIn = NavBar.HOME;
            }
            
            History.newItem(NavBar.SIGN_IN);
        } else {
            History.newItem(NavBar.HOME);
        }
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
        Widget w = new Text("no content");
        if (token.equals(NavBar.HOME)) {
            w = showHomeScreen();
        } else if (token.equals(NavBar.SIGN_IN)) {
            w = showLoginScreen();
        } else if (token.equals(NavBar.VISUALIZATION)) {
            w = showVizScreen();
        } else if (token.equals(NavBar.SIGN_OUT)) {
            w = showLoginScreen();
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
        return new BuildingMgmt();
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
                // should never happen
                Log.e(TAG, "Login returned onFailure?! Message: " + ex.getMessage());
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
        return new Visualization();
    }
}
