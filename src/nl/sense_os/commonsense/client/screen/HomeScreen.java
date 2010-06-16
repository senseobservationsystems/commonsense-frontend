package nl.sense_os.commonsense.client.screen;

import nl.sense_os.commonsense.client.User;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HomeScreen extends Composite{
	
	public HomeScreen(User user)	{
		VerticalPanel vp=new VerticalPanel();
		Label lblWelcome=new Label();
		lblWelcome.setText("Hello "+user.getUserName());
		vp.add(lblWelcome);
		initWidget(vp);
	}

}
