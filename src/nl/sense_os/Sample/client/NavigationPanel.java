package nl.sense_os.Sample.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.sense_os.Sample.client.widgets.PaginationGridPanel;
import nl.sense_os.Sample.client.widgets.UserProfileWin;

import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

//public class NavigationPanel extends LayoutContainer {
public class NavigationPanel extends ContentPanel {
	
	public NavigationPanel() {
		setHeaderVisible(false);
		
		ContentPanel panel = new ContentPanel();  
		panel.setHeaderVisible(false);
		panel.setBodyBorder(false);  
		 
		panel.setLayout(new AccordionLayout()); 

		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();
		ColumnConfig column = new ColumnConfig();
		column.setId("users");
		column.setHeader("users");
		column.setDataIndex("name");
		column.setWidth(198);
		colConf.add(column);

		ModelType model = new ModelType();
		model.setTotalName("total");
		model.setRoot("users");
		model.addField("id");
		model.addField("name");
		
		PaginationGridPanel gridPanel = new PaginationGridPanel(
				"http://dev.almende.com/commonsense/sample/users_nav_test.php", 
				model, 
				colConf, 
				5);

		gridPanel.setAutoHeight(true);
		gridPanel.setAutoWidth(true);
		
		gridPanel.getGrid().addListener(Events.CellClick, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				GridEvent<?> gr = (GridEvent<?>) be;
				String value = gr.getModel().get("name");
				System.out.println("name: " + value);
				
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("name", value);
				
				/*
				Window w = new Window();
				w.setHeading("user profile");
				w.setModal(true);
				w.setSize(650, 450);
				w.setMaximizable(true);
				w.setToolTip("The ExtGWT product page...");
				//w.setUrl("http://www.google.com");
				w.show();
				*/
				UserProfileWin profile = new UserProfileWin(300, 150, params);
				//profile.setMaximizable(true);
				profile.show();

			}
		});
		
		gridPanel.load();
		
		ContentPanel cp = new ContentPanel();  
		cp.setAnimCollapse(false);  
		cp.setHeading("online users");  
		cp.setLayout(new RowLayout());
		cp.add(gridPanel);
		panel.add(cp);
        
		cp = new ContentPanel();  
		cp.setAnimCollapse(false);  
		cp.setBodyStyleName("pad-text");  
		cp.setHeading("settings");  
		cp.addText("Settings ...");  
		panel.add(cp);		

		cp = new ContentPanel();  
		cp.setAnimCollapse(false);  
		cp.setBodyStyleName("pad-text");  
		cp.setHeading("devices");  
		cp.addText("stuff ...");  
		panel.add(cp);		
		
		cp = new ContentPanel();  
		cp.setAnimCollapse(false);  
		cp.setBodyStyleName("pad-text");  
		cp.setHeading("More Stuff");  
		cp.addText("more stuff...");  
		panel.add(cp);
		panel.setSize(200, 325);  
		
		add(panel); 
	}
	
}
