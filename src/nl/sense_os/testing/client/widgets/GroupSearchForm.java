package nl.sense_os.testing.client.widgets;

import java.util.HashMap;

import nl.sense_os.testing.client.common.grid.DataStore;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public class GroupSearchForm extends ContentPanel {

	private int pageSize = 3;
	private int width = 400;
	private int minChars = 2;
	private String title = "group search";
	
	public GroupSearchForm() {
		setHeaderVisible(false);

		ModelType dataModel = new ModelType();
		dataModel.setTotalName("total");
		dataModel.setRoot("groups");
		dataModel.addField("id");
		dataModel.addField("name");
		dataModel.addField("desc");
		
		DataStore store = new DataStore(
				"http://dev2.almende.com/commonsense/testing/groups_list_test.php", 
				dataModel);

		store.setOffset(0);
		
		ListStore<ModelData> listStore = store.getStore();

		ComboBox<ModelData> combo = new ComboBox<ModelData>();  
		combo.setWidth(width);  
		combo.setDisplayField("name");  
		combo.setItemSelector("div.search-item");  
		combo.setTemplate(getTemplate());  
		combo.setStore(listStore);  
		combo.setHideTrigger(true);  
		combo.setPageSize(pageSize);
		combo.setAutoHeight(true);
		combo.setMinChars(minChars); // min chars required to start searching
		
		// Displays a window with the group data when an element is selected in
		// the combo box list.
		combo.addListener(Events.Select, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				FieldEvent fe = (FieldEvent) be;
				ComboBox<ModelData> combo = (ComboBox<ModelData>) fe.getSource();
				String value = combo.getValue().get("name");
								
				HashMap<String, String> winDataParams = new HashMap<String, String>();
				winDataParams.put("name", value);
				
				GroupInfoWin group = new GroupInfoWin(310, 150, winDataParams);
				group.addNotification();
				group.show();
			}
		});

		VerticalPanel vp = new VerticalPanel();  
		vp.setSpacing(10);  
   
		vp.addText("<span class='text'>" + title + "</span>");  
		vp.add(combo);  
   
		add(vp);
	}
	
	/**
	 * Template to give an html format to each row of the combo box list.
	 * This method is executed from javascript in the client side.
	 */
	private native String getTemplate() /*-{ 
	  return [
	  '<tpl for="."><div class="search-item">', 
	  '<h3>{name}</h3>', 
	  '{desc}', 
	  '</div></tpl>' 
	  ].join(""); 
	}-*/;  	
}
