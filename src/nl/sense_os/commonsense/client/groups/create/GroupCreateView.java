package nl.sense_os.commonsense.client.groups.create;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.utility.Md5Hasher;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.groups.create.forms.GroupAccessMgtForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupLoginForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupMemberRightsForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupNameForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupPresetsForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupReqSharingForm;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class GroupCreateView extends View {

    private static final Logger LOG = Logger.getLogger(GroupCreateView.class.getName());
    private Window window;
    private CardLayout layout;
    private Button nextButton;
    private Button backButton;
    private GroupNameForm nameForm;
    private GroupPresetsForm presetsForm;
    private GroupAccessMgtForm accessMgmtForm;
    private GroupReqSharingForm reqSharingForm;
    private FormButtonBinding formButtonBinding;
    private GroupMemberRightsForm memberRightsForm;
    private GroupLoginForm groupLoginForm;

    public GroupCreateView(Controller c) {
        super(c);
        LOG.setLevel(Level.ALL);
    }

    private void goToNext() {

        Component active = layout.getActiveItem();
        if (active.equals(nameForm)) {
            showPresets();
        } else if (active.equals(presetsForm)) {
            showAccessMgmt();
        } else if (active.equals(accessMgmtForm)) {
            showReqSharing();
        } else if (active.equals(reqSharingForm)) {
            showMemberRights();
        } else if (active.equals(memberRightsForm)) {
            showGroupLogin();
        } else {
            LOG.warning("Cannot go to next: unexpected active item");
        }
    }

    private void goToPrev() {

        Component active = layout.getActiveItem();
        if (active.equals(groupLoginForm)) {
            showMemberRights();
        } else if (active.equals(memberRightsForm)) {
            showReqSharing();
        } else if (active.equals(reqSharingForm)) {
            showAccessMgmt();
        } else if (active.equals(accessMgmtForm)) {
            showPresets();
        } else if (active.equals(presetsForm)) {
            showNameForm();
        } else {
            LOG.warning("Cannot go to previous: unexpected active item");
        }
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(GroupCreateEvents.ShowCreator)) {
            LOG.finest("ShowCreator");
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

        window.hide();
        setBusy(false);
    }

    private void initButtons() {

        // listener for clicks on the buttons
        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final Button pressed = ce.getButton();
                if (pressed.equals(nextButton)) {
                    if (nextButton.getText().equals("Next")) {
                        goToNext();
                    } else {
                        submit();
                    }
                } else if (pressed.equals(backButton)) {
                    goToPrev();
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };

        nextButton = new Button("Next", l);
        backButton = new Button("Back", l);

        window.addButton(backButton);
        window.addButton(nextButton);
    }
    private void initForms() {

        nameForm = new GroupNameForm();
        presetsForm = new GroupPresetsForm();
        accessMgmtForm = new GroupAccessMgtForm();
        reqSharingForm = new GroupReqSharingForm();
        memberRightsForm = new GroupMemberRightsForm();
        groupLoginForm = new GroupLoginForm();

        window.add(nameForm);
        window.add(presetsForm);
        window.add(accessMgmtForm);
        window.add(reqSharingForm);
        window.add(memberRightsForm);
        window.add(groupLoginForm);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    private void onComplete() {
        hideDialog();
    }

    private void onFailed() {

        setBusy(false);
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
        window = new CenteredWindow();
        window.setHeading("Create new group");
        window.setSize(500, 450);

        layout = new CardLayout();
        window.setLayout(layout);

        initForms();
        initButtons();

        showNameForm();

        window.show();
    }

    public void setBusy(boolean busy) {
        if (busy) {
            window.setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            window.setIcon(IconHelper.create(""));
        }
        nextButton.setEnabled(!busy);
        backButton.setEnabled(!busy);
    }

    private void showAccessMgmt() {

        // update content
        layout.setActiveItem(accessMgmtForm);

        // update buttons
        nextButton.setText("Next");
        backButton.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(accessMgmtForm);
        formButtonBinding.addButton(nextButton);
    }

    private void showGroupLogin() {

        // update content
        layout.setActiveItem(groupLoginForm);

        // update buttons
        nextButton.setText("Create");
        backButton.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(groupLoginForm);
        formButtonBinding.addButton(nextButton);
    }

    private void showMemberRights() {

        // update content
        layout.setActiveItem(memberRightsForm);

        // update buttons
        nextButton.setText("Next");
        backButton.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(memberRightsForm);
        formButtonBinding.addButton(nextButton);
    }

    private void showNameForm() {
        // update layout
        layout.setActiveItem(nameForm);

        // update button
        nextButton.setText("Next");
        backButton.setEnabled(false);

        // keep button updated
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(nameForm);
        formButtonBinding.addButton(nextButton);
    }

    private void showPresets() {

        // update layout
        layout.setActiveItem(presetsForm);

        // update button
        Radio selected = presetsForm.getPresets().getValue();
        if (selected instanceof GroupPresetsForm.CustomRadio) {
            nextButton.setText("Next");
        } else {
            nextButton.setText("Create");
        }
        backButton.setEnabled(true);

        // listen to selection to update the button
        RadioGroup presets = presetsForm.getPresets();
        presets.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Radio selected = ((RadioGroup) be.getField()).getValue();
                if (selected instanceof GroupPresetsForm.CustomRadio) {
                    nextButton.setText("Next");
                } else {
                    nextButton.setText("Create");
                }
            }
        });

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(presetsForm);
        formButtonBinding.addButton(nextButton);
    }

    private void showReqSharing() {

        // update content
        layout.setActiveItem(reqSharingForm);

        // update buttons
        nextButton.setText("Next");
        backButton.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(reqSharingForm);
        formButtonBinding.addButton(nextButton);
    }

    private void submit() {
        setBusy(true);

        GroupModel group = new GroupModel();
        group.setName(nameForm.getNameValue());
        group.setDescription(nameForm.getDescriptionValue());

        Radio preset = presetsForm.getPresets().getValue();
        if (preset instanceof GroupPresetsForm.PrivateRadio) {
            LOG.fine("private preset");
            group.setHidden(true);
            group.setPublic(false);

            String clearPass = presetsForm.getPrivatePass();
            String hashedPass = Md5Hasher.hash(clearPass);
            group.setAccessPassword(hashedPass);

        } else if (preset instanceof GroupPresetsForm.AnonymousRadio) {
            LOG.fine("anonymous preset");
            group.setHidden(false);
            group.setPublic(true);
            group.setAnonymous(true);

        } else if (preset instanceof GroupPresetsForm.CommunityRadio) {
            LOG.fine("community preset");
            group.setHidden(false);
            group.setPublic(true);
            group.setAnonymous(false);

        } else if (preset instanceof GroupPresetsForm.CustomRadio) {
            LOG.fine("custom preset");

            // first form: visiblity and join policy
            boolean isHidden = accessMgmtForm.getVisibility().getValue() instanceof GroupAccessMgtForm.InvisibleRadio;
            boolean isPublic = accessMgmtForm.getJoinPolicy().getValue() instanceof GroupAccessMgtForm.FreeEntranceRadio;
            group.setHidden(isHidden);
            group.setPublic(isPublic);
            if (accessMgmtForm.getJoinPolicy().getValue() instanceof GroupAccessMgtForm.PwEntranceRadio) {
                String clearPass = accessMgmtForm.getPassword();
                String hashedPass = Md5Hasher.hash(clearPass);
                group.setAccessPassword(hashedPass);
            }

            // second form: required sharing
            String reqSensors = reqSharingForm.getReqSensors();
            if (null != reqSensors && reqSensors.length() > 0) {
                group.setReqSensors(Arrays.asList(reqSensors.split(", ")));
            }
            String optSensors = reqSharingForm.getOptSensors();
            if (null != optSensors && optSensors.length() > 0) {
                group.setOptSensors(Arrays.asList(optSensors.split(", ")));
            }
            group.setShowIdReq(reqSharingForm.isUserIdRequired());
            group.setShowUsernameReq(reqSharingForm.isUsernameRequired());
            group.setShowFirstNameReq(reqSharingForm.isFirstNameRequired());
            group.setShowSurnameReq(reqSharingForm.isSurnameRequired());
            group.setShowEmailReq(reqSharingForm.isEmailRequired());
            group.setShowPhoneReq(reqSharingForm.isPhoneRequired());

            // third form: member rights
            group.setAllowListSensors(memberRightsForm.isReadSensors());
            group.setAllowAddSensors(memberRightsForm.isCreateSensors());
            group.setAllowRemoveSensors(memberRightsForm.isDeleteSensors());
            group.setAllowListUsers(memberRightsForm.isReadMembers());
            group.setAllowAddUsers(memberRightsForm.isCreateMembers());
            group.setAllowRemoveUsers(memberRightsForm.isDeleteMembers());

            // fourth form: group username
            if (groupLoginForm.getRadios().getValue() instanceof GroupLoginForm.UseLoginRadio) {
                group.setUsername(groupLoginForm.getLogin());
                String clearPass = groupLoginForm.getPassword();
                String hashedPass = Md5Hasher.hash(clearPass);
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
