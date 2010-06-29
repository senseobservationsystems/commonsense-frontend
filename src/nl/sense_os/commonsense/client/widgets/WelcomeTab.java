package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;

public class WelcomeTab extends LayoutContainer {

    public WelcomeTab(String username) {
        
        // prepare layout
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(10));
        this.setLayout(layout);

        this.setSize("100%", "100%");
        
        // welcome text
        Text welcome = new Text("Welcome to CommonSense, " + username + "!");
        this.add(welcome, new VBoxLayoutData(new Margins(10, 0, 10, 0)));
        
        // help text
        Text help = new Text("Please select a smart phone in the left panel to view its sensors.");
        this.add(help, new VBoxLayoutData(new Margins(10, 0, 10, 0)));
    }
}
