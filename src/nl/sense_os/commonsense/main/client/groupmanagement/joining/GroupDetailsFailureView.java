package nl.sense_os.commonsense.main.client.groupmanagement.joining;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface GroupDetailsFailureView extends ErrorDialog {

    void setPresenter(Presenter presenter);
}
