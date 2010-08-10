package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

public class WelcomeTab extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "WelcomeTab";
    private final String username;

    public WelcomeTab(String username) {
        this.username = username;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);        
        
        final Image logo = new Image("/img/logo_sense-800.png");
        logo.setPixelSize(800, 600);  

        // welcome text
        Text welcome = new Text("Welcome to CommonSense, " + this.username + "! "
                + "Please select a sensor device in the left panel to view its sensor values.");
        
        VBoxLayout layout = new VBoxLayout();
        layout.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
        LayoutContainer wrapper = new LayoutContainer(layout);
        wrapper.add(logo, new VBoxLayoutData(10,10,10,10));
        wrapper.add(welcome, new VBoxLayoutData(10,10,10,10));
        
        this.setLayout(new FitLayout());
        this.setSize("100%", "100%");
        this.add(wrapper);
    }
}
