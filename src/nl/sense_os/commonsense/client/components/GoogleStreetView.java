package nl.sense_os.commonsense.client.components;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.Frame;

public class GoogleStreetView extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "Google Street View";

    public GoogleStreetView(String id, String email, String password) {
        setLayout(new FitLayout());
        setBorders(false);
        setScrollMode(Scroll.NONE);

        final Frame f = new Frame("http://demo.almende.com/commonSense2/deviceservices/gps_service.php?device_id="+id+"&email="+email+"&password="+password);
        f.setStylePrimaryName("senseFrame");
        this.add(f, new FitData(0));
    }
}