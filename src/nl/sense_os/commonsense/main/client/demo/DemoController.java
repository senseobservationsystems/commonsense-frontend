package nl.sense_os.commonsense.main.client.demo;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

public class DemoController extends Controller {

    @SuppressWarnings("unused")
    private static final String TAG = "DemoController";

    private View demo;

    public DemoController() {
        registerEventTypes(DemoEvents.Show);
    }

    @Override
    public void handleEvent(AppEvent event) {
        forwardToView(this.demo, event);
    }

    @Override
    protected void initialize() {
        this.demo = new DemoView(this);
        super.initialize();
    }

}
