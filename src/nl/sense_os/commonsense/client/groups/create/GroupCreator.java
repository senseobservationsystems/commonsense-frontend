package nl.sense_os.commonsense.client.groups.create;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.groups.create.forms.GroupAccessMgtForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupLoginForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupMemberRightsForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupNameForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupPresetsForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupReqMemberInfoForm;
import nl.sense_os.commonsense.client.groups.create.forms.GroupReqSharingForm;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

public class GroupCreator extends View {

    private static final Logger LOG = Logger.getLogger(GroupCreator.class.getName());
    private Window window;
    private CardLayout layout;
    private Button nextButton;
    private Button backButton;
    private GroupNameForm nameForm;
    private GroupPresetsForm presetsForm;
    private GroupAccessMgtForm accessMgmtForm;
    private GroupReqSharingForm reqSharingForm;
    private GroupReqMemberInfoForm reqMemberInfoForm;
    private FormButtonBinding formButtonBinding;
    private GroupMemberRightsForm memberRightsForm;
    private GroupLoginForm groupLoginForm;

    public GroupCreator(Controller c) {
        super(c);
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
            showReqMemberInfo();
        } else if (active.equals(reqMemberInfoForm)) {
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
            showReqMemberInfo();
        } else if (active.equals(reqMemberInfoForm)) {
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
            onShow(event);

        } else if (type.equals(GroupCreateEvents.CreateComplete)) {
            // LOG.fine( "CreateGroupComplete");
            onComplete(event);

        } else if (type.equals(GroupCreateEvents.CreateFailed)) {
            // LOG.warning( "CreateGroupFailed");
            onFailed(event);

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    private void hideDialog() {
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
                        onSubmit();
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

        window.setButtonAlign(HorizontalAlignment.RIGHT);
        window.addButton(backButton);
        window.addButton(nextButton);
    }

    private void initForms() {

        nameForm = new GroupNameForm();
        presetsForm = new GroupPresetsForm();
        accessMgmtForm = new GroupAccessMgtForm();
        reqSharingForm = new GroupReqSharingForm();
        reqMemberInfoForm = new GroupReqMemberInfoForm();
        memberRightsForm = new GroupMemberRightsForm();
        groupLoginForm = new GroupLoginForm();

        window.add(nameForm);
        window.add(presetsForm);
        window.add(accessMgmtForm);
        window.add(reqSharingForm);
        window.add(reqMemberInfoForm);
        window.add(memberRightsForm);
        window.add(groupLoginForm);
    }

    @Override
    protected void initialize() {
        super.initialize();

        window = new CenteredWindow();
        window.setHeading("Create new group");
        window.setSize(500, 450);

        layout = new CardLayout();
        window.setLayout(layout);

        initForms();
        initButtons();
    }

    private void onComplete(AppEvent event) {
        hideDialog();
    }

    private void onFailed(AppEvent event) {
        setBusy(false);
        MessageBox.confirm(null, "Failed to create group, retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                    onSubmit();
                } else {
                    hideDialog();
                }

            }
        });
    }

    private void onShow(AppEvent event) {
        showNameForm();
        window.show();
        window.center();
    }

    private void onSubmit() {
        setBusy(true);

        AppEvent event = new GroupCreateRequest();
        // TODO
        fireEvent(event);
    }

    private void setBusy(boolean busy) {
        if (busy) {
            nextButton.setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            nextButton.setIcon(SenseIconProvider.ICON_BUTTON_GO);
        }
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

    private void showReqMemberInfo() {

        // update content
        layout.setActiveItem(reqMemberInfoForm);

        // update buttons
        nextButton.setText("Next");
        backButton.setEnabled(true);

        // button binding
        if (null != formButtonBinding) {
            formButtonBinding.removeButton(nextButton);
        }
        formButtonBinding = new FormButtonBinding(reqMemberInfoForm);
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

}
