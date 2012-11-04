package nl.sense_os.commonsense.main.client.sensormanagement.publishing.component;

import nl.sense_os.commonsense.main.client.sensormanagement.publishing.PublicationFailureView;
import nl.sense_os.commonsense.main.client.sensormanagement.publishing.ConfirmPublicationView.Presenter;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GxtPublicationFailureDialog implements PublicationFailureView {

    private Presenter presenter;
    private MessageBox messageBox;

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
            presenter.onPublishClick();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(int code, Throwable error) {
        String message = "Publication failed! Error: " + code + ", message: '" + error.getMessage()
                + "'.<br/><br/>Do you want to try again?";
        messageBox = MessageBox.prompt("Fail!", message, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                Button b = be.getButtonClicked();
                if (b.getText().equalsIgnoreCase("yes")) {
                    onConfirmClick();
                } else {
                    onCancelClick();
                }
            }
        });
    }
}
