package nl.sense_os.commonsense.main.client.groups.invite;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.GroupModel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GroupInviteView extends View {

    private static final Logger LOG = Logger.getLogger(GroupInviteView.class.getName());
    private GroupInviteWindow window;
    private GroupModel group;

    public GroupInviteView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupInviteEvents.ShowInviter)) {
            LOG.finest("ShowInviter");
            onShow(event);

        } else if (type.equals(GroupInviteEvents.InviteComplete)) {
            LOG.finest("InviteComplete");
            hideWindow();

        } else if (type.equals(GroupInviteEvents.InviteFailed)) {
            LOG.warning("InviteFailed");
            onFailed(event);

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        setBusy(false);
        window.hide();
    }

    private void onFailed(AppEvent event) {
        setBusy(false);
        MessageBox.alert("CommonSense",
                "Failed to add user! Make sure the user has requested to join the group first.",
                null);
    }

    private void onShow(AppEvent event) {

        group = event.<GroupModel> getData("group");
        LOG.fine("Add user for group " + group.getId() + "(" + group.getName() + ")");

        window = new GroupInviteWindow(group);
        window.show();

        SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent be) {
                final Button b = be.getButton();
                if (b.equals(window.getBtnAdd())) {
                    onSubmit();
                } else if (b.equals(window.getBtnCancel())) {
                    hideWindow();
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };

        window.getBtnAdd().addSelectionListener(listener);
        window.getBtnCancel().addSelectionListener(listener);
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(GroupInviteEvents.InviteRequested);
        event.setData("group", group);
        event.setData("username", window.getUsername().getValue());
        event.setSource(this);
        fireEvent(event);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            window.getBtnAdd().setIconStyle("sense-btn-icon-loading");
        } else {
            window.getBtnAdd().setIconStyle("sense-btn-icon-go");
        }
    }
}
