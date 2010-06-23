package nl.sense_os.commonsense.client;

import nl.sense_os.commonsense.data.User;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class CommonSense implements EntryPoint {

  public void onModuleLoad() {
	  setLoginScreen();
  }

  @SuppressWarnings("unchecked")
  private void setLoginScreen()	{
	  LoginScreen scrLogin=new LoginScreen(new AsyncCallback() {
          public void onSuccess(Object result) {
        	  setHomeScreen((User) result);
          }
          public void onFailure(Throwable ex) {
          }
      });
	  RootPanel.get().clear();
	  RootPanel.get().add(scrLogin);
  }

  @SuppressWarnings("unchecked")
  private void setHomeScreen(User user)	{
	  HomeScreen homeScreen=new HomeScreen(user, new AsyncCallback() {
          public void onSuccess(Object result) {
        	  setLoginScreen();
          }
          public void onFailure(Throwable ex) {
          }
      });
	  RootPanel.get().clear();
	  RootPanel.get().add(homeScreen);
  }

}
