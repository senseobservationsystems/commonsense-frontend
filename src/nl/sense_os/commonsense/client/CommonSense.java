package nl.sense_os.commonsense.client;

import nl.sense_os.commonsense.client.screen.HomeScreen;
import nl.sense_os.commonsense.client.screen.LoginScreen;
import nl.sense_os.commonsense.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CommonSense implements EntryPoint {

	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private final DataServiceAsync dataService = GWT
			.create(DataService.class);

	private static CommonSense singleton;

	public static CommonSense get() {
		return singleton;
	}

  public void onModuleLoad() {
	  singleton=this;
	  setLoginScreen();
  }

  private void setLoginScreen()	{
	  LoginScreen scrLogin=new LoginScreen();
	  RootPanel.get().clear();
	  RootPanel.get().add(scrLogin);

  }

  public void setHomeScreen(User user)	{
	  HomeScreen homeScreen=new HomeScreen(user);
	  RootPanel.get().clear();
	  RootPanel.get().add(homeScreen);
  }

}
