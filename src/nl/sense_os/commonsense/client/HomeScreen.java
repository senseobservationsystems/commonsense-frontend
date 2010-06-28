package nl.sense_os.commonsense.client;

import java.util.List;

import nl.sense_os.commonsense.dto.PhoneModel;
import nl.sense_os.commonsense.server.data.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class HomeScreen extends Composite{

	DataServiceAsync dataSvc = (DataServiceAsync) GWT.create(DataService.class);
	
	List<PhoneModel> phones;
	
	Grid mainGrid;
	Label lblMessage;
	ListBox phoneList;
	
	
	private void getPhoneDetails() {
		AsyncCallback<List<PhoneModel>> callback = new AsyncCallback<List<PhoneModel>>() {
			public void onSuccess(List<PhoneModel> result) {
				phones = result;
				showPhoneDetails();
            }
            public void onFailure(Throwable ex) {
            	showPhoneDetailsFailure();
            }
    	};
    	dataSvc.getPhoneDetails(callback);
	}

	private void showPhoneDetails() {
		int i = phones.size();
		if (i > 0) {
			// add the phone numbers to the item list
			lblMessage.setText("Found " + i + " registered phones.");
			for (int index = 0; index < phones.size(); index++) {
				PhoneModel phoneModel = phones.get(index);
				phoneList.addItem(phoneModel.getNumber(), Integer.toString(index));
			}
			// show the phone info of the selected item
			showSelectedPhoneInfo();
		} else
			lblMessage.setText("Error: no registered phones found.");
	}
	
	private void showPhoneDetailsFailure() {
		lblMessage.setText("Error: could not receive phone data.");
	}
	
	private void showSelectedPhoneInfo() {
		if (phones != null) {
			int index = Integer.parseInt(phoneList.getValue(phoneList.getSelectedIndex()));
			mainGrid.setWidget(2,1, new Label("test"));

			Grid grid = new Grid(7,2);
			PhoneModel phoneModel = phones.get(index);
			grid.setWidget(0,0, new Label("Brand:"));
			grid.setWidget(0,1, new Label(phoneModel.getBrand()));
			grid.setWidget(1,0, new Label("Type:"));
			grid.setWidget(1,1, new Label(phoneModel.getType()));
			grid.setWidget(2,0, new Label("IMEI:"));
			grid.setWidget(2,1, new Label(phoneModel.getImei()));
			grid.setWidget(3,0, new Label("IP Address:"));
			grid.setWidget(3,1, new Label(phoneModel.getIp()));
			grid.setWidget(4,0, new Label("Phone Number:"));
			grid.setWidget(4,1, new Label(phoneModel.getNumber()));
			grid.setWidget(5,0, new Label("Date added:"));
			grid.setWidget(5,1, new Label(phoneModel.getDate()));
			//grid.setWidget(5,0, new Label("#Sensors:"));
			//grid.setWidget(5,1, new Label(Integer.toString(phoneModel.getSensors().size())));
			
			mainGrid.setWidget(2,1, grid);
		}
	}
	
	public HomeScreen(User user, final AsyncCallback<Void> mainCallback)	{
		Label lblWelcome = new Label();
		lblWelcome.setText("Hello "+user.getName() + "!");
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
				dataSvc.logout(new AsyncCallback<Void>() {
		            public void onSuccess(Void result) {
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
