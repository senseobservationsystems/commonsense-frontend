package nl.sense_os.commonsense.main.client.sensormanagement.deleting.component;

import nl.sense_os.commonsense.main.client.sensormanagement.deleting.ConfirmRemovalView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.deleting.RemovalFailedView;

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
    public void show(int code, Throwable error) {
        messageBox = MessageBox.confirm("Failure",
                "Removal failed! Code: " + code + " " + error.getMessage() + "."
                        + "<br/><br/>Retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        Button b = be.getButtonClicked();
                        if (b.getHtml().equalsIgnoreCase("no")) {
                            onCancelClick();
                        } else {
                            onOkClick();
                        }
                    }
                });

    }
}
