package nl.sense_os.commonsense.client.groups.join;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;
import nl.sense_os.commonsense.client.groups.join.forms.GroupTypeForm;
import nl.sense_os.commonsense.client.groups.join.forms.PublicGroupSelectionForm;

import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
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
    private GroupTypeForm frmGroupType;
    private PublicGroupSelectionForm frmGroupSelection;
    private Button btnNext;
    private Button btnBack;
    private Button btnCancel;
    private FormButtonBinding buttonBinding;

    public GroupJoinDialog(ListLoader<ListLoadResult<GroupModel>> groupLoader,
            List<SensorModel> sensorLibrary) {
        LOG.setLevel(Level.ALL);

        setHeading("Join a public group");
        setClosable(false);
        setSize(540, 480);

        layout = new CardLayout();
        setLayout(layout);

        frmGroupType = new GroupTypeForm();
        add(frmGroupType);
        frmGroupSelection = new PublicGroupSelectionForm(groupLoader);
        add(frmGroupSelection);

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
        return frmGroupSelection.getGroup();
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

    public void goToNext() {
        Component active = layout.getActiveItem();
        if (active.equals(frmGroupType)) {
            showGroupSelection();
        } else if (active.equals(frmGroupSelection)) {
            // TODO
        } else {
            LOG.warning("Unable to go to next card!");
        }
    }

    public void goToPrev() {
        Component active = layout.getActiveItem();
        if (active.equals(frmGroupType)) {
            // should never happen
        } else if (active.equals(frmGroupSelection)) {
            showGroupTypeSelection();
        } else {
            LOG.warning("Unable to go to previous card!");
        }
    }

    private void showGroupTypeSelection() {
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

    private void showGroupSelection() {
        // update active item
        layout.setActiveItem(frmGroupSelection);

        // update buttons
        btnNext.setText("Next");
        btnBack.setEnabled(true);

        if (null != buttonBinding) {
            buttonBinding.removeButton(btnNext);
        }
        buttonBinding = new FormButtonBinding(frmGroupSelection);
        buttonBinding.addButton(btnNext);
    }
}
