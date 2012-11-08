package nl.sense_os.commonsense.main.client.groupmanagement.joining.component;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;
import nl.sense_os.commonsense.main.client.groupmanagement.joining.JoinFailureView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtJoinFailureDialog implements JoinFailureView {

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
        messageBox = MessageBox.confirm("Failure", "Failed to get join the group. Error: " + code
                + " " + error.getMessage() + ".<br/><br/>Do you want to retry?",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                            if (null != presenter) {
                                presenter.onSubmitClick();
                            }
                        } else {
                            if (null != presenter) {
                                presenter.onCancelClick();
                            }
                        }
                    }
                });

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

}
