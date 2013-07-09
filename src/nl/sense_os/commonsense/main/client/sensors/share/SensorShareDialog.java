package nl.sense_os.commonsense.main.client.sensors.share;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

import com.extjs.gxt.ui.client.Registry;
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
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class SensorShareDialog extends View {

	private static final Logger LOG = Logger.getLogger(SensorShareDialog.class.getName());
	private Window window;
	private FormPanel form;
	private TextField<String> user;
	private Button createButton;
	private Button cancelButton;
	private List<ExtSensor> sensors;

	private ListStore<TreeModel> store;

	public SensorShareDialog(Controller c) {
		super(c);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(SensorShareEvents.ShowShareDialog)) {
			// LOG.fine( "Show");
			onShow(event);

		} else if (type.equals(SensorShareEvents.ShareCancelled)) {
			// LOG.fine( "Cancelled");
			hideWindow();

		} else if (type.equals(SensorShareEvents.ShareComplete)) {
			// LOG.fine( "Complete");
			onComplete(event);

		} else if (type.equals(SensorShareEvents.ShareFailed)) {
			LOG.warning("Failed");
			onFailed(event);

		} else {
			LOG.warning("Unexpected event type: " + type);
		}
	}

	private void hideWindow() {
		window.hide();
		form.reset();
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
					Dispatcher.forwardEvent(SensorShareEvents.ShareCancelled);
				} else {
					LOG.warning("Unexpected button pressed");
				}
			}
		};

		createButton = new Button("Share", l);
		createButton.setIconStyle("sense-btn-icon-go");
		cancelButton = new Button("Cancel", l);

		final FormButtonBinding binding = new FormButtonBinding(form);
		binding.addButton(createButton);

		form.addButton(createButton);
		form.addButton(cancelButton);
	}

	private void initFields() {

		final FormData formData = new FormData("-10");

		store = new ListStore<TreeModel>();

		user = new TextField<String>();
		user.setFieldLabel("Share with");
		user.setEmptyText("Enter a username...");
		user.setAllowBlank(false);

		form.add(user, formData);
	}

	private void initForm() {
		form = new FormPanel();
		form.setHeaderVisible(false);
		form.setBodyBorder(false);
		form.setScrollMode(Scroll.AUTOY);

		initFields();
		initButtons();

		window.add(form);
	}

	@Override
	protected void initialize() {
		super.initialize();

		window = new CenteredWindow();
        window.setHeadingText("Manage data sharing");
		window.setLayout(new FitLayout());
		window.setSize(323, 200);

		initForm();
	}

	private void onComplete(AppEvent event) {

		String info = "";
		if (sensors.size() > 1) {
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
                        if (be.getButtonClicked().getHtml().equalsIgnoreCase("yes")) {
							onSubmit();
						} else {
							hideWindow();
						}
					}
				});
	}

	private void onShow(AppEvent event) {
		sensors = event.<List<ExtSensor>> getData("sensors");
		List<TreeModel> users = Registry
				.<List<TreeModel>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_GROUPS);
		store.removeAll();
		store.add(users);

		window.show();
		window.center();
	}

	private void onSubmit() {
		final String user = this.user.getValue();
		final List<ExtSensor> sensors = new ArrayList<ExtSensor>(this.sensors);

		AppEvent event = new AppEvent(SensorShareEvents.ShareRequest);
		event.setData("user", user);
		event.setData("sensors", sensors);

		fireEvent(event);

		setBusy(true);
	}

	private void setBusy(boolean busy) {
		if (busy) {
			createButton.setIconStyle("sense-btn-icon-loading");
			cancelButton.disable();
		} else {
			createButton.setIconStyle("sense-btn-icon-go");
			cancelButton.enable();
		}
	}

}
