package nl.sense_os.commonsense.client.groups.invite;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.utility.SenseIconProvider;

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

    private static final Logger logger = Logger.getLogger("GroupInviter");
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
            // logger.fine( "InviteComplete");
            hideWindow();

        } else if (type.equals(InviteEvents.InviteFailed)) {
            logger.warning("InviteFailed");
            onFailed(event);

        } else {
            logger.warning("Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        this.window.hide();
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
                    logger.warning("Unexpected button pressed");
                }
            }
        };

        this.inviteButton = new Button("Invite", SenseIconProvider.ICON_BUTTON_GO, l);

        this.cancelButton = new Button("Cancel", l);
        setBusy(false);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.inviteButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.inviteButton);
        this.form.addButton(this.cancelButton);
    }

    private void initForm() {
        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setBodyBorder(false);

        final FormData formData = new FormData("-10");

        this.username = new TextField<String>();
        this.username.setFieldLabel("Username");
        this.username.setAllowBlank(false);
        this.form.add(this.username, formData);

        initButtons();

        this.window.add(this.form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Invite user to group");
        this.window.setSize(323, 200);
        this.window.setLayout(new FitLayout());

        initForm();
    }

    private void onFailed(AppEvent event) {
        MessageBox.alert("CommonSense", "Failed to invite user. Please check the username.", null);
        setBusy(false);
    }

    private void onShow(AppEvent event) {
        this.group = event.<GroupModel> getData("group");
        logger.fine("Invite users for group " + group.get(GroupModel.ID));
        this.form.reset();

        this.window.show();
        this.window.center();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(InviteEvents.InviteRequested);
        event.setData("groupId", this.group.<String> get(GroupModel.ID));
        event.setData("username", this.username.getValue());
        fireEvent(event);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.inviteButton.setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            this.inviteButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
        }
    }
}
