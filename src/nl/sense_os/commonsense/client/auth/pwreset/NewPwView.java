package nl.sense_os.commonsense.client.auth.pwreset;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.main.components.NavPanel;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;

public class NewPwView extends View {

    private static final Logger LOG = Logger.getLogger(NewPwView.class.getName());
    private String token;
    private NewPwDialog dialog;

    public NewPwView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (PwResetEvents.ShowNewPasswordForm.equals(type)) {
            LOG.finest("ShowNewPasswordForm");
            LayoutContainer parent = event.getData("parent");
            show(parent);

        } else if (PwResetEvents.NewPasswordSuccess.equals(type)) {
            LOG.finest("NewPasswordSuccess");
            onSuccess();

        } else if (PwResetEvents.NewPasswordFailure.equals(type)) {
            LOG.finest("NewPasswordFailure");
            onFailure();

        } else {
            LOG.severe("Unexpected event: " + event);
        }
    }

    private void onFailure() {
        dialog.setBusy(false);
        MessageBox.alert("Password reset", "Failed to set the new password!", null);
    }

    private void onSuccess() {
        dialog.setBusy(false);
        MessageBox.info("Password reset", "Your password was successfully changed.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        dialog.hide();
                        History.newItem(NavPanel.HOME);
                    }
                });
    }

    private void show(LayoutContainer parent) {

        token = Location.getParameter("token");

        dialog = new NewPwDialog();
        dialog.show();

        dialog.getForm().addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                submit();
            }
        });

        dialog.getBtnCancel().addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                dialog.hide();
                History.newItem(NavPanel.HOME);
            }
        });
    }

    private void submit() {
        dialog.setBusy(true);

        AppEvent event = new AppEvent(PwResetEvents.NewPasswordRequest);
        event.setSource(this);
        event.setData("token", token);
        event.setData("password", dialog.getPassword());
        Dispatcher.forwardEvent(event);
    }
}
