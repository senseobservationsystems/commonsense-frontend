package nl.sense_os.commonsense.client.mvc.views;

import nl.sense_os.commonsense.client.mvc.events.GroupEvents;
import nl.sense_os.commonsense.client.utility.Log;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
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

public class GroupCreatorView extends View {

    private static final String TAG = "GroupCreatorView";
    private Window window;
    private FormPanel form;
    private TextField<String> name;
    private TextField<String> email;
    private TextField<String> username;
    private TextField<String> password;
    private Button createButton;
    private Button cancelButton;

    public GroupCreatorView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupEvents.ShowGroupCreator)) {
            onShow(event);
        } else if (type.equals(GroupEvents.CreateGroupCancelled)) {
            Log.d(TAG, "CreateGroupCancelled");
            onCancelled(event);
        } else if (type.equals(GroupEvents.CreateGroupComplete)) {
            Log.d(TAG, "CreateGroupComplete");
            onComplete(event);
        } else if (type.equals(GroupEvents.CreateGroupFailed)) {
            Log.w(TAG, "CreateGroupFailed");
            onFailed(event);
        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void initForm() {

        this.form = new FormPanel();
        this.form.setHeaderVisible(false);

        initFields();
        initButtons();

        this.window.add(form);
    }

    private void initButtons() {
        this.createButton = new Button("Create", new SelectionListener<ButtonEvent>() {

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
                GroupCreatorView.this.fireEvent(GroupEvents.CreateGroupCancelled);
            }
        });
        setBusy(false);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.createButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.createButton);
        this.form.addButton(this.cancelButton);
    }

    private void initFields() {
        this.name = new TextField<String>();
        this.name.setFieldLabel("Name*");
        this.name.setAllowBlank(false);

        this.email = new TextField<String>();
        this.email.setFieldLabel("Email*");
        this.email.setAllowBlank(false);

        this.username = new TextField<String>();
        this.username.setFieldLabel("Username");
        this.username.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Object value = be.getField().getValue();
                if (null != value) {
                    password.setAllowBlank(false);
                } else {
                    password.setAllowBlank(true);
                }
            }
        });

        this.password = new TextField<String>();
        this.password.setFieldLabel("Password");
        this.password.setPassword(true);
        this.password.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Object value = be.getField().getValue();
                if (null != value) {
                    username.setAllowBlank(false);
                } else {
                    username.setAllowBlank(true);
                }
            }
        });

        this.form.add(this.name);
        this.form.add(this.email);
        this.form.add(this.username);
        this.form.add(this.password);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new Window();
        this.window.setWidth(350);
        this.window.setHeight(200);
        this.window.setResizable(false);
        this.window.setLayout(new FitLayout());
        this.window.setHeading("Create group");

        initForm();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.createButton.setIcon(IconHelper.create("gxt/images/gxt/icons/loading.gif"));
            this.cancelButton.setEnabled(false);
        } else {
            this.createButton.setIcon(IconHelper.create("gxt/images/gxt/icons/page-next.gif"));
            this.cancelButton.setEnabled(true);
        }
    }

    private void onCancelled(AppEvent event) {
        this.window.hide();
        setBusy(false);
    }

    private void onComplete(AppEvent event) {
        this.window.hide();
        Dispatcher.forwardEvent(GroupEvents.GroupListRequested);
        setBusy(false);
    }

    private void onFailed(AppEvent event) {
        MessageBox.alert("CommonSense", "Failed to create group.", null);
        setBusy(false);
    }

    private void onShow(AppEvent event) {
        this.form.reset();
        this.window.show();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new AppEvent(GroupEvents.CreateGroupRequested);
        event.setData("name", this.name.getValue());
        event.setData("email", this.email.getValue());
        if (this.username.getValue() != null && this.username.getValue().length() > 0) {
            event.setData("username", this.username.getValue());
            event.setData("password", this.password.getValue());
        }
        fireEvent(event);
    }

}
