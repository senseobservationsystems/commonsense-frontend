package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.UserModel;

public class CommonSense implements EntryPoint {

    private static final String TAG = "CommonSense";
    
    public void onModuleLoad() {

        // make the root panel as big as the browser view
        RootPanel.get().setLayoutData(new FitLayout());
        
        setLoginScreen();
    }

    private void setHomeScreen(UserModel userModel) {
        Log.d(TAG, "Show home screen...");

        AsyncCallback<Void> callback = new AsyncCallback<Void>() {            
            public void onFailure(Throwable ex) {
                // do nothing
            }     
            
            public void onSuccess(Void result) {
                // logged out from the home screen, login form again
                setLoginScreen();
            }  
        };
        Home home = new Home(userModel, callback);
        
        // set up viewport to fill entire browser screen
        Viewport vp = new Viewport();
        vp.setLayout(new FitLayout());
        vp.add(home);
        
        RootPanel.get().clear();
        RootPanel.get().add(vp);
    }

    private void setLoginScreen() {
        Log.d(TAG,"Show login screen...");  

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
        Viewport vp = new Viewport();
        vp.setLayout(new FitLayout());
        vp.add(login);
        
        RootPanel.get().clear();
        RootPanel.get().add(vp);
    }
}
