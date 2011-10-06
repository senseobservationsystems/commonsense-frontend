package nl.sense_os.commonsense.client.groups.create.forms;

import nl.sense_os.commonsense.client.common.components.CenteredWindow;
import nl.sense_os.commonsense.client.common.utility.SenseIconProvider;
import nl.sense_os.commonsense.client.groups.create.GroupCreateRequest;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;

import java.util.logging.Logger;

public class GroupCreatorWindow extends CenteredWindow {

    private static final Logger LOG = Logger.getLogger(GroupCreatorWindow.class.getName());
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

    public GroupCreatorWindow() {
        setHeading("Create new group");
        setSize(500, 450);

        layout = new CardLayout();
        setLayout(layout);

        initForms();
        initButtons();

        showNameForm();
    }

    public void createGroup() {
        setBusy(true);

        AppEvent event = new GroupCreateRequest();

        // TODO
        Dispatcher.forwardEvent(event);
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
                        createGroup();
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

        addButton(backButton);
        addButton(nextButton);
    }

    private void initForms() {

        nameForm = new GroupNameForm();
        presetsForm = new GroupPresetsForm();
        accessMgmtForm = new GroupAccessMgtForm();
        reqSharingForm = new GroupReqSharingForm();
        memberRightsForm = new GroupMemberRightsForm();
        groupLoginForm = new GroupLoginForm();

        add(nameForm);
        add(presetsForm);
        add(accessMgmtForm);
        add(reqSharingForm);
        add(memberRightsForm);
        add(groupLoginForm);
    }

    public void setBusy(boolean busy) {
        if (busy) {
            setIcon(SenseIconProvider.ICON_LOADING);
        } else {
            setIcon(IconHelper.create(""));
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
}
