package nl.sense_os.commonsense.client.auth.pwreset;

import java.util.logging.Logger;

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
import com.extjs.gxt.ui.client.widget.MessageBox;

public class ForgotPwView extends View {

    private static final Logger LOG = Logger.getLogger(ForgotPwView.class.getName());
    private ForgotPwDialog dialog;

    public ForgotPwView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();

        if (type.equals(PwResetEvents.ShowDialog)) {
            LOG.finest("ShowDialog");
            onShow();

        } else if (PwResetEvents.PwRemindSuccess.equals(type)) {
            LOG.finest("PwRemindSuccess");
            onSuccess();

        } else if (PwResetEvents.PwRemindFailure.equals(type)) {
            LOG.warning("PwRemindFailure");
            onFailure();

        } else if (PwResetEvents.PwRemindNotFound.equals(type)) {
            LOG.warning("PwRemindNotFound");
            onNotFound();

        } else {
            LOG.severe("Unexpected event! " + event);
        }
    }

    private void onFailure() {

        dialog.setBusy(false);

        MessageBox.confirm("Reset password",
                "Sorry, an error occurred during communication! Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            submit();
                        } else {
                            // do nothing
                        }
                    }
                });
    }

    private void onNotFound() {

        dialog.setBusy(false);

        MessageBox.alert("Reset password",
                "Sorry, we could not find any users with the details you entered...",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        // do nothing
                    }
                });
    }

    private void onShow() {
        dialog = new ForgotPwDialog();

        dialog.show();

        dialog.getBtnCancel().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                dialog.hide();
            }
        });

        dialog.getForm().addListener(Events.BeforeSubmit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                submit();
            }
        });
    }

    private void onSuccess() {

        dialog.setBusy(false);

        MessageBox.info("Reset password",
                "We sent you an email with a link to reset your password.",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        dialog.hide();
                    }
                });
    }

    private void submit() {
        dialog.setBusy(true);

        AppEvent event = new AppEvent(PwResetEvents.SubmitRequest);
        event.setData("email", dialog.getEmail());
        event.setData("username", dialog.getUsername());
        event.setSource(this);
        Dispatcher.forwardEvent(event);
    }
}
