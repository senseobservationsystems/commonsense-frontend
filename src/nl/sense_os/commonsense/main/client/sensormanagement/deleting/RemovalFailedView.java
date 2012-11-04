package nl.sense_os.commonsense.main.client.sensormanagement.deleting;

import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface RemovalFailedView extends ErrorDialog {

    void setPresenter(ConfirmRemovalView.Presenter presenter);
}
