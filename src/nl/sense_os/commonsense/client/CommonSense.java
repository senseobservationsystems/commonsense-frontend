package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import nl.sense_os.commonsense.data.User;

public class CommonSense implements EntryPoint {

    public void onModuleLoad() {
        setLoginScreen();
    }

    private void setHomeScreen(User user) {
        System.out.println("Show home screen...");
        
        RootPanel.get().clear();

        AsyncCallback<Void> callback = new AsyncCallback<Void>() {            
            public void onFailure(Throwable ex) {
                // do nothing
            }     
            
            public void onSuccess(Void result) {
                // logged out from the home screen, login form again
                setLoginScreen();
            }  
        };
        HomeContainer home = new HomeContainer(user, callback);
        
        // set up viewport
        Viewport vp = new Viewport();
        vp.setLayout(new FitLayout());
        vp.add(home);
        RootPanel.get().add(vp);
        vp.layout(true);
    }

    private void setLoginScreen() {
        System.out.println("Show login screen...");
        RootPanel.get().clear();

        // create login form widget
        AsyncCallback<User> callback = new AsyncCallback<User>() {
            public void onFailure(Throwable ex) {
                // do nothing
            }

            public void onSuccess(User result) {
                // logged in successfully, continue to home screen
                setHomeScreen(result);
            }
        };
        LoginForm login = new LoginForm(callback);
        
        // set up viewport
        Viewport vp = new Viewport();
        vp.setLayout(new FitLayout());
        vp.add(login);
        RootPanel.get().add(vp);
        vp.layout(true);
    }
}
