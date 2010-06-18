package nl.sense_os.commonsense.client.screen;

import java.util.Iterator;
import java.util.Set;

import nl.sense_os.commonsense.client.DataService;
import nl.sense_os.commonsense.client.DataServiceAsync;
import nl.sense_os.commonsense.client.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

@SuppressWarnings("unchecked")
public class HomeScreen extends Composite{

	DataServiceAsync svc = (DataServiceAsync) GWT.create(DataService.class);
	JSONObject phones = null;
	
	Grid mainGrid;
	Label lblMessage;
	ListBox phoneList;
	
	
	private void getPhoneDetails() {
		AsyncCallback callback = new AsyncCallback() {
			public void onSuccess(Object result) {
				JSONObject phoneDetails = (JSONObject) JSONParser.parse((String) result);
				phones = phoneDetails.get("phones").isObject();
				showPhoneDetails();
            }
            public void onFailure(Throwable ex) {
            	showPhoneDetailsFailure();
            }
    	};
    	svc.getPhoneDetails(callback);
	}

	private void showPhoneDetails() {
		int i = phones.size();
		if (i > 0) {
			lblMessage.setText("Found " + i + " registered phones.");
			Set keys = phones.keySet();
			Iterator ki = keys.iterator();
			while (ki.hasNext()) {
				String key = (String) ki.next();
				JSONObject phone = phones.get(key).isObject();
				String phoneEntry = phone.get("number").toString();
				phoneList.addItem(phoneEntry, key);
			}
			showSelectedPhoneInfo();
		} else
			lblMessage.setText("Error: no registered phones found.");
	}
	
	private void showPhoneDetailsFailure() {
		lblMessage.setText("Error: could not receive phone data.");
	}
	
	private void showSelectedPhoneInfo() {
		if (phones != null) {
			String key = phoneList.getValue(phoneList.getSelectedIndex());
			mainGrid.setWidget(2,1, new Label("test"));
			Grid grid = new Grid(6,2);
			JSONObject phone = phones.get(key).isObject();
			grid.setWidget(0,0, new Label("Brand:"));
			grid.setWidget(0,1, new Label(phone.get("brand").toString()));
			grid.setWidget(1,0, new Label("Type:"));
			grid.setWidget(1,1, new Label(phone.get("type").toString()));
			grid.setWidget(2,0, new Label("IMEI:"));
			grid.setWidget(2,1, new Label(phone.get("imei").toString()));
			grid.setWidget(3,0, new Label("IP Address:"));
			grid.setWidget(3,1, new Label(phone.get("ip").toString()));
			grid.setWidget(4,0, new Label("Phone Number:"));
			grid.setWidget(4,1, new Label(phone.get("number").toString()));
			grid.setWidget(5,0, new Label("Date added:"));
			grid.setWidget(5,1, new Label(phone.get("date").toString()));
			mainGrid.setWidget(2,1, grid);
		}
	}
	
	public HomeScreen(User user, final AsyncCallback mainCallback)	{
		Label lblWelcome = new Label();
		lblWelcome.setText("Hello "+user.getUserName() + "!");
		Button btnLogout = new Button("logout");
		lblMessage = new Label();
		phoneList = new ListBox();
		
		mainGrid = new Grid(3, 3);
		mainGrid.setWidth("800px");
		mainGrid.setWidget(0,0, lblWelcome);
		mainGrid.setWidget(0,1, lblMessage);
		mainGrid.setWidget(0,2, btnLogout);
		mainGrid.setWidget(1,1, phoneList);
		

		btnLogout.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event)	{
				svc.logout(new AsyncCallback() {
		            public void onSuccess(Object result) {
		            	mainCallback.onSuccess(result);
		            }
		            public void onFailure(Throwable ex) {
		            	mainCallback.onFailure(ex);
		            }
				});
			}			
		});

		phoneList.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				showSelectedPhoneInfo();
			}
		});
		
		initWidget(mainGrid);

		getPhoneDetails();
	}
}
