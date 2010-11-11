package nl.sense_os.testing.client.widgets;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;

public class GroupSearchForm extends ContentPanel {

	public GroupSearchForm() {
		setHeaderVisible(false);

		// Form
		FormPanel form = new FormPanel();		
		form.setFrame(true);
		//form.setLabelSeparator("");
		form.setHeading("Group settings");
		form.setWidth(350);

		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");		
		form.setLayout(formLayout);

		// Group search
		TextField<String> groupSearch = new TextField<String>();
		groupSearch.setFieldLabel("group search");
		
		// Buttons
		Button searchBtn = new Button();
		searchBtn.setText("search");
		searchBtn.setWidth(100);
		searchBtn.addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				System.out.println("event: " + be.getEvent().getString());
				System.out.println("save pressed");
			}
		});
		
		LayoutContainer btnContainer= new LayoutContainer();
		HBoxLayout layout = new HBoxLayout();
		layout.setPadding(new Padding(5));  
		layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		layout.setPack(BoxLayoutPack.CENTER);  
		btnContainer.setLayout(layout);  

		HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));  
		btnContainer.add(searchBtn, layoutData);
		
		// Adds widgets to the form.
		form.add(groupSearch);
		form.add(btnContainer);

		// Adds the form to the content panel.
		add(form);		
	}

}
