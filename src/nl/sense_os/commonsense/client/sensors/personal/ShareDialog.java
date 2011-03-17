package nl.sense_os.commonsense.client.sensors.personal;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.CenteredWindow;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class ShareDialog extends View {

    private static final String TAG = "ShareDialog";
    private Window window;
    private FormPanel form;
    private TextField<String> user;
    private Button createButton;
    private Button cancelButton;
    private List<SensorModel> sensors;

    private ListStore<TreeModel> store;

    public ShareDialog(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(MySensorsEvents.ShowShareDialog)) {
            // Log.d(TAG, "Show");
            onShow(event);

        } else if (type.equals(MySensorsEvents.ShareCancelled)) {
            // Log.d(TAG, "Cancelled");
            hideWindow();

        } else if (type.equals(MySensorsEvents.ShareComplete)) {
            // Log.d(TAG, "Complete");
            onComplete(event);

        } else if (type.equals(MySensorsEvents.ShareFailed)) {
            Log.w(TAG, "Failed");
            onFailed(event);

        } else {
            Log.w(TAG, "Unexpected event type: " + type);
        }
    }

    private void hideWindow() {
        this.window.hide();
        this.form.reset();
        setBusy(false);
    }

    private void initButtons() {

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button b = ce.getButton();
                if (b.equals(createButton)) {
                    if (form.isValid()) {
                        onSubmit();
                    }
                } else if (b.equals(cancelButton)) {
                    Dispatcher.forwardEvent(MySensorsEvents.ShareCancelled);
                } else {
                    Log.w(TAG, "Unexpected button pressed");
                }
            }
        };

        this.createButton = new Button("Share", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(this.form);
        binding.addButton(this.createButton);

        this.form.setButtonAlign(HorizontalAlignment.CENTER);
        this.form.addButton(this.createButton);
        this.form.addButton(this.cancelButton);
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        store = new ListStore<TreeModel>();

        this.user = new TextField<String>();
        this.user.setFieldLabel("Share with");
        this.user.setEmptyText("Enter a username...");
        this.user.setAllowBlank(false);

        this.form.add(this.user, formData);
    }

    private void initForm() {
        this.form = new FormPanel();
        this.form.setHeaderVisible(false);
        this.form.setBodyBorder(false);
        this.form.setScrollMode(Scroll.AUTOY);

        initFields();
        initButtons();

        this.window.add(form);
    }

    @Override
    protected void initialize() {
        super.initialize();

        this.window = new CenteredWindow();
        this.window.setHeading("Manage data sharing");
        this.window.setLayout(new FitLayout());
        this.window.setSize(323, 200);

        initForm();
    }

    private void onComplete(AppEvent event) {

        String info = "";
        if (this.sensors.size() > 1) {
            info = "The sensors were successfully shared with " + user.getValue() + ".";
        } else {
            info = "The sensor was successfully shared with " + user.getValue() + ".";
        }

        hideWindow();

        MessageBox.info(null, info, null);
    }

    private void onFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to update sharing settings, retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            onSubmit();
                        } else {
                            hideWindow();
                        }
                    }
                });
    }

    private void onShow(AppEvent event) {
        this.sensors = event.<List<SensorModel>> getData("sensors");
        List<TreeModel> users = Registry.<List<TreeModel>> get(Constants.REG_GROUPS);
        this.store.removeAll();
        this.store.add(users);

        this.window.show();
        this.window.center();
    }

    private void onSubmit() {
        final String user = this.user.getValue();
        final List<SensorModel> sensors = new ArrayList<SensorModel>(this.sensors);

        AppEvent event = new AppEvent(MySensorsEvents.ShareRequested);
        event.setData("user", user);
        event.setData("sensors", sensors);

        fireEvent(event);

        setBusy(true);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            this.createButton.setIcon(IconHelper.create(Constants.ICON_LOADING));
            this.cancelButton.disable();
        } else {
            this.createButton.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
            this.cancelButton.enable();
        }
    }

}
