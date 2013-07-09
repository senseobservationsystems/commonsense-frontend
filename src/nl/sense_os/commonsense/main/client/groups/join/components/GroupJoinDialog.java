package nl.sense_os.commonsense.main.client.groups.join.components;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.ext.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.ext.model.ExtGroup;
import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class GroupJoinDialog extends CenteredWindow {

	public class States {
		public static final int GROUP_TYPE_CHOICE = 0;
		public static final int ALL_VISIBLE_GROUPS = 1;
		public static final int GROUP_NAME = 2;
		public static final int SHARE_SENSORS = 3;
		public static final int MEMBER_RIGHTS = 4;
	}

	private static final Logger LOG = Logger.getLogger(GroupJoinDialog.class.getName());

	private CardLayout layout;
	private GroupTypeForm frmGroupType;
	private AllVisibleGroupsForm frmAllVisibleGroups;
	private GroupNameForm frmGroupName;
	private ShareSensorsForm frmShareSensors;
	private MemberRightsForm frmMemberRights;
	private Button btnNext;
	private Button btnBack;
	private Button btnCancel;
	private FormButtonBinding buttonBinding;
	private int state;

	public GroupJoinDialog(PagingLoader<PagingLoadResult<ExtGroup>> groupLoader,
			List<ExtSensor> sensors) {

		// main window properties
        setHeadingText("Join a public group");
		setClosable(false);
		setSize(540, 480);

		// set card layout
		layout = new CardLayout();
		setLayout(layout);

		// init individual subforms
		frmGroupType = new GroupTypeForm();
		add(frmGroupType);
		frmAllVisibleGroups = new AllVisibleGroupsForm(groupLoader);
		add(frmAllVisibleGroups);
		frmGroupName = new GroupNameForm();
		add(frmGroupName);
		frmShareSensors = new ShareSensorsForm(new ArrayList<String>(), new ArrayList<String>(),
				sensors);
		add(frmShareSensors);
		frmMemberRights = new MemberRightsForm();
		add(frmMemberRights);

		initButtons();

		// initialize
		setWizardState(States.GROUP_TYPE_CHOICE);
	}

	public Button getBtnBack() {
		return btnBack;
	}

	public Button getBtnCancel() {
		return btnCancel;
	}

	public Button getBtnSubmit() {
		return btnNext;
	}

	public ExtGroup getGroup() {
		return frmAllVisibleGroups.getGroup();
	}

	public String getGroupType() {
		return frmGroupType.getType();
	}

	public List<ExtSensor> getSharedSensors() {
		return frmShareSensors.getSharedSensors();
	}

	public int getWizardState() {
		return state;
	}

	private void initButtons() {

		btnNext = new Button("Next");
		btnNext.setIconStyle("sense-btn-icon-go");

		btnBack = new Button("Back");
		btnBack.setEnabled(false);

		btnCancel = new Button("Cancel");

		addButton(btnBack);
		addButton(btnNext);
		addButton(btnCancel);
	}

	public void setBusy(boolean busy) {
		if (busy) {
			btnNext.setIconStyle("sense-btn-icon-loading");
			btnNext.disable();
		} else {
			btnNext.setIconStyle("sense-btn-icon-go");
			btnNext.enable();
		}
	}

	public void setMemberRights(boolean createMembers, boolean createSensors, boolean readMembers,
			boolean readSensors, boolean deleteMembers, boolean deleteSensors) {
		frmMemberRights.setMemberRights(createMembers, createSensors, readMembers, readSensors,
				deleteMembers, deleteSensors);
	}

	public void setReqSensors(List<String> sensorNames) {
		frmShareSensors.setReqSensors(sensorNames);
	}

	public void setWizardState(int state) {
		this.state = state;

		switch (state) {
		case States.GROUP_NAME:
			showGroupNameForm();
			break;
		case States.GROUP_TYPE_CHOICE:
			showGroupTypeChoice();
			break;
		case States.ALL_VISIBLE_GROUPS:
			showAllGroupsList();
			break;
		case States.SHARE_SENSORS:
			showShareSensors();
			break;
		case States.MEMBER_RIGHTS:
			showMemberRights();
			break;
		default:
			LOG.warning("Unexpected new state: " + state);
		}
	}

	private void showAllGroupsList() {
		// update active item
		layout.setActiveItem(frmAllVisibleGroups);

		// update buttons
		btnNext.setText("Next");
		btnBack.setEnabled(true);

		if (null != buttonBinding) {
			buttonBinding.removeButton(btnNext);
		}
		buttonBinding = new FormButtonBinding(frmAllVisibleGroups);
		buttonBinding.addButton(btnNext);
	}

	private void showGroupNameForm() {
		// update active item
		layout.setActiveItem(frmGroupName);

		// update buttons
		btnNext.setText("Next");
		btnBack.setEnabled(true);

		if (null != buttonBinding) {
			buttonBinding.removeButton(btnNext);
		}
		buttonBinding = new FormButtonBinding(frmGroupName);
		buttonBinding.addButton(btnNext);
	}

	private void showGroupTypeChoice() {
		// update active item
		layout.setActiveItem(frmGroupType);

		// update buttons
		btnNext.setText("Next");
		btnBack.setEnabled(false);

		if (null != buttonBinding) {
			buttonBinding.removeButton(btnNext);
		}
		buttonBinding = new FormButtonBinding(frmGroupType);
		buttonBinding.addButton(btnNext);
	}

	private void showMemberRights() {
		// update active item
		layout.setActiveItem(frmMemberRights);

		// update buttons
		btnNext.setText("Finish");
		btnBack.setEnabled(true);

		if (null != buttonBinding) {
			buttonBinding.removeButton(btnNext);
		}
		buttonBinding = new FormButtonBinding(frmMemberRights);
		buttonBinding.addButton(btnNext);
	}

	private void showShareSensors() {
		// update active item
		layout.setActiveItem(frmShareSensors);

		// update buttons
		btnNext.setText("Next");
		btnBack.setEnabled(true);

		if (null != buttonBinding) {
			buttonBinding.removeButton(btnNext);
		}
		buttonBinding = new FormButtonBinding(frmShareSensors);
		buttonBinding.addButton(btnNext);
	}
}
