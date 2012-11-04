package nl.sense_os.commonsense.main.client.sensormanagement.sharing.component;

import nl.sense_os.commonsense.main.client.sensormanagement.sharing.ShareWithUserView.Presenter;
import nl.sense_os.commonsense.main.client.sensormanagement.sharing.SharingCompleteView;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GxtSharingCompleteDialog implements SharingCompleteView {

    private Presenter presenter;
    private MessageBox messageBox;

    @Override
    public void hide() {
        if (null != messageBox) {
            messageBox.close();
            messageBox = null;
        }
    }

    private void onOkClick() {
        if (null != presenter) {
            presenter.onCancelClick();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

    }

    @Override
    public void show() {
        messageBox = MessageBox.info("Success!", "Sharing complete",
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        onOkClick();
                    }
                });
    }

}
