package nl.sense_os.Sample.client.widgets;

import java.util.HashMap;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.user.client.ui.Image;

public class UserProfileWin extends Window {

	public UserProfileWin(int width, int height, HashMap<String, String> param) {
		setWidth(width);
		//setHeight(height);
		setAutoHeight(true);
		
		final LayoutContainer container = new LayoutContainer();
		HBoxLayout layout = new HBoxLayout();
		layout.setPadding(new Padding(5));
		container.setLayout(layout);
				
        //final Image logo = new Image("/img/logo_sense-150.png");
		final Image logo = new Image("/img/avatar.jpg");
        logo.setPixelSize(94, 120);
        //container.add(logo, new HBoxLayoutData(new Margins(2)));
        container.add(logo);
                
		final ContentPanel east = new ContentPanel();		
		east.setHeaderVisible(false);
		east.setBorders(false);
		east.addText("user: " + param.get("name") + "<br />mobile: 2323423<br />mail: " + param.get("name") + "@m.com");
		container.add(east, new HBoxLayoutData(new Margins(0, 0, 0, 20)));
		container.add(east);

		setBodyStyle("background: #fff");
		add(container);
		
	}
}
