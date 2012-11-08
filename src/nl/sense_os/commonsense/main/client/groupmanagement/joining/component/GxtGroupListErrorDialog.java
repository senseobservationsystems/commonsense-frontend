package nl.sense_os.commonsense.main.client.groupmanagement.joining.component;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupListFailureView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtGroupListErrorDialog implements GroupListFailureView {

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
    public void show(int code, Throwable error) {
        messageBox = MessageBox.alert("Failure", "Failed to get list of groups to join. Error: "
                + code + " " + error.getMessage(), new Listener<MessageBoxEvent>() {

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
