package nl.sense_os.commonsense.main.client.groupmanagement.inviting.component;

import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InviteUserView;
import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GxtInviteUserDialog extends CenteredWindow implements InviteUserView {
    private Button btnAdd;
    private Button btnCancel;
    private TextField<String> txtfldUsername;
    private Presenter presenter;

    public GxtInviteUserDialog() {
        setSize("300px", "210px");
        setClosable(false);
        setHeadingText("Add user to group");
        setLayout(new FitLayout());

        FormPanel frmpnlNewFormpanel = new FormPanel();
        frmpnlNewFormpanel.setScrollMode(Scroll.AUTOY);
        frmpnlNewFormpanel.setBodyBorder(false);
        frmpnlNewFormpanel.setLabelAlign(LabelAlign.TOP);
        frmpnlNewFormpanel.setHeaderVisible(false);
        frmpnlNewFormpanel.setHeadingText("New FormPanel");
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

        addListeners();
    }

    private void addListeners() {
        btnAdd.addListener(Events.OnClick, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                if (null != presenter) {
                    presenter.onSubmitClick();
                }
            }
        });
        
        btnCancel.addListener(Events.OnClick, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                if (null != presenter) {
                    presenter.onCancelClick();
                }
            }
        });
    }

    @Override
    public String getUsername() {
        return txtfldUsername.getValue();
    }

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            btnAdd.setIconStyle("sense-btn-icon-loading");
        } else {
            btnAdd.setIconStyle("sense-btn-icon-go");
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
