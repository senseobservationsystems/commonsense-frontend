package nl.sense_os.commonsense.main.client.groupmanagement.leaving.component;

import nl.sense_os.commonsense.main.client.groupmanagement.leaving.ConfirmGroupLeaveView.Presenter;
import nl.sense_os.commonsense.main.client.groupmanagement.leaving.LeavingCompleteView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtLeavingCompleteDialog implements LeavingCompleteView {

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
        messageBox = MessageBox.info("Success", "Successfully left the group",
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
