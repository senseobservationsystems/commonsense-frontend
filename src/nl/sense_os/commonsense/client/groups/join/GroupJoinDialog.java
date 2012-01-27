package nl.sense_os.commonsense.client.groups.join;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.groups.join.forms.AllVisibleGroupsForm;
import nl.sense_os.commonsense.client.groups.join.forms.GroupNameForm;
import nl.sense_os.commonsense.client.groups.join.forms.GroupTypeForm;
import nl.sense_os.commonsense.client.groups.join.forms.ShareSensorsForm;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public class GroupJoinDialog extends Window {

    class States {
        public static final int GROUP_TYPE_CHOICE = 0;
        public static final int ALL_VISIBLE_GROUPS = 1;
        public static final int GROUP_NAME = 2;
        public static final int SHARE_SENSORS = 3;
    }

    private static final Logger LOG = Logger.getLogger(GroupJoinDialog.class.getName());

    private CardLayout layout;
    private GroupTypeForm frmGroupType;
    private AllVisibleGroupsForm frmAllVisibleGroups;
    private GroupNameForm frmGroupName;
    private ShareSensorsForm frmShareSensors;
    private Button btnNext;
    private Button btnBack;
    private Button btnCancel;
    private FormButtonBinding buttonBinding;
    private int state;

    public GroupJoinDialog(PagingLoader<PagingLoadResult<GroupModel>> groupLoader,
            List<SensorModel> sensorLibrary) {
        LOG.setLevel(Level.ALL);

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
        frmAllVisibleGroups = new AllVisibleGroupsForm(groupLoader);
        add(frmAllVisibleGroups);
        frmGroupName = new GroupNameForm();
        add(frmGroupName);
        frmShareSensors = new ShareSensorsForm(new ArrayList<String>(), new ArrayList<String>());
        add(frmShareSensors);

        initButtons();

        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                center();
            }
        });
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

    public GroupModel getGroup() {
        return frmAllVisibleGroups.getGroup();
    }

    public String getGroupType() {
        return frmGroupType.getType();
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

    public void setReqSensors(List<String> sensorNames) {
        frmShareSensors.setReqSensors(sensorNames);
    }

    public void setWizardState(int state) {
        this.state = state;

        switch (state) {
            case States.GROUP_NAME :
                showGroupNameForm();
                break;
            case States.GROUP_TYPE_CHOICE :
                showGroupTypeChoice();
                break;
            case States.ALL_VISIBLE_GROUPS :
                showAllGroupsList();
                break;
            case States.SHARE_SENSORS :
                showShareSensors();
                break;
            default :
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
