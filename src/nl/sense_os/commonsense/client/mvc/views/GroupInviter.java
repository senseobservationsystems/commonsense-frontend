package nl.sense_os.commonsense.client.mvc.views;

import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.utility.Log;

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
        this.inviteButton = new Button("Invite", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (form.isValid()) {
                    onSubmit();
                }
            }
        });

        this.cancelButton = new Button("Cancel", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                GroupInviter.this.fireEvent(GroupEvents.InviteCancelled);
            }
        });
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
        
        this.email = new TextField<String>();
        this.email.setFieldLabel("Email");
        this.email.setAllowBlank(false);
        this.form.add(this.email);
        
        initButtons();
        
        this.window.add(this.form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setWidth(350);
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
        MessageBox.alert("CommonSense", "Failed to invite user. Please check the email address.", null);
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
            this.inviteButton.setIcon(IconHelper.create("gxt/images/gxt/icons/loading.gif"));
            this.cancelButton.setEnabled(false);
        } else {
            this.inviteButton.setIcon(IconHelper.create("gxt/images/gxt/icons/page-next.gif"));
            this.cancelButton.setEnabled(true);
        }
    }
}
