package nl.sense_os.commonsense.main.client.groupmanagement.inviting.component;

import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InvitationFailureView;
import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InviteUserView.Presenter;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtInvitationFailureDialog implements InvitationFailureView {

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
        messageBox = MessageBox.alert("Failure", "Failed to add the user to the group! Error: "
                + code + " "
                + error.getMessage()
                + ".<br/><br/>Are you sure the user has requested to join the group?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (null != presenter) {
                            presenter.onCancelClick();
                        }
                    }
                });
    }

}
