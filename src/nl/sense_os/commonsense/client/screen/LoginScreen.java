package nl.sense_os.commonsense.client.screen;

import nl.sense_os.commonsense.client.CommonSense;
import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.MD5Wrapper;
import nl.sense_os.commonsense.client.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginScreen extends Composite {
	// TextBox for the User Name
	private TextBox txtLogin=new TextBox();
	// PasswordTextBox for the password
	private PasswordTextBox txtPassword=new PasswordTextBox();
	//Error Label
	private Label lblError=new Label();
	
	public LoginScreen()	{
		// Lets add a grid to hold all our widgets
		Grid grid = new Grid(4, 2);
		//Set the error label
		grid.setWidget(0,1, lblError);
		//Add the Label for the username
		grid.setWidget(1,0, new Label("Username"));
		//Add the UserName textBox
		grid.setWidget(1,1, txtLogin);
		//Add the label for password
		grid.setWidget(2,0, new Label("Password"));
		//Add the password widget
		grid.setWidget(2,1, txtPassword);
		//Create a button
		Button btnLogin=new Button("login");
		//Add the Login button to the form
		grid.setWidget(3,1, btnLogin );
		/*Add a click listener which is called 
		when the button is clicked */
		btnLogin.addClickListener(new ClickListener(){
			public void onClick(Widget sender)	{
				checkLogin(txtLogin.getText(),MD5Wrapper.toMD5(MD5Wrapper.toMD5(txtPassword.getText())));
			}
			
		});
		initWidget(grid);

	}
	/*
	 * This method is called when the button is clicked
	 */
	
	private void checkLogin(String userName,String password) {
		System.out.println("Checking login for "+userName);

		/** 
		 * Async call to the server to check for login
		 */
		AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                User user = (User) result;
                if (user != null) {
                	setErrorText("");
                    // The user is authenticated, Set the home screen
                    CommonSense.get().setHomeScreen(user);
                } else {
                    setErrorText("Invalid UserName or Password");
                    
                }
            }

            public void onFailure(Throwable ex) {
                setErrorText("Error "+ex.getMessage());
                

            }
        };
        
        getService().checkLogin(userName, password,callback);

    }
	private void setErrorText(String errorMessage)	{
		lblError.setText(errorMessage);
	}
	
	private DataServiceAsync getService() {
		DataServiceAsync svc = (DataServiceAsync) GWT.create(DataService.class);
		ServiceDefTarget endpoint = (ServiceDefTarget) svc;
		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "data");
		return svc;
	}


}
