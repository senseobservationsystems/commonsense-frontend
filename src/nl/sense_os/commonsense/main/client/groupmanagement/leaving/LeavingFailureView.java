package nl.sense_os.commonsense.main.client.groupmanagement.leaving;

import nl.sense_os.commonsense.main.client.groupmanagement.leaving.ConfirmGroupLeaveView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface LeavingFailureView extends ErrorDialog {

    void setPresenter(Presenter presenter);
}
