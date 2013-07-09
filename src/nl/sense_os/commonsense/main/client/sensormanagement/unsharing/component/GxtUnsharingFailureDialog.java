package nl.sense_os.commonsense.main.client.sensormanagement.unsharing.component;


import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.ConfirmUnshareView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.UnsharingFailureView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GxtUnsharingFailureDialog implements UnsharingFailureView {

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

    private void onOkClick() {
        if (null != presenter) {
            presenter.onSubmitClick();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(int code, Throwable error) {
        messageBox = MessageBox.confirm("Failure",
                "Unsharing failed! Code: " + code + " " + error.getMessage() + "."
                        + "<br/><br/>Retry?", new Listener<MessageBoxEvent>() {

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
