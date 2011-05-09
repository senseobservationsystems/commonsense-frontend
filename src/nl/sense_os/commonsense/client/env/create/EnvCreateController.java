package nl.sense_os.commonsense.client.env.create;

import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class EnvCreateController extends Controller {

    private static final String TAG = "EnvCreateController";
    private View creator;

    public EnvCreateController() {
        registerEventTypes(EnvCreateEvents.ShowCreator);
        registerEventTypes(EnvCreateEvents.Forward, EnvCreateEvents.Back, EnvCreateEvents.Cancel);
        registerEventTypes(EnvCreateEvents.OutlineComplete);
        registerEventTypes(EnvCreateEvents.CreateRequest, EnvCreateEvents.CreateSuccess,
                EnvCreateEvents.CreateFailure);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(EnvCreateEvents.CreateRequest)) {
            Log.d(TAG, "CreateRequest");
            create();

        } else

        {
            forwardToView(this.creator, event);
        }
    }

    private void create() {
        Dispatcher.forwardEvent(EnvCreateEvents.CreateSuccess);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.creator = new EnvCreator(this);
    }

}
