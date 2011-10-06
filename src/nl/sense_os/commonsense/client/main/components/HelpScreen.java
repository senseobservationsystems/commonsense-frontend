package nl.sense_os.commonsense.client.main.components;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;

public class HelpScreen extends LayoutContainer {

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        Frame frame = new Frame("http://welcome.sense-os.nl/node/6");
        frame.setStylePrimaryName("senseFrame");

        LayoutData data = new FitData(new Margins(0));

        this.setLayout(new FitLayout());
        this.add(frame, data);
    }
}
