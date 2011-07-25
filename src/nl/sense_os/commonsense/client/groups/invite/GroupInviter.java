package nl.sense_os.commonsense.client.groups.invite;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupInviter extends View {

    private static final Logger LOG = Logger.getLogger(GroupInviter.class.getName());
    private Window window;
    private FormPanel form;
    private TextField<String> username;
    private Button inviteButton;
    private Button cancelButton;
    private GroupModel group;

    public GroupInviter(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(InviteEvents.ShowInviter)) {
            onShow(event);

        } else if (type.equals(InviteEvents.InviteComplete)) {
            // LOG.fine( "InviteComplete");
            hideWindow();

        } else if (type.equals(InviteEvents.InviteFailed)) {
            LOG.warning("InviteFailed");
            onFailed(event);

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        window.hide();
        setBusy(false);
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent be) {
                final Button b = be.getButton();
                if (b.equals(inviteButton)) {
                    if (form.isValid()) {
                        onSubmit();
                    }
                } else if (b.equals(cancelButton)) {
                    hideWindow();
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };

        inviteButton = new Button("Invite", SenseIconProvider.ICON_BUTTON_GO, l);

        cancelButton = new Button("Cancel", l);
        setBusy(false);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(inviteButton);

        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.addButton(inviteButton);
        form.addButton(cancelButton);
    }

    private void initForm() {
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);

        final FormData formData = new FormData("-10");

        username = new TextField<String>();
        username.setFieldLabel("Username");
        username.setAllowBlank(false);
        form.add(username, formData);

        initButtons();

        window.add(form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        window = new CenteredWindow();
        window.setHeading("Invite user to group");
        window.setSize(323, 200);
        window.setLayout(new FitLayout());

        initForm();
    }

    private void onFailed(AppEvent event) {
        MessageBox.alert("CommonSense", "Failed to invite user. Please check the username.", null);
        setBusy(false);
    }

    private void onShow(AppEvent event) {
        group = event.<GroupModel> getData("group");
        LOG.fine("Invite users for group " + group.getId());
        form.reset();

        window.show();
        window.center();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(InviteEvents.InviteRequested);
        event.setData("groupId", group.getId());
        event.setData("username", username.getValue());
        fireEvent(event);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            inviteButton.setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            inviteButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
        }
    }
}
