package nl.sense_os.commonsense.client.mvc.views;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

import nl.sense_os.commonsense.client.components.NavPanel;
import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.MainEvents;
import nl.sense_os.commonsense.client.mvc.events.NavEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.UserModel;

public class NavView extends View implements ValueChangeHandler<String> {

    private static final String TAG = "NavView";
    private NavPanel navPanel = new NavPanel();

    public NavView(Controller controller) {
        super(controller);
    }
    
    @Override
    protected void handleEvent(AppEvent event) {
        EventType eventType = event.getType();
        if (eventType.equals(MainEvents.Init)) {
            onInit(event);
        } else if (eventType.equals(MainEvents.UiReady)) {
            onUiReady(event);
        } else if (eventType.equals(NavEvents.NavChanged)) {
            onNavChanged(event);
        } else if (eventType.equals(LoginEvents.LoggedIn)) {
            onLoggedIn(event);
        } else if (eventType.equals(LoginEvents.LoggedOut)) {
            onLoggedOut(event);
        } else {
            Log.e(TAG, "Unexpected event type: " + eventType);
        }
    }
    
    private void onUiReady(AppEvent event) {
        Log.d(TAG, "onUiReady");
        
        // supply initial History token
        String token = History.getToken();
        boolean needsLogin = (token.equals(NavPanel.SIGN_IN) || token.equals(NavPanel.SETTINGS)
                || token.equals(NavPanel.SHARE_DATA) || token.equals(NavPanel.TRAINING_DATA) || token
                .equals(NavPanel.VISUALIZATION));        
        String startLocation = needsLogin ? NavPanel.SIGN_IN : NavPanel.HOME;
        History.newItem(startLocation);
        History.fireCurrentHistoryState();
    }

    private void onLoggedOut(AppEvent event) {
        Log.d(TAG, "onLoggedOut");
        
        this.navPanel.setLoggedIn(false);
    }

    private void onLoggedIn(AppEvent event) {
        Log.d(TAG, "onLoggedIn");
        
        final UserModel user = event.<UserModel> getData();
        
        this.navPanel.setUser(user);
        this.navPanel.setLoggedIn(true);
    }

    private void onNavChanged(AppEvent event) {
        Log.d(TAG, "onNavChanged");
        
        String location = event.<String> getData();
        this.navPanel.setHighlight(location);
    }

    private void onInit(AppEvent event) {
        Log.d(TAG, "onInit");
        
        History.addValueChangeHandler(this);
        Dispatcher.forwardEvent(new AppEvent(NavEvents.NavReady, this.navPanel));
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        Log.d(TAG, "onValueChange");
        
        String token = event.getValue();
        Dispatcher.forwardEvent(NavEvents.NavChanged, token);
    }
}
