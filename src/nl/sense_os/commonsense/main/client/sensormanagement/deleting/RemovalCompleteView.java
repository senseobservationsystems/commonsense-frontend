package nl.sense_os.commonsense.main.client.sensormanagement.deleting;

import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface RemovalCompleteView extends InfoDialog {

    void setPresenter(ConfirmRemovalView.Presenter presenter);
}
