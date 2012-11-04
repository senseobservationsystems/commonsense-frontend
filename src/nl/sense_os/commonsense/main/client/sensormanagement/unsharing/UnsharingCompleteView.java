package nl.sense_os.commonsense.main.client.sensormanagement.unsharing;

import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.ConfirmUnshareView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface UnsharingCompleteView extends InfoDialog {

    void setPresenter(Presenter presenter);
}
