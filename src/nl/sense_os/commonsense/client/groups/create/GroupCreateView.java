package nl.sense_os.commonsense.client.groups.create;

import java.util.HashMap;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.groups.create.forms.GroupCreatorWindow;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GroupCreateView extends View {

    private static final Logger LOG = Logger.getLogger(GroupCreateView.class.getName());
    private HashMap<String, GroupCreatorWindow> windows;

    public GroupCreateView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(GroupCreateEvents.ShowCreator)) {
            LOG.finest("ShowCreator");
            onShow();

        } else if (type.equals(GroupCreateEvents.CreateComplete)) {
            LOG.finest("CreateGroupComplete");
            String id = event.getData("id");
            onComplete(id);

        } else if (type.equals(GroupCreateEvents.CreateFailed)) {
            LOG.warning("CreateGroupFailed");
            String id = event.getData("id");
            onFailed(id);

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void hideDialog(String id) {
        GroupCreatorWindow window = windows.get(id);
        if (null == window) {
            LOG.warning("Window " + id + " not found.");
            return;
        }

        window.hide();
        window.setBusy(false);
    }

    @Override
    protected void initialize() {
        super.initialize();
        windows = new HashMap<String, GroupCreatorWindow>();
    }

    private void onComplete(String id) {
        hideDialog(id);
    }

    private void onFailed(final String id) {
        final GroupCreatorWindow window = windows.get(id);
        if (null == window) {
            LOG.warning("Window " + id + " not found.");
            return;
        }

        window.setBusy(false);
        MessageBox.confirm(null, "Failed to create group, retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                    window.createGroup();
                } else {
                    hideDialog(id);
                }
            }
        });
    }

    private void onShow() {
        GroupCreatorWindow window = new GroupCreatorWindow();
        window.show();
        windows.put("" + System.currentTimeMillis(), window);
    }
}
