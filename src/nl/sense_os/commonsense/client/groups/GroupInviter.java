package nl.sense_os.commonsense.client.groups;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GroupInviter extends View {

    private static final String TAG = "GroupInviter";
    private Window window;
    private FormPanel form;
    private TextField<String> email;
    private Button inviteButton;
    private Button cancelButton;
    private TreeModel group;

    public GroupInviter(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupEvents.ShowInviter)) {
            onShow(event);
        } else if (type.equals(GroupEvents.InviteCancelled)) {
            Log.d(TAG, "CreateGroupCancelled");
            onCancelled(event);
        } else if (type.equals(GroupEvents.InviteComplete)) {
            Log.d(TAG, "CreateGroupComplete");
            onComplete(event);
        } else if (type.equals(GroupEvents.InviteFailed)) {
            Log.w(TAG, "CreateGroupFailed");
            onFailed(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
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
                    GroupInviter.this.fireEvent(GroupEvents.InviteCancelled);
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        this.inviteButton = new Button("Invite", IconHelper.create(Constants.ICON_BUTTON_GO), l);

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

        this.email = new TextField<String>();
        this.email.setFieldLabel("Email");
        this.email.setAllowBlank(false);
        this.form.add(this.email, formData);

        initButtons();

        this.window.add(this.form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setWidth(323);
        this.window.setHeight(200);
        this.window.setResizable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Invite user to group");

        initForm();
    }

    private void onCancelled(AppEvent event) {
        this.window.hide();
        setBusy(false);
    }

    private void onComplete(AppEvent event) {
        this.window.hide();
        Dispatcher.forwardEvent(GroupEvents.ListRequested);
        setBusy(false);
    }

    private void onFailed(AppEvent event) {
        MessageBox.alert("CommonSense", "Failed to invite user. Please check the email address.",
                null);
        setBusy(false);
    }

    private void onShow(AppEvent event) {
        this.group = event.<TreeModel> getData();
        Log.d(TAG, "Invite users for group " + group.get("id"));
        this.window.show();
        this.form.reset();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(GroupEvents.InviteRequested);
        event.setData("groupId", this.group.<String> get("id"));
        event.setData("email", this.email.getValue());
        fireEvent(event);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.inviteButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
            this.cancelButton.disable();
        } else {
            this.inviteButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
            this.cancelButton.enable();
        }
    }
}
