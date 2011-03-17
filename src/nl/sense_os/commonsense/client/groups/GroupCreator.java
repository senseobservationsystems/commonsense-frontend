package nl.sense_os.commonsense.client.groups;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class GroupCreator extends View {

    private static final String TAG = "GroupCreator";
    private Window window;
    private FormPanel form;
    private TextField<String> name;
    // private TextField<String> email;
    private TextField<String> username;
    private TextField<String> password;
    private Button submitButton;
    private Button cancelButton;

    public GroupCreator(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(GroupEvents.ShowCreator)) {
            onShow(event);
        } else if (type.equals(GroupEvents.CreateCancelled)) {
            // Log.d(TAG, "CreateGroupCancelled");
            onCancelled(event);

        } else if (type.equals(GroupEvents.CreateComplete)) {
            // Log.d(TAG, "CreateGroupComplete");
            onComplete(event);

        } else if (type.equals(GroupEvents.CreateFailed)) {
            // Log.w(TAG, "CreateGroupFailed");
            onFailed(event);

        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initForm() {

        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setBodyBorder(false);

        initFields();
        initButtons();

        this.window.add(form);
    }

    private void initButtons() {
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(submitButton)) {
                    if (form.isValid()) {
                        onSubmit();
                    }
                } else if (pressed.equals(cancelButton)) {
                    GroupCreator.this.fireEvent(GroupEvents.CreateCancelled);
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };
        this.submitButton = new Button("Create", IconHelper.create(Constants.ICON_BUTTON_GO), l);

        this.cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.submitButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.submitButton);
        this.form.addButton(this.cancelButton);
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        this.name = new TextField<String>();
        this.name.setFieldLabel("Name");
        this.name.setAllowBlank(false);

        // this.email = new TextField<String>();
        // this.email.setFieldLabel("Email*");
        // this.email.setAllowBlank(false);

        final FieldSet loginFields = new FieldSet();
        loginFields.setHeading("Log in as this group");
        loginFields.setCheckboxToggle(true);
        loginFields.setExpanded(false);
        loginFields.setLayout(new FormLayout());

        // allow the username and password to be blank iff the fieldset is not checked
        Listener<FieldSetEvent> listener = new Listener<FieldSetEvent>() {

            @Override
            public void handleEvent(FieldSetEvent be) {
                // Log.d(TAG, "Expand");
                boolean isVisible = loginFields.isExpanded();
                username.setAllowBlank(!isVisible);
                password.setAllowBlank(!isVisible);
            }
        };
        loginFields.addListener(Events.Expand, listener);
        loginFields.addListener(Events.Collapse, listener);

        this.username = new TextField<String>();
        this.username.setFieldLabel("Username");

        this.password = new TextField<String>();
        this.password.setFieldLabel("Password");
        this.password.setPassword(true);

        loginFields.add(this.username, formData);
        loginFields.add(this.password, formData);

        this.form.add(this.name, formData);
        this.form.add(loginFields, formData);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Create group");
        this.window.setSize(323, 200);
        this.window.setLayout(new FitLayout());

        initForm();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.submitButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
            this.cancelButton.disable();
        } else {
            this.submitButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
            this.cancelButton.enable();
        }
    }

    private void onCancelled(AppEvent event) {
        this.window.hide();
        setBusy(false);
    }

    private void onComplete(AppEvent event) {
        this.window.hide();
        Dispatcher.forwardEvent(GroupEvents.ListRequest);
        setBusy(false);
    }

    private void onFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to create group, retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                    onSubmit();
                } else {
                    window.hide();
                }

            }
        });
    }

    private void onShow(AppEvent event) {
        this.form.reset();
        this.window.show();
        this.window.center();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(GroupEvents.CreateRequested);
        event.setData("name", this.name.getValue());
        // event.setData("email", this.email.getValue());
        if (this.username.getValue() != null && this.username.getValue().length() > 0) {
            event.setData("username", this.username.getValue());
            event.setData("password", this.password.getValue());
        }
        fireEvent(event);
    }

}
