package nl.sense_os.commonsense.main.client.sensormanagement.sharing.component;

import java.util.logging.Logger;

import nl.sense_os.commonsense.main.client.gxt.component.CenteredWindow;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.ShareWithUserView;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

public class GxtShareWithUserDialog extends CenteredWindow implements ShareWithUserView {

    private static final Logger LOG = Logger.getLogger(GxtShareWithUserDialog.class.getName());

    private Presenter presenter;

    private FormPanel form;
    private TextField<String> user;
    private Button createButton;
    private Button cancelButton;

    public GxtShareWithUserDialog() {

        setHeadingText("Manage data sharing");
        setLayout(new FitLayout());
        setSize(323, 200);

        initForm();
    }

    private void initButtons() {

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Button b = ce.getButton();
                if (b.equals(createButton)) {
                    onSubmitClick();
                } else if (b.equals(cancelButton)) {
                    onCancelClick();
                } else {
                    LOG.warning("Unexpected button pressed");
                }
            }
        };

        createButton = new Button("Share", l);
        createButton.setIconStyle("sense-btn-icon-go");
        cancelButton = new Button("Cancel", l);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(createButton);

        form.addButton(createButton);
        form.addButton(cancelButton);
    }

    private void onCancelClick() {
        if (null != presenter) {
            presenter.onCancelClick();
        }
    }

    private void onSubmitClick() {
        if (form.isValid() && null != presenter) {
            presenter.onSubmitClick();
        }
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        user = new TextField<String>();
        user.setFieldLabel("Share with");
        user.setEmptyText("Enter a username...");
        user.setAllowBlank(false);

        form.add(user, formData);
    }

    private void initForm() {
        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setScrollMode(Scroll.AUTOY);

        initFields();
        initButtons();

        add(form);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getUsername() {
        return user.getValue();
    }

    @Override
    public void setBusy(boolean busy) {
        if (busy) {
            createButton.setIconStyle("sense-btn-icon-loading");
            cancelButton.disable();
        } else {
            createButton.setIconStyle("sense-btn-icon-go");
            cancelButton.enable();
        }
    }

}
