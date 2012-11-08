package nl.sense_os.commonsense.main.client.groupmanagement.joining.component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView;
import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class GxtGroupJoinDialog extends CenteredWindow implements GroupJoinView {

    public class States {
        public static final int GROUP_TYPE_CHOICE = 0;
        public static final int ALL_VISIBLE_GROUPS = 1;
        public static final int GROUP_NAME = 2;
        public static final int SHARE_SENSORS = 3;
        public static final int MEMBER_RIGHTS = 4;
    }

    private static final Logger LOG = Logger.getLogger(GxtGroupJoinDialog.class.getName());

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

    private Presenter presenter;

    public GxtGroupJoinDialog(List<GxtGroup> groups, List<GxtSensor> sensors) {

        // main window properties
        setHeading("Join a public group");
        setClosable(false);
        setSize(540, 480);

        // set card layout
        layout = new CardLayout();
        setLayout(layout);

        // init individual subforms
        frmGroupType = new GroupTypeForm();
        add(frmGroupType);
        frmAllVisibleGroups = new AllVisibleGroupsForm(groups);
        add(frmAllVisibleGroups);
        frmGroupName = new GroupNameForm();
        add(frmGroupName);
        frmShareSensors = new ShareSensorsForm(new ArrayList<String>(), new ArrayList<String>(),
                sensors);
        add(frmShareSensors);
        frmMemberRights = new MemberRightsForm();
        add(frmMemberRights);

        initButtons();

        addListeners();

        // initialize
        setWizardState(States.GROUP_TYPE_CHOICE);
    }

    private void addListeners() {

        btnNext.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (ce.getButton().getText().equalsIgnoreCase("next")) {
                    goToNext();
                } else {
                    if (null != presenter) {
                        presenter.onSubmitClick();
                    }
                }
            }
        });
        btnBack.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                goToPrev();
            }
        });
        btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (null != presenter) {
                    presenter.onCancelClick();
                }
            }
        });

    }

    @Override
    public GxtGroup getGroup() {
        return frmAllVisibleGroups.getGroup();
    }

    private String getGroupType() {
        return frmGroupType.getType();
    }

    @Override
    public List<GxtSensor> getSharedSensors() {
        return frmShareSensors.getSharedSensors();
    }

    private void goToNext() {
        switch (state) {
        case States.GROUP_TYPE_CHOICE:
            if (getGroupType().equals("visible")) {
                setWizardState(States.ALL_VISIBLE_GROUPS);
            } else {
                setWizardState(States.GROUP_NAME);
            }
            break;
        case States.ALL_VISIBLE_GROUPS:
            if (null != presenter) {
                presenter.onGroupDetailsRequest(getGroup());
            }
            break;
        case States.SHARE_SENSORS:
            setWizardState(States.MEMBER_RIGHTS);
            break;
        case States.GROUP_NAME:
            // TODO
            break;
        default:
            LOG.warning("Unable to go to next state!");
        }
    }

    private void goToPrev() {
        switch (state) {
        case States.GROUP_TYPE_CHOICE:
            // should never happen
            break;
        case States.ALL_VISIBLE_GROUPS:
            setWizardState(States.GROUP_TYPE_CHOICE);
            break;
        case States.SHARE_SENSORS:
            if (getGroupType().equals("visible")) {
                setWizardState(States.ALL_VISIBLE_GROUPS);
            } else {
                setWizardState(States.GROUP_NAME);
            }
            break;
        case States.GROUP_NAME:
            setWizardState(States.GROUP_TYPE_CHOICE);
            break;
        case States.MEMBER_RIGHTS:
            setWizardState(States.SHARE_SENSORS);
            break;
        default:
            LOG.warning("Unable to go to previous state!");
        }
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

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            btnNext.setIconStyle("sense-btn-icon-loading");
            btnNext.disable();
        } else {
            btnNext.setIconStyle("sense-btn-icon-go");
            btnNext.enable();
        }
    }

    @Override
    public void setGroupDetails(GxtGroup group) {

        setReqSensors(group.getReqSensors());
        setMemberRights(group.isAllowAddUsers(), group.isAllowAddSensors(),
                group.isAllowListUsers(), group.isAllowListSensors(), group.isAllowRemoveUsers(),
                group.isAllowRemoveSensors());
        setWizardState(States.SHARE_SENSORS);

    }

    private void setMemberRights(boolean createMembers, boolean createSensors, boolean readMembers,
            boolean readSensors, boolean deleteMembers, boolean deleteSensors) {
        frmMemberRights.setMemberRights(createMembers, createSensors, readMembers, readSensors,
                deleteMembers, deleteSensors);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private void setReqSensors(List<String> sensorNames) {
        frmShareSensors.setReqSensors(sensorNames);
    }

    private void setWizardState(int state) {
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
