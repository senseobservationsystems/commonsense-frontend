package nl.sense_os.commonsense.main.client.sensormanagement.sharing.component;

import nl.sense_os.commonsense.main.client.sensormanagement.sharing.ShareWithUserView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.SharingFailureView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GxtSharingFailureDialog implements SharingFailureView {

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
        messageBox = MessageBox.prompt("Failure",
                "Removal failed! Code: " + code + " " + error.getMessage() + "."
                        + "<br/><br/>Retry?", new Listener<MessageBoxEvent>() {

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
