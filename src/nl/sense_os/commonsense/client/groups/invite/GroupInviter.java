package nl.sense_os.commonsense.client.groups.invite;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;

import com.extjs.gxt.ui.client.Style.Scroll;
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
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

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
        if (type.equals(GroupInviteEvents.ShowInviter)) {
            onShow(event);

        } else if (type.equals(GroupInviteEvents.InviteComplete)) {
            // LOG.fine( "InviteComplete");
            hideWindow();

        } else if (type.equals(GroupInviteEvents.InviteFailed)) {
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

        inviteButton = new Button("Add", SenseIconProvider.ICON_BUTTON_GO, l);
        cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(inviteButton);

        form.addButton(inviteButton);
        form.addButton(cancelButton);
    }

    private void initForm() {
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setLayout(new FormLayout());
        form.setLayout(new FormLayout(LabelAlign.TOP));
        form.setScrollMode(Scroll.AUTOY);

        final FormData formData = new FormData("-20");

        LabelField explanation = new LabelField(
                "Please enter the username of the person that you want to add to the group.");
        explanation.setHideLabel(true);

        LabelField notice = new LabelField(
                "Please note: you can only add users to the group if they have already requested to join it.");
        notice.setHideLabel(true);

        username = new TextField<String>();
        username.setFieldLabel("Username");
        username.setAllowBlank(false);

        form.add(explanation, new FormData(""));
        form.add(username, formData);
        form.add(notice, new FormData(""));

        initButtons();

        window.add(form, new FitData());
    }

    private void onFailed(AppEvent event) {
        MessageBox.alert("CommonSense",
                "Failed to add user! Make sure the user has requested to join the group first.",
                null);
        setBusy(false);
    }

    private void onShow(AppEvent event) {

        group = event.<GroupModel> getData("group");
        LOG.fine("Add user for group " + group.getId() + "(" + group.getName() + ")");

        window = new CenteredWindow();
        window.setHeading("Add user to " + group.getName());
        window.setSize(323, 210);
        window.setLayout(new FitLayout());

        initForm();

        window.show();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(GroupInviteEvents.InviteRequested);
        event.setData("group", group);
        event.setData("username", username.getValue());
        event.setSource(this);
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
