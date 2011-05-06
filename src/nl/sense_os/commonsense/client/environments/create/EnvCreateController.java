package nl.sense_os.commonsense.client.environments.create;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;

public class EnvCreateController extends Controller {

    private static final String TAG = "EnvCreateController";
    private View creator;

    public EnvCreateController() {
        registerEventTypes(EnvCreateEvents.ShowCreator);
        registerEventTypes(EnvCreateEvents.FormValid, EnvCreateEvents.FormInvalid);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvCreateEvents.CreateRequest)) {
            create();

        } else

        {
            forwardToView(this.creator, event);
        }
    }

    private void create() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initialize() {
        super.initialize();
        this.creator = new EnvCreator(this);
    }

}
