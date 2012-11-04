package nl.sense_os.commonsense.main.client.groupmanagement.creating;

import nl.sense_os.commonsense.main.client.groupmanagement.creating.GroupCreatorView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface CreationFailureView extends ErrorDialog {

    void setPresenter(Presenter presenter);
}
