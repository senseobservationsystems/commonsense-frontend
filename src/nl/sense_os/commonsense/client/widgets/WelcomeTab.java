package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.Frame;

public class WelcomeTab extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "WelcomeTab";

    public WelcomeTab() {
        
        this.setLayout(new FitLayout());
        this.setBorders(false);
        this.setScrollMode(Scroll.NONE);
        
        Frame f = new Frame("http://welcome.sense-os.nl");
        f.setStylePrimaryName("senseFrame");
        this.add(f, new FitData(0));
    }
}