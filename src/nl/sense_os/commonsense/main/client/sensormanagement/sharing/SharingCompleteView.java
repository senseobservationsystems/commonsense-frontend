package nl.sense_os.commonsense.main.client.sensormanagement.sharing;

import nl.sense_os.commonsense.main.client.sensormanagement.sharing.ShareWithUserView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface SharingCompleteView extends InfoDialog {

    void setPresenter(Presenter presenter);
}
