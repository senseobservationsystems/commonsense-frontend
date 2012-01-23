package nl.sense_os.commonsense.client.auth.pwreset;

import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class NewPwForm extends Composite {

    private final static Logger LOG = Logger.getLogger(NewPwForm.class.getName());
    private TextBox textBoxEmail;
    private TextBox textBoxUsername;
    private RadioButton rdbtnUsername;
    private RadioButton rdbtnEmail;

    public NewPwForm() {

        // LOG.setLevel(Level.ALL);

        FormPanel formPanel = new FormPanel();
        formPanel.setStyleName("pw-reset-form");
        initWidget(formPanel);
        formPanel.setSize("", "");

        VerticalPanel verticalPanel = new VerticalPanel();
        formPanel.setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");

        Label lblNewLabel = new Label(
                "Please enter your email address or username so we can send you an email to reset your password.");
        lblNewLabel.setStyleName("pw-reset-label");
        verticalPanel.add(lblNewLabel);

        rdbtnEmail = new RadioButton("group", "Email:");
        rdbtnEmail.setValue(true);
        rdbtnEmail.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                LOG.finest("Email value change: " + event.getValue());
                textBoxEmail.setEnabled(event.getValue());
                textBoxUsername.setEnabled(!event.getValue());
            }
        });
        verticalPanel.add(rdbtnEmail);

        textBoxEmail = new TextBox();
        textBoxEmail.setStyleName("pw-reset-field");
        textBoxEmail.setName("email");
        verticalPanel.add(textBoxEmail);
        textBoxEmail.setSize("100%", "");

        Label lblOr = new Label("or,");
        lblOr.setStyleName("pw-reset-label");
        verticalPanel.add(lblOr);

        rdbtnUsername = new RadioButton("group", "Username:");
        rdbtnUsername.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                LOG.finest("Username value change: " + event.getValue());
                textBoxEmail.setEnabled(!event.getValue());
                textBoxUsername.setEnabled(event.getValue());
            }
        });
        verticalPanel.add(rdbtnUsername);

        textBoxUsername = new TextBox();
        textBoxUsername.setStyleName("pw-reset-field");
        textBoxUsername.setEnabled(false);
        textBoxUsername.setName("username");
        verticalPanel.add(textBoxUsername);
        textBoxUsername.setSize("100%", "");

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        verticalPanel.add(horizontalPanel);
        horizontalPanel.setSize("100%", "");

        Button btnSubmit = new Button("Submit");
        horizontalPanel.add(btnSubmit);
    }

}
