package nl.sense_os.commonsense.main.client.groupmanagement.leaving;

import nl.sense_os.commonsense.main.client.groupmanagement.leaving.ConfirmGroupLeaveView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface LeavingCompleteView extends InfoDialog {

    void setPresenter(Presenter presenter);
}
