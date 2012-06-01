package nl.sense_os.commonsense.client.groups.create.components;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class GroupCreator extends CenteredWindow {

    private static final Logger LOG = Logger.getLogger(GroupCreator.class.getName());

    private CardLayout layout;

    private GroupNameForm frmGroupName;
    private GroupPresetsForm frmPresets;
    private GroupAccessMgtForm frmAccessMgmt;
    private GroupReqSharingForm frmReqSharing;
    private GroupMemberRightsForm frmMemberRights;
    private GroupLoginForm frmGroupLogin;

    private Button btnNext;
    private Button btnBack;
    private Button btnCancel;
    private FormButtonBinding formButtonBinding;

    public GroupCreator() {
        super();

        // LOG.setLevel(Level.ALL);

        // basic stuff
        setHeading("Create new group");
        setClosable(false);
        setSize(500, 450);

        // card layout
        layout = new CardLayout();
        setLayout(layout);

        // forms for window content
        frmGroupName = new GroupNameForm();
        frmPresets = new GroupPresetsForm();
        frmAccessMgmt = new GroupAccessMgtForm();
        frmReqSharing = new GroupReqSharingForm();
        frmMemberRights = new GroupMemberRightsForm();
        frmGroupLogin = new GroupLoginForm();
        add(frmGroupName);
        add(frmPresets);
        add(frmAccessMgmt);
        add(frmReqSharing);
        add(frmMemberRights);
        add(frmGroupLogin);

        // buttons
        btnBack = new Button("Back");
        btnNext = new Button("Next");
        btnNext.setIconStyle("sense-btn-icon-go");
        btnCancel = new Button("Cancel");
        addButton(btnBack);
        addButton(btnNext);
        addButton(btnCancel);

        // initialize
        showNameForm();
    }

    public String getAccessPassword() {
        String pass = frmPresets.getAccessPassword();
        if (null == pass) {
            pass = frmAccessMgmt.getPassword();
        }
        return pass;
    }

    public Button getBtnBack() {
        return btnBack;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public Button getBtnNext() {
        return btnNext;
    }

    public String getGroupDescription() {
        return frmGroupName.getDescriptionValue();
    }

    public String getGroupName() {
        return frmGroupName.getNameValue();
    }

    public List<String> getOptSensors() {
        String reqSensors = frmReqSharing.getOptSensors();
        if (null != reqSensors && reqSensors.length() > 0) {
            return Arrays.asList(reqSensors.split(", "));
        } else {
            return null;
        }
    }

    public String getPresetChoice() {
        return frmPresets.getPresetChoice();
    }

    public List<String> getReqSensors() {
        String reqSensors = frmReqSharing.getReqSensors();
        if (null != reqSensors && reqSensors.length() > 0) {
            return Arrays.asList(reqSensors.split(", "));
        } else {
            return null;
        }
    }

    public String getGroupUsername() {
        return frmGroupLogin.getLogin();
    }

    public String getGroupPassword() {
        return frmGroupLogin.getPassword();
    }

    public void goToNext() {

        Component active = layout.getActiveItem();
        if (active.equals(frmGroupName)) {
            showPresets();
        } else if (active.equals(frmPresets)) {
            showAccessMgmt();
        } else if (active.equals(frmAccessMgmt)) {
            showReqSharing();
        } else if (active.equals(frmReqSharing)) {
            showMemberRights();
        } else if (active.equals(frmMemberRights)) {
            showGroupLogin();
        } else {
            LOG.warning("Cannot go to next: unexpected active item");
        }
    }

    public void goToPrev() {

        Component active = layout.getActiveItem();
        if (active.equals(frmGroupLogin)) {
            showMemberRights();
        } else if (active.equals(frmMemberRights)) {
            showReqSharing();
        } else if (active.equals(frmReqSharing)) {
            showAccessMgmt();
        } else if (active.equals(frmAccessMgmt)) {
            showPresets();
        } else if (active.equals(frmPresets)) {
            showNameForm();
        } else {
            LOG.warning("Cannot go to previous: unexpected active item");
        }
    }

    public boolean isCreateMembers() {
        return frmMemberRights.isCreateMembers();
    }

    public boolean isCreateSensors() {
        return frmMemberRights.isCreateSensors();
    }

    public boolean isDeleteMembers() {
        return frmMemberRights.isDeleteMembers();
    }

    public boolean isDeleteSensors() {
        return frmMemberRights.isDeleteSensors();
    }

    public boolean isEmailRequired() {
        return frmReqSharing.isEmailRequired();
    }

    public boolean isFirstNameRequired() {
        return frmReqSharing.isFirstNameRequired();
    }

    public boolean isGroupAnonymous() {
        return frmReqSharing.isGroupAnonymous();
    }

    public boolean isGroupHidden() {
        return frmAccessMgmt.isGroupHidden();
    }

    public boolean isGroupPublic() {
        return frmAccessMgmt.isGroupPublic();
    }

    public boolean isGroupLogin() {
        return frmGroupLogin.isGroupLogin();
    }

    public boolean isPhoneRequired() {
        return frmReqSharing.isPhoneRequired();
    }

    public boolean isReadMembers() {
        return frmMemberRights.isReadMembers();
    }

    public boolean isReadSensors() {
        return frmMemberRights.isReadSensors();
    }

    public boolean isSurnameRequired() {
        return frmReqSharing.isSurnameRequired();
    }

    public boolean isUserIdRequired() {
        return frmReqSharing.isUserIdRequired();
    }

    public boolean isUsernameRequired() {
        return frmReqSharing.isUsernameRequired();
    }

    public void setBusy(boolean busy) {
        if (busy) {
            btnNext.setIconStyle("sense-btn-icon-loading");
        } else {
            btnNext.setIconStyle("sense-btn-icon=go");
        }
        btnNext.setEnabled(!busy);
        btnBack.setEnabled(!busy);
    }

    private void showAccessMgmt() {

        // update content
        layout.setActiveItem(frmAccessMgmt);

        // update buttons
        btnNext.setText("Next");
        btnBack.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(frmAccessMgmt);
        formButtonBinding.addButton(btnNext);
    }

    private void showGroupLogin() {

        // update content
        layout.setActiveItem(frmGroupLogin);

        // update buttons
        btnNext.setText("Create");
        btnBack.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(frmGroupLogin);
        formButtonBinding.addButton(btnNext);
    }

    private void showMemberRights() {

        // update content
        layout.setActiveItem(frmMemberRights);

        // update buttons
        btnNext.setText("Next");
        btnBack.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(frmMemberRights);
        formButtonBinding.addButton(btnNext);
    }

    private void showNameForm() {
        // update layout
        layout.setActiveItem(frmGroupName);

        // update button
        btnNext.setText("Next");
        btnBack.setEnabled(false);

        // keep button updated
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(frmGroupName);
        formButtonBinding.addButton(btnNext);
    }

    private void showPresets() {

        // update layout
        layout.setActiveItem(frmPresets);

        // update button
        String selected = frmPresets.getPresetChoice();
        if ("custom".equals(selected)) {
            btnNext.setText("Next");
        } else {
            btnNext.setText("Create");
        }
        btnBack.setEnabled(true);

        LOG.fine("Show presets form. Selected preset: " + selected);

        // listen to selection to update the button
        RadioGroup presets = frmPresets.getPresets();
        presets.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                String selected = frmPresets.getPresetChoice();
                LOG.fine("Preset changed! Selected value: " + selected);
                if ("custom".equals(selected)) {
                    btnNext.setText("Next");
                } else {
                    btnNext.setText("Create");
                }
            }
        });

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(frmPresets);
        formButtonBinding.addButton(btnNext);
    }

    private void showReqSharing() {

        // update content
        layout.setActiveItem(frmReqSharing);

        // update buttons
        btnNext.setText("Next");
        btnBack.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(btnNext);
        }
        formButtonBinding = new FormButtonBinding(frmReqSharing);
        formButtonBinding.addButton(btnNext);
    }
}
