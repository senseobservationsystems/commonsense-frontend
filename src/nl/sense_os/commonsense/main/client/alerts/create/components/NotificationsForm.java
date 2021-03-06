package nl.sense_os.commonsense.main.client.alerts.create.components;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.main.client.alerts.create.triggers.Notification;
import nl.sense_os.commonsense.main.client.ext.component.WizardFormPanel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class NotificationsForm extends WizardFormPanel {

	private final List<NotificationFields> forms = new ArrayList<NotificationFields>();

	public NotificationsForm() {
		addFieldSet();
	}

	private void addFieldSet() {

		final NotificationFields fields = new NotificationFields();

		forms.add(fields);

		fields.getBtnAdd().addListener(Events.Select, new Listener<ButtonEvent>() {

			@Override
			public void handleEvent(ButtonEvent be) {
				addFieldSet();
				layout();
			}
		});
		fields.getBtnRemove().addListener(Events.Select, new Listener<ButtonEvent>() {

			@Override
			public void handleEvent(ButtonEvent be) {
				removeFieldSet(fields);
				layout();
			}
		});

		add(fields, new FormData("-10"));
	}

	private void removeFieldSet(NotificationFields fieldSet) {
		forms.remove(fieldSet);
		remove(fieldSet);
	}

	public ArrayList<Notification> getNotifications() {
		// TODO
		return null;
	}
}
