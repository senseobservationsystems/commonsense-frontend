package nl.sense_os.commonsense.main.client.groupmanagement.creating.component;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.groupmanagement.creating.GroupCreatorView;
import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class GxtGroupCreatorForm extends CenteredWindow implements GroupCreatorView {

    private static final Logger LOG = Logger.getLogger(GxtGroupCreatorForm.class.getName());

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

    private Presenter presenter;

    public GxtGroupCreatorForm() {
        super();

        // LOG.setLevel(Level.ALL);

        // basic stuff
        setHeadingText("Create new group");
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

        addListeners();

        // initialize
        showNameForm();
    }

    private void addListeners() {
        btnNext.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button b = ce.getButton();
                if (b.getHtml().equals("Next")) {
                    goToNext();
                } else {
                    if (null != presenter) {
                        presenter.onCreateClick();
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

    @Override
    public String getGroupDescription() {
        return frmGroupName.getDescriptionValue();
    }

    @Override
    public String getGroupName() {
        return frmGroupName.getNameValue();
    }

    @Override
    public String getGroupPassword() {
        return frmGroupLogin.getPassword();
    }

    @Override
    public String getGroupUsername() {
        return frmGroupLogin.getLogin();
    }

    @Override
    public List<String> getOptSensors() {
        String reqSensors = frmReqSharing.getOptSensors();
        if (null != reqSensors && reqSensors.length() > 0) {
            return Arrays.asList(reqSensors.split(", "));
        } else {
            return null;
        }
    }

    @Override
    public String getPresetChoice() {
        return frmPresets.getPresetChoice();
    }

    @Override
    public List<String> getReqSensors() {
        String reqSensors = frmReqSharing.getReqSensors();
        if (null != reqSensors && reqSensors.length() > 0) {
            return Arrays.asList(reqSensors.split(", "));
        } else {
            return null;
        }
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

    @Override
    public boolean isCreateMembers() {
        return frmMemberRights.isCreateMembers();
    }

    @Override
    public boolean isCreateSensors() {
        return frmMemberRights.isCreateSensors();
    }

    @Override
    public boolean isDeleteMembers() {
        return frmMemberRights.isDeleteMembers();
    }

    @Override
    public boolean isDeleteSensors() {
        return frmMemberRights.isDeleteSensors();
    }

    @Override
    public boolean isEmailRequired() {
        return frmReqSharing.isEmailRequired();
    }

    @Override
    public boolean isFirstNameRequired() {
        return frmReqSharing.isFirstNameRequired();
    }

    @Override
    public boolean isGroupAnonymous() {
        return frmReqSharing.isGroupAnonymous();
    }

    @Override
    public boolean isGroupHidden() {
        return frmAccessMgmt.isGroupHidden();
    }

    @Override
    public boolean isGroupLogin() {
        return frmGroupLogin.isGroupLogin();
    }

    @Override
    public boolean isGroupPublic() {
        return frmAccessMgmt.isGroupPublic();
    }

    @Override
    public boolean isPhoneRequired() {
        return frmReqSharing.isPhoneRequired();
    }

    @Override
    public boolean isReadMembers() {
        return frmMemberRights.isReadMembers();
    }

    @Override
    public boolean isReadSensors() {
        return frmMemberRights.isReadSensors();
    }

    @Override
    public boolean isSurnameRequired() {
        return frmReqSharing.isSurnameRequired();
    }

    @Override
    public boolean isUserIdRequired() {
        return frmReqSharing.isUserIdRequired();
    }

    @Override
    public boolean isUsernameRequired() {
        return frmReqSharing.isUsernameRequired();
    }

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            btnNext.setIconStyle("sense-btn-icon-loading");
        } else {
            btnNext.setIconStyle("sense-btn-icon=go");
        }
        btnNext.setEnabled(!busy);
        btnBack.setEnabled(!busy);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
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
