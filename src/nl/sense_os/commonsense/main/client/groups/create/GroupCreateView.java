package nl.sense_os.commonsense.main.client.groups.create;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.GroupModel;
import nl.sense_os.commonsense.common.client.util.Md5Hasher;
import nl.sense_os.commonsense.main.client.groups.create.components.GroupCreator;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GroupCreateView extends View {

	private static final Logger LOG = Logger.getLogger(GroupCreateView.class.getName());
	private GroupCreator window;

	public GroupCreateView(Controller c) {
		super(c);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		final EventType type = event.getType();

		if (type.equals(GroupCreateEvents.ShowCreator)) {
			LOG.finest("NewCreator");
			onShow();

		} else if (type.equals(GroupCreateEvents.CreateComplete)) {
			LOG.finest("CreateGroupComplete");
			onComplete();

		} else if (type.equals(GroupCreateEvents.CreateFailed)) {
			LOG.warning("CreateGroupFailed");
			onFailed();

		} else {
			LOG.warning("Unexpected event type: " + type);
		}
	}

	private void hideDialog() {
		if (null == window) {
			LOG.warning("Window not found.");
			return;
		}

		window.setBusy(false);
		window.hide();
	}

	private void onComplete() {
		hideDialog();
	}

	private void onFailed() {

		window.setBusy(false);

		MessageBox.confirm(null, "Failed to create group, retry?", new Listener<MessageBoxEvent>() {

			@Override
			public void handleEvent(MessageBoxEvent be) {
				if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
					submit();
				} else {
					hideDialog();
				}
			}
		});
	}

	private void onShow() {
		window = new GroupCreator();
		window.show();

		window.getBtnNext().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Button b = ce.getButton();
				if (b.getText().equals("Next")) {
					window.goToNext();
				} else {
					submit();
				}
			}
		});

		window.getBtnBack().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				window.goToPrev();
			}
		});

		window.getBtnCancel().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				hideDialog();
			}
		});
	}

	private void submit() {
		window.setBusy(true);

		GroupModel group = new GroupModel();
		group.setName(window.getGroupName());
		group.setDescription(window.getGroupDescription());

		String preset = window.getPresetChoice();
		if ("private".equals(preset)) {
			LOG.fine("private preset");
			group.setHidden(true);
			group.setPublic(false);
			group.setAnonymous(false);

			String clearPass = window.getAccessPassword();
			String hashedPass = Md5Hasher.hash(clearPass);
			group.setAccessPassword(hashedPass);

		} else if ("anomymous".equals(preset)) {
			LOG.fine("anonymous preset");
			group.setHidden(false);
			group.setPublic(true);
			group.setAnonymous(true);

		} else if ("community".equals(preset)) {
			LOG.fine("community preset");
			group.setHidden(false);
			group.setPublic(true);
			group.setAnonymous(false);

		} else if ("custom".equals(preset)) {
			LOG.fine("custom preset");
			group.setHidden(window.isGroupHidden());
			group.setPublic(window.isGroupPublic());
			group.setAnonymous(window.isGroupAnonymous());

			String accessPass = window.getAccessPassword();
			if (null != accessPass) {
				String hashedPass = Md5Hasher.hash(accessPass);
				group.setAccessPassword(hashedPass);
			}
			group.setReqSensors(window.getReqSensors());
			group.setOptSensors(window.getOptSensors());
			if (!window.isGroupAnonymous()) {
				group.setShowIdReq(window.isUserIdRequired());
				group.setShowUsernameReq(window.isUsernameRequired());
				group.setShowFirstNameReq(window.isFirstNameRequired());
				group.setShowSurnameReq(window.isSurnameRequired());
				group.setShowEmailReq(window.isEmailRequired());
				group.setShowPhoneReq(window.isPhoneRequired());
			}

			// third form: member rights
			group.setAllowListSensors(window.isReadSensors());
			group.setAllowAddSensors(window.isCreateSensors());
			group.setAllowRemoveSensors(window.isDeleteSensors());
			group.setAllowListUsers(window.isReadMembers());
			group.setAllowAddUsers(window.isCreateMembers());
			group.setAllowRemoveUsers(window.isDeleteMembers());

			// fourth form: group username
			if (window.isGroupLogin()) {
				group.setUsername(window.getGroupUsername());
				String groupPass = window.getGroupPassword();
				String hashedPass = Md5Hasher.hash(groupPass);
				group.setPassword(hashedPass);
			}

		} else {
			LOG.warning("Unexpected group preset selection: " + preset);
		}

		AppEvent event = new GroupCreateRequest();
		event.setData("group", group);
		event.setSource(this);
		Dispatcher.forwardEvent(event);
	}
}
