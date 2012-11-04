package nl.sense_os.commonsense.main.client.groupmanagement.leaving.component;

import nl.sense_os.commonsense.main.client.groupmanagement.leaving.ConfirmGroupLeaveView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

public class GxtConfirmLeaveDialog implements ConfirmGroupLeaveView {

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
    public void setBusy(boolean busy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(String name) {

        messageBox = MessageBox.confirm(null, "Are you sure you want to leave the group '" + name
                + "'?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                Button clicked = be.getButtonClicked();
                if ("yes".equalsIgnoreCase(clicked.getText())) {
                    if (null != presenter) {
                        presenter.onLeaveClick();
                    }
                }
            }
        });
    }
}
