package nl.sense_os.commonsense.main.client.sensormanagement.unsharing;

import nl.sense_os.commonsense.main.client.sensormanagement.unsharing.ConfirmUnshareView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface UnsharingFailureView extends ErrorDialog {

    void setPresenter(Presenter presenter);
}
