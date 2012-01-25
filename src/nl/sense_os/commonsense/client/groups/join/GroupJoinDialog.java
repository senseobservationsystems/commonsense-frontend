package nl.sense_os.commonsense.client.groups.join;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.groups.join.forms.AllVisibleGroupsForm;
import nl.sense_os.commonsense.client.groups.join.forms.GroupNameForm;
import nl.sense_os.commonsense.client.groups.join.forms.GroupTypeForm;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public class GroupJoinDialog extends Window {

    private static final Logger LOG = Logger.getLogger(GroupJoinDialog.class.getName());

    private CardLayout layout;
    private final GroupTypeForm frmGroupType;
    private final AllVisibleGroupsForm frmPublicGroups;
    private final GroupNameForm frmPrivateGroup;
    private Button btnNext;
    private Button btnBack;
    private Button btnCancel;
    private FormButtonBinding buttonBinding;

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
        frmPublicGroups = new AllVisibleGroupsForm(groupLoader);
        add(frmPublicGroups);
        frmPrivateGroup = new GroupNameForm();
        add(frmPrivateGroup);

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
        return frmPublicGroups.getGroup();
    }

    public void goToNext() {
        Component active = layout.getActiveItem();
        if (active.equals(frmGroupType)) {
            if (frmGroupType.getType().equals("visible")) {
                showAllGroupsList();
            } else {
                showHiddenGroupForm();
            }
        } else if (active.equals(frmPublicGroups)) {
            // TODO
        } else if (active.equals(frmPrivateGroup)) {
            // TODO
        } else {
            LOG.warning("Unable to go to next card!");
        }
    }

    public void goToPrev() {
        Component active = layout.getActiveItem();
        if (active.equals(frmGroupType)) {
            // should never happen
        } else if (active.equals(frmPublicGroups)) {
            showGroupTypeChoice();
        } else if (active.equals(frmPrivateGroup)) {
            showGroupTypeChoice();
        } else {
            LOG.warning("Unable to go to previous card!");
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

    public void setBusy(boolean busy) {
        if (busy) {
            btnNext.setIconStyle("sense-btn-icon-loading");
            btnNext.disable();
        } else {
            btnNext.setIconStyle("sense-btn-icon-go");
            btnNext.enable();
        }
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

    private void showHiddenGroupForm() {
        // update active item
        layout.setActiveItem(frmPrivateGroup);

        // update buttons
        btnNext.setText("Next");
        btnBack.setEnabled(true);

        if (null != buttonBinding) {
            buttonBinding.removeButton(btnNext);
        }
        buttonBinding = new FormButtonBinding(frmPrivateGroup);
        buttonBinding.addButton(btnNext);
    }

    private void showAllGroupsList() {
        // update active item
        layout.setActiveItem(frmPublicGroups);

        // update buttons
        btnNext.setText("Next");
        btnBack.setEnabled(true);

        if (null != buttonBinding) {
            buttonBinding.removeButton(btnNext);
        }
        buttonBinding = new FormButtonBinding(frmPublicGroups);
        buttonBinding.addButton(btnNext);
    }
}
