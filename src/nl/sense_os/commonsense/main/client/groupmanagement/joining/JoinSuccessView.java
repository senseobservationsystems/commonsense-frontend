package nl.sense_os.commonsense.main.client.groupmanagement.joining;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface JoinSuccessView extends InfoDialog {

    void setPresenter(Presenter presenter);
}
