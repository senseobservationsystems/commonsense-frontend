package nl.sense_os.commonsense.client.main.components;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;

public class HomeScreen extends LayoutContainer {

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        
        Frame frame = new Frame("http://welcome.sense-os.nl");
        frame.setStylePrimaryName("senseFrame");
        
        this.setLayout(new FitLayout());
        this.add(frame);
    }
}
