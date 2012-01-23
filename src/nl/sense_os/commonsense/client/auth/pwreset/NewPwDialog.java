package nl.sense_os.commonsense.client.auth.pwreset;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.DialogBox;

public class NewPwDialog extends DialogBox {

    public NewPwDialog() {
        setModal(false);
        setSize("300px", "");
        setHTML("Reset password");

        NewPwForm newPwForm = new NewPwForm();
        setWidget(newPwForm);
        newPwForm.setSize("auto", "100%");

        com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                center();
            }
        });
    }
}
