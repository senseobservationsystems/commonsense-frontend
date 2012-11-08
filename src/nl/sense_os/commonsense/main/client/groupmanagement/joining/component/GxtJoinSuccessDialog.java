package nl.sense_os.commonsense.main.client.groupmanagement.joining.component;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.JoinSuccessView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtJoinSuccessDialog implements JoinSuccessView {

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
        messageBox = MessageBox.info("Success", "You have joined the group!"
                + " Any sensors you selected were automatically shared with the group.",
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
