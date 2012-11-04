package nl.sense_os.commonsense.main.client.sensormanagement.unsharing.component;

import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.ConfirmUnshareView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtConfirmUnshareDialog implements ConfirmUnshareView {

    private MessageBox messageBox;
    private Presenter presenter;

    @Override
    public void hide() {
        if (null != messageBox) {
            messageBox.close();
            messageBox = null;
        }
    }

    private void onCancelClick() {
        if (null != presenter) {
            presenter.onCancelClick();
        }
    }

    private void onConfirmClick() {
        if (null != presenter) {
            presenter.onSubmitClick();
        }
    }

    @Override
    public void setBusy(boolean busy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show() {
        messageBox = MessageBox.confirm(null, "Are you sure you want to stop sharing this sensor?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            onConfirmClick();
                        } else {
                            onCancelClick();
                        }
                    }
                });
    }

}
