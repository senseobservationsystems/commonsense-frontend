package nl.sense_os.commonsense.main.client.sensormanagement.delete.component;

import nl.sense_os.commonsense.main.client.sensormanagement.delete.ConfirmRemovalView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.delete.RemovalFailedView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GxtRemovalFailedDialog implements RemovalFailedView {

    private Presenter presenter;
    private MessageBox messageBox;

    @Override
    public void hide() {
        if (null != messageBox) {
            messageBox.close();
        }
        messageBox = null;
    }

    private void onCancelClick() {
        if (null != presenter) {
            presenter.onCancelClick();
        }
    }

    private void onOkClick() {
        if (null != presenter) {
            presenter.onDeleteClick();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

    }

    @Override
    public void show() {
        messageBox = MessageBox.prompt("Failure", "Removal failed, retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button b = be.getButtonClicked();
                        if (b.getText().equalsIgnoreCase("cancel")) {
                            onCancelClick();
                        } else {
                            onOkClick();
                        }
                    }
                });

    }
}
