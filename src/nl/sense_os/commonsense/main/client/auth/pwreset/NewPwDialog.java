package nl.sense_os.commonsense.main.client.auth.pwreset;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;

public class NewPwDialog extends Window {

    private FormPanel form;
    private Button btnSubmit;
    private Button btnCancel;
    private TextField<String> password;

    public NewPwDialog() {

        setSize(300, 200);
        setHeading("Choose new password");
        setLayout(new FitLayout());
        setClosable(false);

        form = new FormPanel();
        form.setHeaderVisible(false);
        form.setScrollMode(Scroll.AUTOY);
        form.setBodyBorder(false);
        form.setLabelAlign(LabelAlign.TOP);
        form.setAction("javascript:;");

        LabelField explanation = new LabelField("Please enter a new password for your account.");
        explanation.setHideLabel(true);

        password = new TextField<String>();
        password.setFieldLabel("New password");
        password.setPassword(true);
        password.setAllowBlank(false);

        TextField<String> retype = new TextField<String>();
        retype.setFieldLabel("Retype password");
        retype.setPassword(true);
        retype.setAllowBlank(false);
        retype.setValidator(new Validator() {

            @Override
            public String validate(Field<?> field, String value) {
                if (value != null && value.equals(password.getValue())) {
                    return null;
                } else {
                    return "Passwords do not match";
                }
            }
        });

        btnSubmit = new Button("Submit");
        btnSubmit.setIconStyle("sense-btn-icon-go");
        btnSubmit.setType("submit");
        new FormButtonBinding(form).addButton(btnSubmit);
        btnSubmit.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (form.isValid()) {
                    form.submit();
                }
            }
        });

        btnCancel = new Button("Cancel");

        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                center();
            }
        });

        form.add(explanation, new FormData("-5"));
        form.add(password, new FormData("-20"));
        form.add(retype, new FormData("-20"));
        form.addButton(btnSubmit);
        form.addButton(btnCancel);

        add(form, new FitData());
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public FormPanel getForm() {
        return form;
    }

    public String getPassword() {
        return form.isValid() ? password.getValue() : null;
    }

    public void setBusy(boolean busy) {
        if (busy) {
            btnSubmit.setIconStyle("sense-btn-icon-loading");
            btnSubmit.setEnabled(false);
        } else {
            btnSubmit.setIconStyle("sense-btn-icon-go");
            btnSubmit.setEnabled(true);
        }
    }
}
