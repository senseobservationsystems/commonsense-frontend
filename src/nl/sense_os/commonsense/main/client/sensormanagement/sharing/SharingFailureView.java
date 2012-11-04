package nl.sense_os.commonsense.main.client.sensormanagement.sharing;

import nl.sense_os.commonsense.main.client.sensormanagement.sharing.ShareWithUserView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface SharingFailureView extends ErrorDialog {

    void setPresenter(Presenter presenter);
}
