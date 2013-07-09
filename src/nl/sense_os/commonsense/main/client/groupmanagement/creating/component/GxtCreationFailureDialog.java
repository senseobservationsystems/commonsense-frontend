package nl.sense_os.commonsense.main.client.groupmanagement.creating.component;

import nl.sense_os.commonsense.main.client.groupmanagement.creating.CreationFailureView;
import nl.sense_os.commonsense.main.client.groupmanagement.creating.GroupCreatorView.Presenter;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtCreationFailureDialog implements CreationFailureView {

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
            presenter.onCreateClick();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(int code, Throwable error) {
        String msg = "Failed to create group! Error: " + code + " " + error.getMessage() + "."
                + "<br/><br/>Do you want to retry?";
        messageBox = MessageBox.confirm("Failure", msg, new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getHtml().equalsIgnoreCase("yes")) {
                    onConfirmClick();
                } else {
                    onCancelClick();
                }
            }
        });
    }
}
