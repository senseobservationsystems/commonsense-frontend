package nl.sense_os.commonsense.main.client.groupmanagement.joining.component;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupListWaitView;

import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtGroupListWaitDialog implements GroupListWaitView {

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
        // nothing to do
    }

    @Override
    public void show() {
        messageBox = MessageBox.wait("Join group", "Please wait...", "Getting list of groups");
    }

}
