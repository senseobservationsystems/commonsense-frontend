package nl.sense_os.commonsense.client.screen;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.MD5Wrapper;
import nl.sense_os.commonsense.client.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@SuppressWarnings("unchecked")
public class LoginScreen extends Composite {
	
	DataServiceAsync svc = (DataServiceAsync) GWT.create(DataService.class);

	private TextBox txtLogin=new TextBox();
	private PasswordTextBox txtPassword=new PasswordTextBox();
	private Label lblError=new Label();
	
	public LoginScreen(final AsyncCallback callback)	{
		Grid grid = new Grid(4, 2);
		grid.setWidget(0,1, lblError);
		grid.setWidget(1,0, new Label("Username"));
		grid.setWidget(1,1, txtLogin);
		grid.setWidget(2,0, new Label("Password"));
		grid.setWidget(2,1, txtPassword);
		Button btnLogin=new Button("login");
		grid.setWidget(3,1, btnLogin );

		btnLogin.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event)	{
				checkLogin(txtLogin.getText(),MD5Wrapper.toMD5(txtPassword.getText()), callback);
			}			
		});
		initWidget(grid);
	}
	
	private void checkLogin(String userName,String password, final AsyncCallback mainCallback) {

		AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                User user = (User) result;
                if (user != null) {
                	setErrorText("");
                	mainCallback.onSuccess(user);
                } else {
                    setErrorText("Invalid UserName or Password");                    
                }
            }
            public void onFailure(Throwable ex) {
                setErrorText("Error: "+ex.getMessage());
            }
        };
        svc.checkLogin(userName, password, callback);
    }

	private void setErrorText(String errorMessage)	{
		lblError.setText(errorMessage);
	}
}
