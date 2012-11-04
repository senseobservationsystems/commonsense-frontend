package nl.sense_os.commonsense.main.client.groupmanagement.creating.component;

import nl.sense_os.commonsense.main.client.groupmanagement.creating.CreationSuccessView;
import nl.sense_os.commonsense.main.client.groupmanagement.creating.GroupCreatorView.Presenter;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtCreationSuccessDialog implements CreationSuccessView {

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
    public void show() {
        messageBox = MessageBox.info("Success!", "Group created", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (null != presenter) {
                    presenter.onCancelClick();
                }
            }
        });
    }
}
