package nl.sense_os.commonsense.main.client.groupmanagement.inviting;

import nl.sense_os.commonsense.main.client.groupmanagement.inviting.InviteUserView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface InvitationCompleteView extends InfoDialog {

    void setPresenter(Presenter presenter);
}
