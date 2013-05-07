package nl.sense_os.commonsense.main.client.groups.leave;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GroupLeaveDialog extends View {

	private static final Logger LOG = Logger.getLogger(GroupLeaveController.class.getName());
	private GxtGroup group;

	public GroupLeaveDialog(Controller c) {
		super(c);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		EventType type = event.getType();
		if (type.equals(GroupLeaveEvents.LeaveRequest)) {
			LOG.finest("LeaveRequest");
			GxtGroup group = event.getData("group");
			show(group);

		} else if (type.equals(GroupLeaveEvents.LeaveComplete)) {
			LOG.finest("LeaveComplete");
			onLeaveComplete();

		} else if (type.equals(GroupLeaveEvents.LeaveFailed)) {
			LOG.finest("LeaveFailed");
			onLeaveFailed();

		} else {
			LOG.warning("Unexpected event: " + event);
		}
	}

	private void leave() {
		AppEvent event = new AppEvent(GroupLeaveEvents.Leave);
		event.setData("group", group);
		event.setSource(this);
		Dispatcher.forwardEvent(event);
	}

	private void onLeaveComplete() {
		// nothing to do
	}

	private void onLeaveFailed() {
		MessageBox.confirm(null, "Failed to leave the group, retry?",
				new Listener<MessageBoxEvent>() {

					@Override
					public void handleEvent(MessageBoxEvent be) {
						Button clicked = be.getButtonClicked();
                        if ("yes".equalsIgnoreCase(clicked.getHtml())) {
							leave();
						}
					}
				});
	}

	private void show(GxtGroup group) {

		this.group = group;

		MessageBox.confirm(null, "Are you sure you want to leave the group '" + group.getName()
				+ "'?", new Listener<MessageBoxEvent>() {

			@Override
			public void handleEvent(MessageBoxEvent be) {
				Button clicked = be.getButtonClicked();
                if ("yes".equalsIgnoreCase(clicked.getHtml())) {
					leave();
				}
			}
		});
	}
}
