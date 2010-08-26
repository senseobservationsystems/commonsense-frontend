package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.Date;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.UserModel;

public class CommonSense implements EntryPoint {

    private static final String TAG = "CommonSense";
    
    public void onModuleLoad() {
        Log.d(TAG, "========== Module Load ==========");
        
        // make the root panel as big as the browser view
        RootPanel.get().setLayoutData(new FitLayout());        
        
        setLoginScreen();
    }

    private void setHomeScreen(UserModel userModel) {        

        AsyncCallback<Void> callback = new AsyncCallback<Void>() {            
            public void onFailure(Throwable ex) {
                // do nothing
            }     
            
            public void onSuccess(Void result) {
                
                final long DURATION = 1000 * 60 * 60 * 24 * 14; // 2 weeks
                Date expires = new Date(System.currentTimeMillis() + DURATION);
                Cookies.setCookie("user_pass", "", expires, null, "/", false);
                
                // logged out from the home screen, login form again
                setLoginScreen();
            }  
        };
        Home home = new Home(userModel, callback);
        
        // set up viewport to fill entire browser screen
        Viewport vp = new Viewport();
        vp.setSize("100%", "100%");
        vp.setLayout(new FitLayout());
        vp.add(home);
        
        RootPanel.get().clear();
        RootPanel.get().add(vp);
    }

    private void setLoginScreen() {

        // create login form widget
        AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
            public void onFailure(Throwable ex) {
                // do nothing
            }

            public void onSuccess(UserModel result) {
                // logged in successfully, continue to home screen
                setHomeScreen(result);
            }
        };
        Login login = new Login(callback);
        
        // set up viewport
        final Viewport vp = new Viewport();
        vp.setLayout(new CenterLayout());
        vp.add(login);
        
        RootPanel.get().clear();
        RootPanel.get().add(vp);
    }
}
