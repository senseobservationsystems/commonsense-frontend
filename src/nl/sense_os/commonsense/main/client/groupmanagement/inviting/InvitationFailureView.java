package nl.sense_os.commonsense.main.client.groupmanagement.inviting;

import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InviteUserView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface InvitationFailureView extends ErrorDialog {

    void setPresenter(Presenter presenter);
}
