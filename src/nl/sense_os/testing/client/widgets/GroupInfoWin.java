package nl.sense_os.testing.client.widgets;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.google.gwt.user.client.ui.Image;

public class GroupInfoWin extends Window {

	public GroupInfoWin(int width, int height, HashMap<String, String> param) {
		setWidth(width);
		setAutoHeight(true);
		setBodyStyle("background: #fff");
		setBodyBorder(false);

		// Container
		final LayoutContainer container = new LayoutContainer();
		HBoxLayout layout = new HBoxLayout();
		//layout.setPadding(new Padding(2));
		container.setLayout(layout);	
		
		// Image
		final Image logo = new Image("/img/avatar.jpg");
        logo.setPixelSize(96, 96);
        container.add(logo);        
		
        // West and center panels.
        final ContentPanel center = new ContentPanel();
        center.setHeaderVisible(false);
        center.setWidth(60);
        center.addText("group:<br />description:");
        center.setBorders(false);

		final ContentPanel west = new ContentPanel();		
		west.setHeaderVisible(false);
		west.addText(param.get("name") + "<br />Lorem ipsum ad his scripta blandit partiendo, "+
				"eum fastidii accumsan euripidis in, eum liber hendrerit an.");
		
		Margins margins = new Margins(10, 0, 0, 10);        
		container.add(center, new HBoxLayoutData(margins));
		container.add(west, new HBoxLayoutData(margins));
        
		// Button
		Button joinBtn = new Button("join");
		
		HBoxLayout btnLayout = new HBoxLayout();
		btnLayout.setPadding(new Padding(5));
		btnLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		btnLayout.setPack(BoxLayoutPack.CENTER);

		LayoutContainer btnContainer = new LayoutContainer();
		btnContainer.setLayout(btnLayout);
		btnContainer.add(joinBtn);
		
		// Adds two panels to the window
		add(container);
		add(btnContainer);
	}
	
	public void addNotification() {
		addListener(Events.Activate, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				Info.display("Info", "Example!!!");
			}
		});
	}
}
