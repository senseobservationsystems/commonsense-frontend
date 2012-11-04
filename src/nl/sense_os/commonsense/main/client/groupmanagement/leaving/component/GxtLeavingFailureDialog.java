package nl.sense_os.commonsense.main.client.groupmanagement.leaving.component;

import nl.sense_os.commonsense.main.client.groupmanagement.leaving.ConfirmGroupLeaveView.Presenter;
import nl.sense_os.commonsense.main.client.groupmanagement.leaving.LeavingFailureView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtLeavingFailureDialog implements LeavingFailureView {

    private Presenter presenter;
    private MessageBox messageBox;

    @Override
    public void hide() {
        if (null != messageBox) {
            messageBox.close();
            messageBox = null;
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(int code, Throwable error) {
        messageBox = MessageBox.confirm("Failure", "Failed to leave the group! Error: " + code
                + " " + error.getMessage() + ".<br/><br/>Do you want to retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equals("yes")) {
                            if (null != presenter) {
                                presenter.onLeaveClick();
                            }
                        } else {
                            if (null != presenter) {
                                presenter.onCancelClick();
                            }
                        }

                    }
                });
    }
}
