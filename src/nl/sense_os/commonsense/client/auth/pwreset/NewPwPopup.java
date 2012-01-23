package nl.sense_os.commonsense.client.auth.pwreset;

import com.google.gwt.user.client.ui.PopupPanel;

public class NewPwPopup extends PopupPanel {

    public NewPwPopup() {
        super(true);

        NewPwForm newPwForm = new NewPwForm();
        setWidget(newPwForm);
        newPwForm.setSize("100%", "100%");
    }

}
