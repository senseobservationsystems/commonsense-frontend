package nl.sense_os.commonsense.main.client.groupmanagement.creating;

import nl.sense_os.commonsense.main.client.groupmanagement.creating.GroupCreatorView.Presenter;
import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface CreationSuccessView extends InfoDialog {

    void setPresenter(Presenter presenter);
}
