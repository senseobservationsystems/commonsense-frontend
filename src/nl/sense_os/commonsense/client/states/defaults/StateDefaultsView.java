package nl.sense_os.commonsense.client.states.defaults;

import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class StateDefaultsView extends View {

    private static final Logger LOG = Logger.getLogger(StateDefaultsView.class.getName());

    private StateDefaultsDialog dialog;

    public StateDefaultsView(Controller c) {
        super(c);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(StateDefaultsEvents.CheckDefaults)) {
            LOG.finest("CheckDefaults");
            showDialog();

        } else if (type.equals(StateDefaultsEvents.CheckDefaultsSuccess)) {
            LOG.finest("CheckDefaultsSuccess");
            onCheckDefaultsSucess();

        } else if (type.equals(StateDefaultsEvents.CheckDefaultsFailure)) {
            LOG.warning("CheckDefaultsFailure");
            onCheckDefaultsFailure();

        } else {
            LOG.warning("Unexpected event type: " + type);
        }
    }

    @Override
    protected void initialize() {
        dialog = new StateDefaultsDialog();
        dialog.getSubmitButton().addListener(Events.Select, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                submit();
            }
        });
        dialog.getCancelButton().addListener(Events.Select, new Listener<ButtonEvent>() {

            @Override
            public void handleEvent(ButtonEvent be) {
                dialog.hide();
            }
        });
        super.initialize();
    }

    private void onCheckDefaultsFailure() {
        dialog.setBusy(false);

        MessageBox.confirm(null, "Failed to create the default states! Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            submit();
                        } else {
                            dialog.hide();
                        }
                    }
                });

    }

    private void onCheckDefaultsSucess() {
        dialog.hide();
        MessageBox.info(null, "The default states were created successfully.", null);
    }

    private void showDialog() {
        dialog.show();
    }

    private void submit() {
        dialog.setBusy(true);

        AppEvent checkDefaults = new AppEvent(StateDefaultsEvents.CheckDefaultsRequest);
        checkDefaults.setData("devices", dialog.getGrid().getSelectionModel().getSelection());
        checkDefaults.setData("overwrite", dialog.isOverwrite());
        checkDefaults.setSource(this);
        fireEvent(checkDefaults);
    }
}
