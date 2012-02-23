package nl.sense_os.commonsense.client.groups.invite;

import nl.sense_os.commonsense.client.common.models.GroupModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public class GroupInviteWindow extends Window {
    private Button btnAdd;
    private Button btnCancel;
    private TextField<String> txtfldUsername;

    public GroupInviteWindow(GroupModel group) {
        setSize("300px", "210px");
        setClosable(false);
        setHeading("Add user to " + group.getName());
        setLayout(new FitLayout());

        FormPanel frmpnlNewFormpanel = new FormPanel();
        frmpnlNewFormpanel.setScrollMode(Scroll.AUTOY);
        frmpnlNewFormpanel.setBodyBorder(false);
        frmpnlNewFormpanel.setLabelAlign(LabelAlign.TOP);
        frmpnlNewFormpanel.setHeaderVisible(false);
        frmpnlNewFormpanel.setHeading("New FormPanel");
        frmpnlNewFormpanel.setCollapsible(true);

        LabelField lblfldExplanation = new LabelField(
                "Enter the username of the person that you want to add to the group.");
        lblfldExplanation.setHideLabel(true);
        frmpnlNewFormpanel.add(lblfldExplanation, new FormData("100%"));

        txtfldUsername = new TextField<String>();
        txtfldUsername.setAllowBlank(false);
        frmpnlNewFormpanel.add(txtfldUsername, new FormData("100%"));
        txtfldUsername.setFieldLabel("Username");

        LabelField lblfldNotice = new LabelField(
                "Please note: you can only add users to the group if they have already requested to join it.");
        lblfldNotice.setHideLabel(true);
        frmpnlNewFormpanel.add(lblfldNotice, new FormData("100%"));

        btnAdd = new Button("Add");
        btnAdd.setIconStyle("sense-btn-icon-go");
        frmpnlNewFormpanel.addButton(btnAdd);
        new FormButtonBinding(frmpnlNewFormpanel).addButton(btnAdd);

        btnCancel = new Button("Cancel");
        frmpnlNewFormpanel.addButton(btnCancel);
        add(frmpnlNewFormpanel);

        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                center();
            }
        });
    }

    public TextField<String> getUsername() {
        return txtfldUsername;
    }

    public Button getBtnAdd() {
        return btnAdd;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }
}
