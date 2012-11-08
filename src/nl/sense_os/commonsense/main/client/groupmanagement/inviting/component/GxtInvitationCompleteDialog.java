package nl.sense_os.commonsense.main.client.groupmanagement.inviting.component;

import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InvitationCompleteView;
import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InviteUserView.Presenter;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtInvitationCompleteDialog implements InvitationCompleteView {

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
    public void show() {
        messageBox = MessageBox.info("Success", "The user was added to the group",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (null != presenter) {
                            presenter.onCancelClick();
                        }
                    }
                });

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
